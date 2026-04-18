package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Group_Join_Requests",
       uniqueConstraints = @UniqueConstraint(columnNames = {"groupId", "userId"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"group", "user", "reviewer"})
public class GroupJoinRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, insertable = false, updatable = false)
    private Long groupId;
    
    @Column(nullable = false, insertable = false, updatable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('pending', 'approved', 'rejected') DEFAULT 'pending'")
    private Status status = Status.PENDING;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column(insertable = false, updatable = false)
    private Long reviewedBy;
    
    @Column
    private LocalDateTime reviewedAt;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // ========== Relationships ==========
    
    // Request thuộc về 1 Group (N-1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupId", nullable = false)
    @JsonIgnore
    private Group group;
    
    // Request từ 1 User (N-1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    @JsonIgnore
    private User user;
    
    // Request được review bởi 1 User (N-1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewedBy")
    @JsonIgnore
    private User reviewer;
    
    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}
