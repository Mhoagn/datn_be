package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Meeting_Summary_Final")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"meetingRecord", "creator"})
public class MeetingSummaryFinal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, insertable = false, updatable = false)
    private Long meetingRecordId;
    
    @Column(nullable = false, insertable = false, updatable = false)
    private Long createdBy;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String finalContent;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "JSON")
    private List<Long> selectedPointIds;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // ========== Relationships ==========
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetingRecordId", nullable = false, unique = true)
    @JsonIgnore
    private MeetingRecord meetingRecord;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy", nullable = false)
    @JsonIgnore
    private User creator;
}
