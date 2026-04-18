package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"createdGroups", "groupMemberships", "posts", "comments", "likes", "sentMessages"})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(nullable = false, length = 255)
    @JsonIgnore
    private String password;
    
    @Column(nullable = false, length = 255)
    private String fullname;
    
    @Column
    private LocalDate birthday;
    
    @Column(length = 500)
    private String avatarUrl;
    
    @Column(length = 255)
    private String cloudinaryAvatarId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('ONLINE', 'OFFLINE', 'AWAY') DEFAULT 'OFFLINE'")
    private OnlineStatus onlineStatus = OnlineStatus.OFFLINE;
    
    @Column
    private LocalDateTime lastSeenAt;
    
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // ========== Relationships ==========
    
    // User tạo nhiều Groups (1-N)
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Group> createdGroups = new ArrayList<>();
    
    // User có nhiều GroupMember (1-N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<GroupMember> groupMemberships = new ArrayList<>();
    
    // User tạo nhiều Posts (1-N)
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Post> posts = new ArrayList<>();
    
    // User tạo nhiều Comments (1-N)
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PostComment> comments = new ArrayList<>();
    
    // User có nhiều Likes (1-N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PostLike> likes = new ArrayList<>();
    
    // User tạo nhiều Meetings (1-N)
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Meeting> createdMeetings = new ArrayList<>();
    
    // User gửi nhiều Messages (1-N)
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Message> sentMessages = new ArrayList<>();
    
    public enum OnlineStatus {
        ONLINE, OFFLINE, AWAY
    }
}
