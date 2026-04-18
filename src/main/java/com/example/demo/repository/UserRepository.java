package com.example.demo.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);

    Slice<User> findByEmailContainingIgnoreCaseAndIdNot(String email, Long id, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.onlineStatus = :status, u.lastSeenAt = :lastSeenAt WHERE u.id = :userId")
    void updateOnlineStatus(
            @Param("userId") Long userId,
            @Param("status") User.OnlineStatus status,
            @Param("lastSeenAt") LocalDateTime lastSeenAt
    );
}
