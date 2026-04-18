package com.example.demo.repository;

import com.example.demo.entity.MeetingSummaryPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingSummaryPointRepository extends JpaRepository<MeetingSummaryPoint, Long> {
    
    List<MeetingSummaryPoint> findByCandidateIdOrderByOrderIndexAsc(Long candidateId);
    
    void deleteByCandidateId(Long candidateId);
}
