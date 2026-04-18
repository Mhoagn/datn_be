package com.example.demo.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.MeetingParticipant;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {

    @Query("""
        SELECT COUNT(mp) > 0 FROM MeetingParticipant mp
        JOIN mp.meeting m
        WHERE mp.user.id = :userId
          AND m.status = 'ONGOING'
          AND mp.leftAt IS NULL
    """)
    boolean existsActiveSessionByUserId(@Param("userId") Long userId);

    // Kiểm tra user đang active trong chính meeting này (2 thiết bị)
    @Query("""
        SELECT COUNT(mp) > 0 FROM MeetingParticipant mp
        WHERE mp.user.id = :userId
          AND mp.meeting.id = :meetingId
          AND mp.leftAt IS NULL
    """)
    boolean existsActiveSessionInMeeting(
            @Param("userId") Long userId,
            @Param("meetingId") Long meetingId
    );

    // Lấy sessionIndex lớn nhất của user trong meeting (để tăng khi re-join)
    @Query("""
        SELECT COALESCE(MAX(mp.sessionIndex), 0) FROM MeetingParticipant mp
        WHERE mp.user.id = :userId
          AND mp.meeting.id = :meetingId
    """)
    int findMaxSessionIndex(
            @Param("userId") Long userId,
            @Param("meetingId") Long meetingId
    );

    @Query("""
        SELECT mp FROM MeetingParticipant mp
        WHERE mp.user.id = :userId
          AND mp.meeting.id = :meetingId
          AND mp.leftAt IS NULL
    """)
    Optional<MeetingParticipant> findActiveSession(
            @Param("userId") Long userId,
            @Param("meetingId") Long meetingId
    );

    @Query("""
        SELECT mp FROM MeetingParticipant mp
        WHERE mp.meeting.id = :meetingId
          AND mp.leftAt IS NULL
    """)
    List<MeetingParticipant> findAllActiveByMeetingId(@Param("meetingId") Long meetingId);

    // Lấy tất cả participant còn active trong meeting kèm thông tin user
    @Query("""
        SELECT mp FROM MeetingParticipant mp
        JOIN FETCH mp.user u
        WHERE mp.meeting.id = :meetingId
          AND mp.leftAt IS NULL
        ORDER BY mp.joinedAt ASC
    """)
    Slice<MeetingParticipant> findActiveParticipants(
            @Param("meetingId") Long meetingId,
            Pageable pageable
    );
 
}
