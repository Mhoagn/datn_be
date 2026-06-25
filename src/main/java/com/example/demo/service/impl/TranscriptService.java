package com.example.demo.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.dto.TranscriptDTO.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.SummaryDTO.FinalSummaryResponse;
import com.example.demo.dto.SummaryDTO.SaveFinalSummaryRequest;
import com.example.demo.dto.SummaryDTO.SummaryPointDTO;
import com.example.demo.dto.SummaryDTO.SummaryResponse;
import com.example.demo.entity.MeetingRecord;
import com.example.demo.entity.MeetingSummaryCandidate;
import com.example.demo.entity.MeetingSummaryFinal;
import com.example.demo.entity.MeetingSummaryPoint;
import com.example.demo.entity.MeetingTranscript;
import com.example.demo.entity.User;
import com.example.demo.exception.FinalSummaryNotFoundException;
import com.example.demo.exception.MeetingRecordNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.MeetingRecordRepository;
import com.example.demo.repository.MeetingSummaryCandidateRepository;
import com.example.demo.repository.MeetingSummaryFinalRepository;
import com.example.demo.repository.MeetingSummaryPointRepository;
import com.example.demo.repository.MeetingTranscriptRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.SecurityUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscriptService {

    private final MeetingRecordRepository meetingRecordRepository;
    private final MeetingTranscriptRepository transcriptRepository;
    private final MeetingSummaryCandidateRepository summaryCandidateRepository;
    private final MeetingSummaryPointRepository summaryPointRepository;
    private final MeetingSummaryFinalRepository summaryFinalRepository;
    private final UserRepository userRepository;
    private final AIServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;
    private final SecurityUtil securityUtil;

    @Value("${livekit.s3.bucket}")
    private String s3Bucket;

    @Value("${livekit.s3.region}")
    private String s3Region;

    @Value("${livekit.s3.access-key}")
    private String awsAccessKey;

    @Value("${livekit.s3.secret-key}")
    private String awsSecretKey;

    /**
     * Xử lý video sau khi record hoàn tất.
     *
     * Sử dụng Async Job Pattern để tránh lỗi ngrok ERR_NGROK_3004:
     *  - Gọi POST /start-video-processing → nhận job_id ngay lập tức (< 1 giây)
     *  - Poll GET /job-status/{job_id} mỗi 20 giây cho đến khi completed/failed
     *
     * Không đánh @Transactional ở đây vì method có thể chạy tới 40 phút
     * (giữ transaction mở lâu sẽ gây leak DB connection).
     * Mỗi thao tác DB được bọc trong @Transactional riêng ở các helper method.
     */
    @Async
    public void processRecordedVideo(Long recordId) {
        log.info("Bắt đầu xử lý video cho record ID: {}", recordId);

        // 1. Lấy thông tin record
        MeetingRecord record = meetingRecordRepository.findById(recordId)
                .orElseThrow(() -> new MeetingRecordNotFoundException("Record không tồn tại"));

        // 1.1. Validate s3Key
        if (record.getS3Key() == null || record.getS3Key().trim().isEmpty()) {
            log.error("Record ID {} chưa có s3Key. Video có thể chưa upload lên S3.", recordId);
            return;
        }

        // 1.2. Validate record status
        if (record.getStatus() != MeetingRecord.Status.COMPLETED) {
            log.error("Record ID {} chưa COMPLETED. Status hiện tại: {}", recordId, record.getStatus());
            return;
        }

        // 2. Khởi tạo hoặc reset MeetingTranscript → PROCESSING
        MeetingTranscript transcript = initOrResetTranscript(recordId, record);
        if (transcript == null) {
            return; // đã COMPLETED hoặc đang PROCESSING bởi thread khác
        }

        // 3. Khởi tạo hoặc reset MeetingSummaryCandidate → PROCESSING
        MeetingSummaryCandidate summaryCandidate = initOrResetSummaryCandidate(recordId, transcript, record);
        if (summaryCandidate == null) {
            return;
        }

        // 4. Gửi job lên AI-service (trả về job_id ngay, không giữ connection lâu)
        String jobId;
        try {
            log.info("Gửi job xử lý video lên AI-service (async job pattern)...");
            AIServiceRequest request = new AIServiceRequest(
                    s3Bucket, record.getS3Key(), s3Region, awsAccessKey, awsSecretKey);

            AIServiceJobStartResponse jobStart = aiServiceClient.startProcessingJob(request);
            jobId = jobStart.getJobId();
            log.info("AI-service đã nhận job, job_id = {}", jobId);

            // Ghi tạm job_id vào errorMessage để dễ debug khi backend restart
            transcript.setErrorMessage("PROCESSING:job_id=" + jobId);
            transcriptRepository.save(transcript);

        } catch (Exception e) {
            log.error("Không thể gửi job đến AI-service: {}", e.getMessage(), e);
            markFailed(transcript, summaryCandidate, "Không thể kết nối AI-service: " + e.getMessage());
            return;
        }

        // 5. Poll cho đến khi job hoàn thành (tối đa 40 phút)
        final int POLL_INTERVAL_MS = 20_000; // 20 giây
        final int MAX_ATTEMPTS = 120;        // 120 × 20s = 40 phút
        AIServiceResponse aiResponse = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                markFailed(transcript, summaryCandidate, "Polling bị ngắt");
                return;
            }

            try {
                AIServiceJobStatusResponse jobStatus = aiServiceClient.getJobStatus(jobId);
                log.info("Poll [{}/{}] job {} → status = {}",
                        attempt, MAX_ATTEMPTS, jobId, jobStatus.getStatus());

                if ("completed".equals(jobStatus.getStatus())) {
                    aiResponse = jobStatus.getResult();
                    break;
                } else if ("failed".equals(jobStatus.getStatus())) {
                    throw new RuntimeException("AI-service xử lý thất bại: " + jobStatus.getError());
                }
                // status == "processing" → tiếp tục poll

            } catch (RuntimeException re) {
                log.error("Lỗi khi poll job {}: {}", jobId, re.getMessage(), re);
                markFailed(transcript, summaryCandidate, re.getMessage());
                return;
            } catch (Exception e) {
                // Lỗi tạm thời (network flap, ngrok restart...) → thử lại lần sau
                log.warn("Lỗi tạm thời khi poll job {} (lần {}): {}", jobId, attempt, e.getMessage());
            }
        }

        if (aiResponse == null) {
            markFailed(transcript, summaryCandidate,
                    "Hết thời gian chờ AI-service sau 40 phút (job_id=" + jobId + ")");
            return;
        }

        // 6. Lưu kết quả vào DB
        try {
            if (!"success".equals(aiResponse.getStatus())) {
                throw new RuntimeException("AI service trả về status không thành công");
            }

            transcript.setSegments(convertSegmentsToJson(aiResponse.getTranscriptSegments()));
            transcript.setFullText(aiResponse.getFullText());
            transcript.setStatus(MeetingTranscript.Status.COMPLETED);
            transcript.setErrorMessage(null);
            transcriptRepository.save(transcript);
            log.info("Lưu transcript thành công cho record ID: {}", recordId);

            summaryCandidate.setRawSummary(aiResponse.getSummary());
            summaryCandidate.setStatus(MeetingSummaryCandidate.Status.COMPLETED);
            summaryCandidateRepository.save(summaryCandidate);

            saveSummaryPoints(summaryCandidate, aiResponse.getSummary());

        } catch (Exception e) {
            log.error("Lỗi khi lưu kết quả AI: {}", e.getMessage(), e);
            markFailed(transcript, summaryCandidate, e.getMessage());
        }
    }

    // =========================================================
    // Helper methods — mỗi cái có @Transactional riêng
    // =========================================================

    /**
     * Khởi tạo hoặc reset MeetingTranscript về trạng thái PROCESSING.
     * Trả về null nếu không cần xử lý (đã COMPLETED hoặc đang PROCESSING).
     */
    @Transactional
    protected MeetingTranscript initOrResetTranscript(Long recordId, MeetingRecord record) {
        MeetingTranscript existing = transcriptRepository.findByMeetingRecordId(recordId).orElse(null);

        if (existing != null) {
            if (existing.getStatus() == MeetingTranscript.Status.COMPLETED) {
                log.info("Transcript đã COMPLETED cho record ID: {}. Bỏ qua.", recordId);
                return null;
            }
            if (existing.getStatus() == MeetingTranscript.Status.PROCESSING) {
                log.info("Transcript đang PROCESSING cho record ID: {}. Bỏ qua.", recordId);
                return null;
            }
            // FAILED hoặc PENDING → reset để thử lại
            log.info("Reset transcript → PROCESSING cho record ID: {}", recordId);
            existing.setStatus(MeetingTranscript.Status.PROCESSING);
            existing.setFullText(null);
            existing.setSegments(null);
            existing.setErrorMessage(null);
            return transcriptRepository.save(existing);
        }

        try {
            MeetingTranscript t = new MeetingTranscript();
            t.setMeetingRecordId(recordId);
            t.setMeetingRecord(record);
            t.setStatus(MeetingTranscript.Status.PROCESSING);
            return transcriptRepository.save(t);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                log.warn("Transcript đã được tạo bởi thread khác cho record ID: {}. Bỏ qua.", recordId);
                return null;
            }
            throw e;
        }
    }

    /**
     * Khởi tạo hoặc reset MeetingSummaryCandidate về trạng thái PROCESSING.
     * Trả về null nếu có race condition.
     */
    @Transactional
    protected MeetingSummaryCandidate initOrResetSummaryCandidate(Long recordId,
                                                                   MeetingTranscript transcript,
                                                                   MeetingRecord record) {
        List<MeetingSummaryCandidate> existing = summaryCandidateRepository.findByMeetingRecordId(recordId);
        MeetingSummaryCandidate candidate = existing.stream()
                .filter(c -> c.getAiModel() == MeetingSummaryCandidate.AiModel.QWEN)
                .findFirst().orElse(null);

        if (candidate != null) {
            log.info("SummaryCandidate đã tồn tại, reset → PROCESSING cho record ID: {}", recordId);
            summaryPointRepository.deleteByCandidateId(candidate.getId());
            candidate.setStatus(MeetingSummaryCandidate.Status.PROCESSING);
            candidate.setRawSummary(null);
            candidate.setErrorMessage(null);
            candidate.setTranscriptId(transcript.getId());
            candidate.setTranscript(transcript);
            return summaryCandidateRepository.save(candidate);
        }

        try {
            MeetingSummaryCandidate c = new MeetingSummaryCandidate();
            c.setMeetingRecordId(recordId);
            c.setTranscriptId(transcript.getId());
            c.setMeetingRecord(record);
            c.setTranscript(transcript);
            c.setAiModel(MeetingSummaryCandidate.AiModel.QWEN);
            c.setStatus(MeetingSummaryCandidate.Status.PROCESSING);
            return summaryCandidateRepository.save(c);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                log.warn("SummaryCandidate đã được tạo bởi thread khác cho record ID: {}. Bỏ qua.", recordId);
                return null;
            }
            throw e;
        }
    }

    @Transactional
    protected void markFailed(MeetingTranscript transcript, MeetingSummaryCandidate candidate, String message) {
        log.error("Đánh dấu FAILED cho record ID {}: {}", transcript.getMeetingRecordId(), message);
        transcript.setStatus(MeetingTranscript.Status.FAILED);
        transcript.setErrorMessage(message);
        transcriptRepository.save(transcript);

        candidate.setStatus(MeetingSummaryCandidate.Status.FAILED);
        candidate.setErrorMessage(message);
        summaryCandidateRepository.save(candidate);
    }

    @Transactional
    protected void saveSummaryPoints(MeetingSummaryCandidate candidate, String rawSummary) {
        String[] lines = rawSummary.split("\n");
        int orderIndex = 0;
        int pointCount = 0;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            if (trimmed.matches("^\\d+\\..*") || trimmed.startsWith("-")) {
                String content = trimmed
                        .replaceFirst("^\\d+\\.\\s*", "")
                        .replaceFirst("^-\\s*", "")
                        .trim();

                if (content.length() >= 5) {
                    MeetingSummaryPoint point = new MeetingSummaryPoint();
                    point.setCandidateId(candidate.getId());
                    point.setCandidate(candidate);
                    point.setContent(content);
                    point.setOrderIndex(orderIndex++);
                    point.setIsSelected(false);
                    summaryPointRepository.save(point);
                    pointCount++;
                    log.debug("  → Điểm {}: {}",
                            orderIndex,
                            content.length() > 50 ? content.substring(0, 50) + "..." : content);
                }
            }
        }
        log.info("Lưu {} điểm tóm tắt thành công cho candidate ID: {}", pointCount, candidate.getId());
    }

    // =========================================================
    // Các method read/write khác (không thay đổi)
    // =========================================================

    @Transactional(readOnly = true)
    public TranscriptResponse getTranscript(Long recordId) {
        MeetingTranscript transcript = transcriptRepository.findByMeetingRecordId(recordId)
                .orElseThrow(() -> new MeetingRecordNotFoundException("Transcript không tồn tại"));
        return convertToTranscriptResponse(transcript);
    }

    @Transactional(readOnly = true)
    public List<SummaryResponse> getSummaries(Long recordId) {
        List<MeetingSummaryCandidate> candidates = summaryCandidateRepository.findByMeetingRecordId(recordId);
        return candidates.stream()
                .map(this::convertToSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SummaryPointDTO updatePointSelection(Long pointId, Boolean isSelected) {
        log.info("Cập nhật selection cho point ID: {}, isSelected: {}", pointId, isSelected);
        MeetingSummaryPoint point = summaryPointRepository.findById(pointId)
                .orElseThrow(() -> new RuntimeException("Point không tồn tại"));
        point.setIsSelected(isSelected);
        MeetingSummaryPoint updated = summaryPointRepository.save(point);
        log.info("Cập nhật selection thành công");
        return new SummaryPointDTO(updated.getId(), updated.getContent(),
                updated.getOrderIndex(), updated.getIsSelected());
    }

    @Transactional
    public SummaryPointDTO updatePointContent(Long pointId, String content) {
        log.info("Cập nhật nội dung cho point ID: {}", pointId);
        MeetingSummaryPoint point = summaryPointRepository.findById(pointId)
                .orElseThrow(() -> new RuntimeException("Point không tồn tại"));
        point.setContent(content);
        MeetingSummaryPoint updated = summaryPointRepository.save(point);
        log.info("Cập nhật nội dung thành công");
        return new SummaryPointDTO(updated.getId(), updated.getContent(),
                updated.getOrderIndex(), updated.getIsSelected());
    }

    @Transactional
    public FinalSummaryResponse saveFinalSummary(SaveFinalSummaryRequest request) {
        log.info("Lưu final summary cho record ID: {}", request.getMeetingRecordId());

        Long userId = securityUtil.getCurrentUserId();

        MeetingRecord record = meetingRecordRepository.findById(request.getMeetingRecordId())
                .orElseThrow(() -> new MeetingRecordNotFoundException("Record không tồn tại"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User không tồn tại"));

        List<MeetingSummaryPoint> points = summaryPointRepository.findAllById(request.getSelectedPointIds());
        if (points.size() != request.getSelectedPointIds().size()) {
            throw new RuntimeException("Một số point IDs không hợp lệ");
        }

        MeetingSummaryFinal finalSummary = summaryFinalRepository
                .findByMeetingRecordId(request.getMeetingRecordId())
                .orElse(null);

        if (finalSummary == null) {
            finalSummary = new MeetingSummaryFinal();
            finalSummary.setMeetingRecordId(request.getMeetingRecordId());
            finalSummary.setMeetingRecord(record);
            finalSummary.setCreatedBy(userId);
            finalSummary.setCreator(user);
        }

        finalSummary.setFinalContent(request.getFinalContent());
        finalSummary.setSelectedPointIds(request.getSelectedPointIds());

        MeetingSummaryFinal saved = summaryFinalRepository.save(finalSummary);
        log.info("Lưu final summary thành công với {} điểm được chọn", request.getSelectedPointIds().size());

        return new FinalSummaryResponse(
                saved.getId(),
                saved.getMeetingRecordId(),
                saved.getCreatedBy(),
                user.getFullname(),
                saved.getFinalContent(),
                saved.getSelectedPointIds(),
                saved.getCreatedAt(),
                saved.getUpdatedAt());
    }

    @Transactional(readOnly = true)
    public FinalSummaryResponse getFinalSummary(Long recordId) {
        MeetingSummaryFinal finalSummary = summaryFinalRepository.findByMeetingRecordId(recordId)
                .orElseThrow(() -> new FinalSummaryNotFoundException("Final summary chưa được tạo"));

        User user = userRepository.findById(finalSummary.getCreatedBy())
                .orElseThrow(() -> new UserNotFoundException("User không tồn tại"));

        return new FinalSummaryResponse(
                finalSummary.getId(),
                finalSummary.getMeetingRecordId(),
                finalSummary.getCreatedBy(),
                user.getFullname(),
                finalSummary.getFinalContent(),
                finalSummary.getSelectedPointIds(),
                finalSummary.getCreatedAt(),
                finalSummary.getUpdatedAt());
    }

    // =========================================================
    // Private converters
    // =========================================================

    private Object convertSegmentsToJson(List<TranscriptSegmentDTO> segments) {
        try {
            return objectMapper.writeValueAsString(segments);
        } catch (JsonProcessingException e) {
            log.error("Lỗi khi convert segments sang JSON", e);
            return "[]";
        }
    }

    private TranscriptResponse convertToTranscriptResponse(MeetingTranscript transcript) {
        List<TranscriptSegmentDTO> segments = new ArrayList<>();

        if (transcript.getSegments() != null) {
            try {
                String json = transcript.getSegments().toString();
                segments = objectMapper.readValue(json,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, TranscriptSegmentDTO.class));
            } catch (Exception e) {
                log.error("Lỗi khi parse segments JSON", e);
            }
        }

        return new TranscriptResponse(
                transcript.getId(),
                transcript.getMeetingRecordId(),
                segments,
                transcript.getFullText(),
                transcript.getStatus().name(),
                transcript.getErrorMessage(),
                transcript.getCreatedAt());
    }

    private SummaryResponse convertToSummaryResponse(MeetingSummaryCandidate candidate) {
        List<MeetingSummaryPoint> points = summaryPointRepository
                .findByCandidateIdOrderByOrderIndexAsc(candidate.getId());

        List<SummaryPointDTO> pointDTOs = points.stream()
                .map(p -> new SummaryPointDTO(p.getId(), p.getContent(), p.getOrderIndex(), p.getIsSelected()))
                .collect(Collectors.toList());

        return new SummaryResponse(
                candidate.getId(),
                candidate.getMeetingRecordId(),
                candidate.getAiModel().name(),
                candidate.getRawSummary(),
                pointDTOs,
                candidate.getStatus().name(),
                candidate.getErrorMessage(),
                candidate.getCreatedAt());
    }
}
