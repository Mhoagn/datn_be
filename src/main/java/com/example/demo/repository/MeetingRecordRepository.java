package com.example.demo.repository;

import java.util.Optional;

import java.util.Optional;

import com.example.demo.entity.MeetingRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingRecordRepository extends JpaRepository<MeetingRecord, Long> {
    @Query("""
        SELECT COUNT(r) > 0 FROM MeetingRecord r
        WHERE r.meeting.id = :meetingId
          AND r.status = 'PROCESSING'
    """)
    boolean existsActiveRecordByMeetingId(@Param("meetingId") Long meetingId);

    @Query("""
        SELECT r FROM MeetingRecord r
        WHERE r.meeting.id = :meetingId
          AND r.status = 'PROCESSING'
    """)
    Optional<MeetingRecord> findActiveRecordByMeetingId(@Param("meetingId") Long meetingId);

    /**
     * Tìm MeetingRecord theo egressId (để xử lý webhook từ LiveKit)
     */
    Optional<MeetingRecord> findByEgressId(String egressId);

    /**
     * Lấy danh sách records của tất cả meetings trong group
     * Sắp xếp theo createdAt DESC (mới nhất trước)
     */
    @Query("""
        SELECT r FROM MeetingRecord r
        WHERE r.meeting.group.id = :groupId
        AND r.status = 'COMPLETED'
        ORDER BY r.createdAt DESC
    """)
    Slice<MeetingRecord> findByGroupId(@Param("groupId") Long groupId, Pageable pageable);
}
