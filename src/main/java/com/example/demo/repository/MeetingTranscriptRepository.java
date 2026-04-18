package com.example.demo.repository;

import com.example.demo.entity.MeetingTranscript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingTranscriptRepository extends JpaRepository<MeetingTranscript, Long> {
    
    Optional<MeetingTranscript> findByMeetingRecordId(Long meetingRecordId);
    
    @Query("SELECT t FROM MeetingTranscript t WHERE t.meetingRecordId = :recordId AND t.status = 'COMPLETED'")
    Optional<MeetingTranscript> findCompletedByRecordId(Long recordId);
}
