package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Meeting_Messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"meeting", "author"})
public class MeetingMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, insertable = false, updatable = false)
    private Long meetingId;
    
    @Column(nullable = false, insertable = false, updatable = false)
    private Long authorId;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('TEXT', 'IMAGE', 'FILE') DEFAULT 'TEXT'")
    private MessageType messageType = MessageType.TEXT;
    
    @Column(length = 1000)
    private String attachmentUrl;
    
    @Column(length = 255)
    private String attachmentPublicId;
    
    @Column(length = 255)
    private String attachmentName;
    
    @Column
    private Long attachmentSize;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;
    
    // ========== Relationships ==========
    
    // Message thuộc về 1 Meeting (N-1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetingId", nullable = false)
    @JsonIgnore
    private Meeting meeting;
    
    // Message được gửi bởi 1 User (N-1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorId", nullable = false)
    @JsonIgnore
    private User author;
    
    public enum MessageType {
        TEXT, IMAGE, FILE
    }
}
