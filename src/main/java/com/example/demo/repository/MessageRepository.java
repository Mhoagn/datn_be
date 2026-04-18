package com.example.demo.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Message;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findTopByConversationIdOrderBySentAtDesc(Long conversationId);

    @Query("""
        SELECT COUNT(m) FROM Message m
        WHERE m.conversationId = :convId
          AND m.senderId != :userId
          AND m.isRead = false
          AND m.isDeleted = false
    """)
    int countUnreadMessages(
            @Param("convId") Long convId,
            @Param("userId") Long userId
    );

    @Query("""
        SELECT m FROM Message m
        WHERE m.conversationId = :conversationId
        ORDER BY m.sentAt DESC
    """)
    Slice<Message> findByConversationId(
            @Param("conversationId") Long conversationId,
            Pageable pageable
    );

    @Query("""
    SELECT m FROM Message m
    WHERE m.conversationId = :conversationId
      AND m.senderId != :currentUserId
      AND m.isRead = false
      AND m.isDeleted = false
""")
    List<Message> findUnreadMessages(
            @Param("conversationId") Long conversationId,
            @Param("currentUserId") Long currentUserId
    );
}
