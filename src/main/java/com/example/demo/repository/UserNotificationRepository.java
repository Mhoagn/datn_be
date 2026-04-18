package com.example.demo.repository;

import com.example.demo.dto.SliceResponse;
import com.example.demo.entity.UserNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    @Query("""
        SELECT un FROM UserNotification un
        JOIN FETCH un.notification n
        LEFT JOIN FETCH n.actor
        LEFT JOIN FETCH n.group
        WHERE un.userId = :userId
        ORDER BY n.createdAt DESC
    """)
    Slice<UserNotification> findAllByUserIdWithDetails(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(un) FROM UserNotification un WHERE un.userId = :userId AND un.isRead = false")
    int countUnreadByUserId(@Param("userId") Long userId);

    @Query("""
    SELECT un FROM UserNotification un
    JOIN FETCH un.notification n
    LEFT JOIN FETCH n.actor
    LEFT JOIN FETCH n.group
    WHERE un.userId = :userId
    AND un.isRead = false
    ORDER BY n.createdAt DESC
""")
    Slice<UserNotification> findUnreadByUserIdWithDetails(@Param("userId") Long userId, Pageable pageable);

    @Query("""
    SELECT un FROM UserNotification un
    WHERE un.userId = :userId
    AND un.notificationId = :notificationId
""")
    Optional<UserNotification> findByUserIdAndNotificationId(
            @Param("userId") Long userId,
            @Param("notificationId") Long notificationId);
}
