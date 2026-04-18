package com.example.demo.mapper;

import com.example.demo.dto.MeetingRecordDTO.RecordListResponse;
import com.example.demo.dto.MeetingRecordDTO.RecordResponse;
import com.example.demo.dto.MeetingRecordDTO.RecordStopResponse;
import com.example.demo.entity.MeetingRecord;
import com.example.demo.repository.MeetingSummaryFinalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MeetingRecordMapper {
    
    private final MeetingSummaryFinalRepository summaryFinalRepository;

    public RecordResponse toRecordResponse(MeetingRecord record) {
        return RecordResponse.builder()
                .id(record.getId())
                .meetingId(record.getMeetingId())
                .recordedBy(record.getRecordedBy())
                .egressId(record.getEgressId())
                .status(record.getStatus().name())
                .createdAt(record.getCreatedAt())
                .build();
    }

    public RecordStopResponse toRecordStopResponse(MeetingRecord record) {
        return RecordStopResponse.builder()
                .id(record.getId())
                .meetingId(record.getMeetingId())
                .recordedBy(record.getRecordedBy())
                .egressId(record.getEgressId())
                .status(record.getStatus().name())
                .storageUrl(record.getStorageUrl())
                .durationSeconds(record.getDurationSeconds())
                .createdAt(record.getCreatedAt())
                .build();
    }

    public RecordListResponse toRecordListResponse(MeetingRecord record) {
        // Check if final summary exists for this record
        boolean hasFinalSummary = summaryFinalRepository.findByMeetingRecordId(record.getId()).isPresent();
        
        return RecordListResponse.builder()
                .id(record.getId())
                .meetingId(record.getMeetingId())
                .meetingTitle(record.getMeeting() != null ? record.getMeeting().getMeetingTitle() : null)
                .recordedBy(record.getRecordedBy())
                .recordedByName(record.getRecordedBy() != null && record.getMeeting() != null 
                    ? getUserName(record) : null)
                .fileName(record.getFileName())
                .storageUrl(record.getStorageUrl())
                .fileSizeBytes(record.getFileSizeBytes())
                .durationSeconds(record.getDurationSeconds())
                .status(record.getStatus().name())
                .hasFinalSummary(hasFinalSummary)
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    private String getUserName(MeetingRecord record) {
        try {
            return record.getMeeting().getCreator().getFullname();
        } catch (Exception e) {
            return null;
        }
    }
}
