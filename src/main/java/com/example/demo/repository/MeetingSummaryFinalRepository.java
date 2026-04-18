package com.example.demo.repository;

import com.example.demo.entity.MeetingSummaryFinal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingSummaryFinalRepository extends JpaRepository<MeetingSummaryFinal, Long> {
    
    Optional<MeetingSummaryFinal> findByMeetingRecordId(Long meetingRecordId);
    
    boolean existsByMeetingRecordId(Long meetingRecordId);
}
