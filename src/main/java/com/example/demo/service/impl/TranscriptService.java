package com.example.demo.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.exception.UserNotFoundException;
import com.example.demo.exception.FinalSummaryNotFoundException;
import com.example.demo.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.SummaryDTO.FinalSummaryResponse;
import com.example.demo.dto.SummaryDTO.SaveFinalSummaryRequest;
import com.example.demo.dto.SummaryDTO.SummaryPointDTO;
import com.example.demo.dto.SummaryDTO.SummaryResponse;
import com.example.demo.dto.TranscriptDTO.AIServiceRequest;
import com.example.demo.dto.TranscriptDTO.AIServiceResponse;
import com.example.demo.dto.TranscriptDTO.TranscriptResponse;
import com.example.demo.dto.TranscriptDTO.TranscriptSegmentDTO;
import com.example.demo.entity.MeetingRecord;
import com.example.demo.entity.MeetingSummaryCandidate;
import com.example.demo.entity.MeetingSummaryFinal;
import com.example.demo.entity.MeetingSummaryPoint;
import com.example.demo.entity.MeetingTranscript;
import com.example.demo.entity.User;
import com.example.demo.exception.MeetingRecordNotFoundException;
import com.example.demo.repository.MeetingRecordRepository;
import com.example.demo.repository.MeetingSummaryCandidateRepository;
import com.example.demo.repository.MeetingSummaryFinalRepository;
import com.example.demo.repository.MeetingSummaryPointRepository;
import com.example.demo.repository.MeetingTranscriptRepository;
import com.example.demo.repository.UserRepository;
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
     * Xử lý video sau khi record hoàn tất
     * Gọi AI service để transcript và tóm tắt
     */
    @Async
    @Transactional
    public void processRecordedVideo(Long recordId) {
        log.info("Bắt đầu xử lý video cho record ID: {}", recordId);
        
        // 1. Lấy thông tin record
        MeetingRecord record = meetingRecordRepository.findById(recordId)
                .orElseThrow(() -> new MeetingRecordNotFoundException("Record không tồn tại"));
        
        // 1.1. Validate record có s3Key chưa (video đã upload lên S3 chưa)
        if (record.getS3Key() == null || record.getS3Key().trim().isEmpty()) {
            log.error("Record ID {} chưa có s3Key. Video có thể chưa upload lên S3.", recordId);
            throw new RuntimeException("Video chưa sẵn sàng. Vui lòng đợi quá trình upload hoàn tất.");
        }
        
        // 1.2. Validate record status
        if (record.getStatus() != MeetingRecord.Status.COMPLETED) {
            log.error("Record ID {} chưa COMPLETED. Status hiện tại: {}", recordId, record.getStatus());
            throw new RuntimeException("Bản ghi chưa hoàn tất. Vui lòng đợi quá trình ghi hình kết thúc.");
        }
        
        // 2. Kiểm tra xem đã có transcript chưa (tránh duplicate khi trigger nhiều lần)
        MeetingTranscript existingTranscript = transcriptRepository.findByMeetingRecordId(recordId).orElse(null);
        if (existingTranscript != null) {
            if (existingTranscript.getStatus() == MeetingTranscript.Status.COMPLETED) {
                log.info("Transcript đã tồn tại và đã COMPLETED cho record ID: {}. Bỏ qua.", recordId);
                return;
            } else if (existingTranscript.getStatus() == MeetingTranscript.Status.PROCESSING) {
                log.info("Transcript đang được xử lý cho record ID: {}. Bỏ qua.", recordId);
                return;
            } else if (existingTranscript.getStatus() == MeetingTranscript.Status.FAILED) {
                log.info("Transcript đã FAILED trước đó, reset để thử lại cho record ID: {}", recordId);
                // UPDATE thay vì DELETE để tránh race condition
                existingTranscript.setStatus(MeetingTranscript.Status.PROCESSING);
                existingTranscript.setFullText(null);
                existingTranscript.setSegments(null);
                existingTranscript.setErrorMessage(null);
                existingTranscript = transcriptRepository.save(existingTranscript);
            }
        }
        
        // 3. Tạo MeetingTranscript với status PROCESSING (handle race condition nếu chưa có)
        MeetingTranscript transcript;
        if (existingTranscript != null) {
            transcript = existingTranscript;
        } else {
            try {
                transcript = new MeetingTranscript();
                transcript.setMeetingRecordId(recordId);
                transcript.setMeetingRecord(record);
                transcript.setStatus(MeetingTranscript.Status.PROCESSING);
                transcript = transcriptRepository.save(transcript);
            } catch (Exception e) {
                // Nếu lỗi duplicate key, có nghĩa là thread khác đã tạo transcript
                if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                    log.warn("Transcript đã được tạo bởi thread khác cho record ID: {}. Bỏ qua.", recordId);
                    return;
                }
                throw e;
            }
        }
        
        // 4. Tạo/Update MeetingSummaryCandidate với status PROCESSING
        // Tìm candidate cũ trước (để UPDATE thay vì DELETE-then-INSERT)
        List<MeetingSummaryCandidate> existingCandidates = summaryCandidateRepository.findByMeetingRecordId(recordId);
        MeetingSummaryCandidate summaryCandidate = existingCandidates.stream()
            .filter(c -> c.getAiModel() == MeetingSummaryCandidate.AiModel.QWEN)
            .findFirst()
            .orElse(null);
        
        if (summaryCandidate != null) {
            log.info("SummaryCandidate đã tồn tại, UPDATE để retry cho record ID: {}", recordId);
            // Xóa points cũ
            summaryPointRepository.deleteByCandidateId(summaryCandidate.getId());
            // UPDATE candidate
            summaryCandidate.setStatus(MeetingSummaryCandidate.Status.PROCESSING);
            summaryCandidate.setRawSummary(null);
            summaryCandidate.setErrorMessage(null);
            summaryCandidate.setTranscriptId(transcript.getId());
            summaryCandidate.setTranscript(transcript);
            summaryCandidate = summaryCandidateRepository.save(summaryCandidate);
        } else {
            try {
                summaryCandidate = new MeetingSummaryCandidate();
                summaryCandidate.setMeetingRecordId(recordId);
                summaryCandidate.setTranscriptId(transcript.getId());
                summaryCandidate.setMeetingRecord(record);
                summaryCandidate.setTranscript(transcript);
                summaryCandidate.setAiModel(MeetingSummaryCandidate.AiModel.QWEN);
                summaryCandidate.setStatus(MeetingSummaryCandidate.Status.PROCESSING);
                summaryCandidate = summaryCandidateRepository.save(summaryCandidate);
            } catch (Exception e) {
                // Nếu lỗi duplicate key, có nghĩa là thread khác đã tạo candidate
                if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                    log.warn("SummaryCandidate đã được tạo bởi thread khác cho record ID: {}. Bỏ qua.", recordId);
                    return;
                }
                throw e;
            }
        }
        
        try {
            // 4. Gọi AI service
            log.info("Gọi AI service để xử lý video...");
            AIServiceRequest request = new AIServiceRequest(
                s3Bucket,
                record.getS3Key(),
                s3Region,
                awsAccessKey,
                awsSecretKey
            );
            
            AIServiceResponse response = aiServiceClient.processVideo(request);
            
            if (!"success".equals(response.getStatus())) {
                throw new RuntimeException("AI service trả về status không thành công");
            }
            
            log.info("AI service xử lý thành công");
            
            // 5. Lưu transcript
            transcript.setSegments(convertSegmentsToJson(response.getTranscriptSegments()));
            transcript.setFullText(response.getFullText());
            transcript.setStatus(MeetingTranscript.Status.COMPLETED);
            transcriptRepository.save(transcript);
            
            log.info("Lưu transcript thành công");
            
            // 6. Lưu summary
            summaryCandidate.setRawSummary(response.getSummary());
            summaryCandidate.setStatus(MeetingSummaryCandidate.Status.COMPLETED);
            summaryCandidateRepository.save(summaryCandidate);
            
            // 7. Parse summary thành các points (tách theo dòng)
            log.info("Parsing summary thành các điểm chính...");
            String[] lines = response.getSummary().split("\n");
            int orderIndex = 0;
            int pointCount = 0;
            
            for (String line : lines) {
                String trimmed = line.trim();
                
                // Chỉ xử lý các dòng có nội dung và bắt đầu bằng số hoặc dấu gạch đầu dòng
                if (trimmed.isEmpty()) {
                    continue;
                }
                
                // Pattern: "1. Nội dung" hoặc "- Nội dung"
                if (trimmed.matches("^\\d+\\..*") || trimmed.startsWith("-")) {
                    // Loại bỏ số thứ tự và dấu gạch đầu dòng
                    String content = trimmed
                        .replaceFirst("^\\d+\\.\\s*", "")
                        .replaceFirst("^-\\s*", "")
                        .trim();
                    
                    // Chỉ lưu nếu nội dung có ý nghĩa (ít nhất 5 ký tự)
                    if (content.length() >= 5) {
                        MeetingSummaryPoint point = new MeetingSummaryPoint();
                        point.setCandidateId(summaryCandidate.getId());
                        point.setCandidate(summaryCandidate);
                        point.setContent(content);
                        point.setOrderIndex(orderIndex++);
                        point.setIsSelected(false);
                        summaryPointRepository.save(point);
                        pointCount++;
                        
                        log.debug("  → Điểm {}: {}", orderIndex, 
                            content.length() > 50 ? content.substring(0, 50) + "..." : content);
                    }
                }
            }
            
            log.info("Lưu summary thành công với {} điểm chính", pointCount);
            
        } catch (Exception e) {
            log.error("Lỗi khi xử lý video: {}", e.getMessage(), e);
            
            // Cập nhật status thành FAILED
            transcript.setStatus(MeetingTranscript.Status.FAILED);
            transcript.setErrorMessage(e.getMessage());
            transcriptRepository.save(transcript);
            
            summaryCandidate.setStatus(MeetingSummaryCandidate.Status.FAILED);
            summaryCandidate.setErrorMessage(e.getMessage());
            summaryCandidateRepository.save(summaryCandidate);
        }
    }
    
    /**
     * Lấy transcript theo recordId
     */
    @Transactional(readOnly = true)
    public TranscriptResponse getTranscript(Long recordId) {
        MeetingTranscript transcript = transcriptRepository.findByMeetingRecordId(recordId)
                .orElseThrow(() -> new MeetingRecordNotFoundException("Transcript không tồn tại"));
        
        return convertToTranscriptResponse(transcript);
    }
    
    /**
     * Lấy summary theo recordId
     */
    @Transactional(readOnly = true)
    public List<SummaryResponse> getSummaries(Long recordId) {
        List<MeetingSummaryCandidate> candidates = summaryCandidateRepository.findByMeetingRecordId(recordId);
        
        return candidates.stream()
                .map(this::convertToSummaryResponse)
                .collect(Collectors.toList());
    }
    
    // Helper methods
    
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
            transcript.getCreatedAt()
        );
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
            candidate.getCreatedAt()
        );
    }
    
    /**
     * Cập nhật trạng thái isSelected của một điểm tóm tắt
     */
    @Transactional
    public SummaryPointDTO updatePointSelection(Long pointId, Boolean isSelected) {
        log.info("Cập nhật selection cho point ID: {}, isSelected: {}", pointId, isSelected);
        
        MeetingSummaryPoint point = summaryPointRepository.findById(pointId)
                .orElseThrow(() -> new RuntimeException("Point không tồn tại"));
        
        point.setIsSelected(isSelected);
        MeetingSummaryPoint updated = summaryPointRepository.save(point);
        
        log.info("Cập nhật selection thành công");
        
        return new SummaryPointDTO(
            updated.getId(),
            updated.getContent(),
            updated.getOrderIndex(),
            updated.getIsSelected()
        );
    }
    
    /**
     * Lưu final summary (người dùng chọn các điểm và tạo tóm tắt cuối cùng)
     */
    @Transactional
    public FinalSummaryResponse saveFinalSummary(SaveFinalSummaryRequest request) {
        log.info("Lưu final summary cho record ID: {}", request.getMeetingRecordId());

        Long userId = securityUtil.getCurrentUserId();
        
        // Kiểm tra record có tồn tại không
        MeetingRecord record = meetingRecordRepository.findById(request.getMeetingRecordId())
                .orElseThrow(() -> new MeetingRecordNotFoundException("Record không tồn tại"));
        
        // Kiểm tra user có tồn tại không
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User không tồn tại"));
        
        // Kiểm tra các point IDs có hợp lệ không
        List<MeetingSummaryPoint> points = summaryPointRepository.findAllById(request.getSelectedPointIds());
        if (points.size() != request.getSelectedPointIds().size()) {
            throw new RuntimeException("Một số point IDs không hợp lệ");
        }
        
        // Kiểm tra xem đã có final summary chưa
        MeetingSummaryFinal finalSummary = summaryFinalRepository
                .findByMeetingRecordId(request.getMeetingRecordId())
                .orElse(null);
        
        if (finalSummary == null) {
            // Tạo mới
            finalSummary = new MeetingSummaryFinal();
            finalSummary.setMeetingRecordId(request.getMeetingRecordId());
            finalSummary.setMeetingRecord(record);
            finalSummary.setCreatedBy(userId);
            finalSummary.setCreator(user);
        }
        
        // Cập nhật nội dung
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
            saved.getUpdatedAt()
        );
    }
    
    /**
     * Lấy final summary theo recordId
     */
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
            finalSummary.getUpdatedAt()
        );
    }
}
