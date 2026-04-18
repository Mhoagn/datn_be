package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Meeting_Participants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"meetingId", "userId", "sessionIndex"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"meeting", "user"})
public class MeetingParticipant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, insertable = false, updatable = false)
    private Long meetingId;
    
    @Column(nullable = false, insertable = false, updatable = false)
    private Long userId;

    @Column(name="livekit_identity", nullable = false)
    private String liveKitIdentity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.PARTICIPANT;
    
    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 1")
    private Integer sessionIndex = 1;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;
    
    @Column
    private LocalDateTime leftAt;
    
    @Column
    private Integer durationSeconds;
    
    // ========== Relationships ==========
    
    // Participant thuộc về 1 Meeting (N-1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetingId", nullable = false)
    @JsonIgnore
    private Meeting meeting;
    
    // Participant là 1 User (N-1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    @JsonIgnore
    private User user;

    public enum Role {
        HOST,PARTICIPANT
    }
}
