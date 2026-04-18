package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"conversation", "sender"})
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, insertable = false, updatable = false)
    private Long conversationId;
    
    @Column(nullable = false, insertable = false, updatable = false)
    private Long senderId;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('text', 'image', 'video', 'file', 'audio') DEFAULT 'text'")
    private MessageType messageType = MessageType.TEXT;
    
    @Column(length = 1000)
    private String attachmentUrl;
    
    @Column(length = 255)
    private String attachmentPublicId;
    
    @Column(length = 255)
    private String attachmentName;
    
    @Column
    private Long attachmentSize;
    
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isRead = false;
    
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDeleted = false;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;
    
    @Column
    private LocalDateTime readAt;
    
    // ========== Relationships ==========
    
    // Message thuộc về 1 Conversation (N-1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversationId", nullable = false)
    @JsonIgnore
    private Conversation conversation;
    
    // Message được gửi bởi 1 User (N-1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senderId", nullable = false)
    @JsonIgnore
    private User sender;
    
    public enum MessageType {
        TEXT, IMAGE, VIDEO, FILE, AUDIO
    }
}
