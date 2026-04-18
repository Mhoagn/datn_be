package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Meeting_Summary_Points")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"candidate"})
public class MeetingSummaryPoint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, insertable = false, updatable = false)
    private Long candidateId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 0")
    private Integer orderIndex = 0;
    
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isSelected = false;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // ========== Relationships ==========
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidateId", nullable = false)
    @JsonIgnore
    private MeetingSummaryCandidate candidate;
}
