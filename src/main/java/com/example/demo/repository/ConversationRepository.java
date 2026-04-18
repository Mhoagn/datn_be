package com.example.demo.repository;

import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Conversation;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("""
        SELECT c FROM Conversation c
        WHERE c.user1Id = :smallerId AND c.user2Id = :largerId
    """)
    Optional<Conversation> findByTwoUsers(
            @Param("smallerId") Long smallerId,
            @Param("largerId") Long largerId
    );

    @Query("""
    SELECT c FROM Conversation c
    WHERE c.user1.id = :userId OR c.user2.id = :userId
    ORDER BY c.lastMessageAt DESC NULLS LAST
""")
    Slice<Conversation> findAllByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
    SELECT c FROM Conversation c
    WHERE c.user1.id = :userId OR c.user2.id = :userId
""")
    List<Conversation> findAllByUserIdNoPage(@Param("userId") Long userId);
}
