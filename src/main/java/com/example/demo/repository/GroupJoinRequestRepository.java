package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.GroupJoinRequest;

import java.util.List;

@Repository
public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequest, Long> {
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    List<GroupJoinRequest> findByGroupIdAndStatus(Long groupId,GroupJoinRequest.Status status);
}
