package com.example.demo.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Meeting;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    @Query("""
        SELECT m FROM Meeting m
        WHERE m.group.id = :groupId
        ORDER BY m.startedAt DESC
    """)
    Slice<Meeting> findByGroupId(@Param("groupId") Long groupId, Pageable pageable);
}
