package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Meeting_Records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"meeting", "transcript", "summaryFinal"})
public class MeetingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 255)
    private String egressId;

    @Column
    private Long recordedBy;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(length = 500)
    private String s3Key;

    @Column(length = 255)
    private String s3Bucket;

    @Column(length = 1000)
    private String storageUrl;

    @Column
    private Long fileSizeBytes;

    @Column
    private Integer durationSeconds;

    @Column(length = 50)
    private String format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('PROCESSING','COMPLETED','FAILED') DEFAULT 'PROCESSING'")
    private Status status = Status.PROCESSING;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ========== Relationships ==========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    @JsonIgnore
    private Meeting meeting;

    @OneToOne(mappedBy = "meetingRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private MeetingTranscript transcript;
    
    @OneToOne(mappedBy = "meetingRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private MeetingSummaryFinal summaryFinal;

    // Nếu cần lấy meetingId mà không load cả Meeting object
    public Long getMeetingId() {
        return meeting != null ? meeting.getId() : null;
    }

    public enum Status {
        PROCESSING, COMPLETED, FAILED
    }
}