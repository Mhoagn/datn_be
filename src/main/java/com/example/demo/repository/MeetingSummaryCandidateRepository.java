package com.example.demo.repository;

import com.example.demo.entity.MeetingSummaryCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingSummaryCandidateRepository extends JpaRepository<MeetingSummaryCandidate, Long> {
    
    List<MeetingSummaryCandidate> findByMeetingRecordId(Long meetingRecordId);
    
    Optional<MeetingSummaryCandidate> findByMeetingRecordIdAndAiModel(
        Long meetingRecordId, 
        MeetingSummaryCandidate.AiModel aiModel
    );
    
    @Query("SELECT c FROM MeetingSummaryCandidate c WHERE c.meetingRecordId = :recordId AND c.status = 'COMPLETED'")
    List<MeetingSummaryCandidate> findCompletedByRecordId(Long recordId);
}
