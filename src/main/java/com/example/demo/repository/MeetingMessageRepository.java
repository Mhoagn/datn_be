package com.example.demo.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.MeetingMessage;

@Repository
public interface MeetingMessageRepository extends JpaRepository<MeetingMessage, Long> {
    @Query("""
        SELECT mm FROM MeetingMessage mm
        JOIN FETCH mm.author u
        WHERE mm.meeting.id = :meetingId
        ORDER BY mm.sentAt ASC
    """)
    Slice<MeetingMessage> findByMeetingId(
            @Param("meetingId") Long meetingId,
            Pageable pageable
    );
}
