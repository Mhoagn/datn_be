package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Meetings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"group", "creator", "participants", "messages", "records"})
public class Meeting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, insertable = false, updatable = false)
    private Long groupId;
    
    @Column(length = 255)
    private String meetingTitle;
    
    @Column(nullable = false, insertable = false, updatable = false)
    private Long createdBy;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;
    
    @Column
    private LocalDateTime endedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('ONGOING', 'END') DEFAULT 'ONGOING'")
    private Status status = Status.ONGOING;
    
    @Column(name = "livekit_room_name", nullable = false)
    private String liveKitRoomName;
    
    // ========== Relationships ==========
    
    // Meeting thuộc về 1 Group (N-1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupId", nullable = false)
    @JsonIgnore
    private Group group;
    
    // Meeting được tạo bởi 1 User (N-1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy", nullable = false)
    @JsonIgnore
    private User creator;
    
    // Meeting có nhiều Participants (1-N)
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MeetingParticipant> participants = new ArrayList<>();
    
    // Meeting có nhiều Messages (1-N)
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MeetingMessage> messages = new ArrayList<>();
    
    // Meeting có nhiều Records (1-N)
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MeetingRecord> records = new ArrayList<>();
    
    public enum Status {
        ONGOING, END
    }

}
