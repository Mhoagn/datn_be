package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "Meeting_Summary_Candidates",
       uniqueConstraints = @UniqueConstraint(columnNames = {"meetingRecordId", "aiModel"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"meetingRecord", "transcript", "summaryPoints"})
public class MeetingSummaryCandidate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, insertable = false, updatable = false)
    private Long meetingRecordId;
    
    @Column(nullable = false, insertable = false, updatable = false)
    private Long transcriptId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('QWEN') DEFAULT 'QWEN'")
    private AiModel aiModel = AiModel.QWEN;
    
    @Column(columnDefinition = "TEXT")
    private String rawSummary;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('PENDING','PROCESSING','COMPLETED','FAILED') DEFAULT 'PENDING'")
    private Status status = Status.PENDING;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // ========== Relationships ==========
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetingRecordId", nullable = false)
    @JsonIgnore
    private MeetingRecord meetingRecord;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcriptId", nullable = false)
    @JsonIgnore
    private MeetingTranscript transcript;
    
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MeetingSummaryPoint> summaryPoints = new ArrayList<>();
    
    public enum AiModel {
        QWEN
    }
    
    public enum Status {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}
