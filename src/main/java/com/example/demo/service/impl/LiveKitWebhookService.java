package com.example.demo.service.impl;

import com.example.demo.entity.MeetingRecord;
import com.example.demo.repository.MeetingRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveKitWebhookService {

    private final MeetingRecordRepository meetingRecordRepository;
    private final TranscriptService transcriptService;

    @Value("${livekit.s3.bucket}")
    private String s3Bucket;

    @Value("${livekit.s3.region}")
    private String s3Region;

    /**
     * Xử lý webhook từ LiveKit khi egress thay đổi trạng thái
     * 
     * Payload structure:
     * {
     *   "event": "egress_ended" | "egress_updated",
     *   "egressInfo": {
     *     "egressId": "EG_xxx",
     *     "status": "EGRESS_COMPLETE" | "EGRESS_FAILED" | "EGRESS_ABORTED",
     *     "startedAt": 1234567890,
     *     "endedAt": 1234567890,
     *     "file": {
     *       "filename": "meeting-1-xxx.mp4",
     *       "size": 12345678,
     *       "duration": 123
     *     }
     *   }
     * }
     */
    @Transactional
    public void handleEgressWebhook(Map<String, Object> payload) {
        String event = (String) payload.get("event");
        
        if (!"egress_ended".equals(event) && !"egress_updated".equals(event)) {
            log.debug("⏭️ Skipping non-egress event: {}", event);
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> egressInfo = (Map<String, Object>) payload.get("egressInfo");
        
        if (egressInfo == null) {
            log.warn("⚠️ No egressInfo in webhook payload");
            return;
        }

        String egressId = (String) egressInfo.get("egressId");
        String status = (String) egressInfo.get("status");

        log.info("🔄 Processing egress webhook - egressId: {}, status: {}", egressId, status);

        // Tìm MeetingRecord theo egressId
        MeetingRecord record = meetingRecordRepository.findByEgressId(egressId)
                .orElse(null);

        if (record == null) {
            log.warn("⚠️ MeetingRecord not found for egressId: {}", egressId);
            return;
        }

        // Xử lý theo status
        switch (status) {
            case "EGRESS_COMPLETE" -> handleEgressComplete(record, egressInfo);
            case "EGRESS_FAILED", "EGRESS_ABORTED" -> handleEgressFailed(record, status);
            default -> log.debug("⏭️ Skipping egress status: {}", status);
        }
    }

    /**
     * Xử lý khi egress hoàn thành thành công
     */
    private void handleEgressComplete(MeetingRecord record, Map<String, Object> egressInfo) {
        log.info("✅ Egress completed for record ID: {}", record.getId());

        // Lấy thông tin file
        @SuppressWarnings("unchecked")
        Map<String, Object> fileInfo = (Map<String, Object>) egressInfo.get("file");

        if (fileInfo != null) {
            // Lấy size và duration nếu có
            Object sizeObj = fileInfo.get("size");
            if (sizeObj instanceof Number) {
                record.setFileSizeBytes(((Number) sizeObj).longValue());
            }

            Object durationObj = fileInfo.get("duration");
            if (durationObj instanceof Number) {
                record.setDurationSeconds(((Number) durationObj).intValue());
            }
        }

        // Tính duration từ createdAt nếu chưa có
        if (record.getDurationSeconds() == null) {
            int durationSeconds = (int) Duration.between(
                record.getCreatedAt(), 
                LocalDateTime.now()
            ).getSeconds();
            record.setDurationSeconds(durationSeconds);
        }

        // Build storageUrl
        String storageUrl = "https://" + s3Bucket + ".s3." + s3Region
                + ".amazonaws.com/" + record.getFileName();

        // Cập nhật record
        record.setStatus(MeetingRecord.Status.COMPLETED);
        record.setStorageUrl(storageUrl);
        record.setS3Key(record.getFileName());
        record.setS3Bucket(s3Bucket);

        meetingRecordRepository.save(record);

        log.info("✅ Updated record ID {} - storageUrl: {}", record.getId(), storageUrl);

        // Gọi AI service để xử lý video (async)
        try {
            transcriptService.processRecordedVideo(record.getId());
            log.info("🤖 Triggered transcript processing for record ID: {}", record.getId());
        } catch (Exception e) {
            log.error("❌ Failed to trigger transcript processing: {}", e.getMessage(), e);
        }
    }

    /**
     * Xử lý khi egress thất bại hoặc bị hủy
     */
    private void handleEgressFailed(MeetingRecord record, String status) {
        log.warn("❌ Egress {} for record ID: {}", status, record.getId());

        // Cập nhật trạng thái thành FAILED
        record.setStatus(MeetingRecord.Status.FAILED);
        record.setStorageUrl(null); // Không có file

        meetingRecordRepository.save(record);

        log.info("❌ Updated record ID {} to FAILED status", record.getId());
    }
}
