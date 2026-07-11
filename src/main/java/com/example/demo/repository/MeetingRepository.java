package com.example.demo.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Meeting;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    @Query("""
        SELECT m FROM Meeting m
        WHERE m.group.id = :groupId
        ORDER BY COALESCE(m.startedAt, m.scheduledStartAt) DESC
    """)
    Slice<Meeting> findByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    @Query("""
        SELECT m FROM Meeting m
        WHERE m.group.id = :groupId AND m.status = 'SCHEDULED'
        ORDER BY m.scheduledStartAt ASC
    """)
    Slice<Meeting> findScheduledByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    @Query("""
        SELECT m FROM Meeting m
        WHERE m.status = 'SCHEDULED'
          AND m.creatorReminderSent = false
          AND m.scheduledStartAt > :now
          AND m.scheduledStartAt <= :reminderDeadline
    """)
    List<Meeting> findMeetingsNeedingCreatorReminder(
            @Param("now") LocalDateTime now,
            @Param("reminderDeadline") LocalDateTime reminderDeadline
    );

    @Query("""
        SELECT m FROM Meeting m
        WHERE m.status = 'SCHEDULED'
          AND m.dueNotificationSent = false
          AND m.scheduledStartAt <= :now
    """)
    List<Meeting> findMeetingsNeedingDueNotification(@Param("now") LocalDateTime now);
}
