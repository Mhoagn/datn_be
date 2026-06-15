package com.example.demo.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    //@Column(nullable = false, columnDefinition = "ENUM('pending', 'approved', 'rejected') DEFAULT 'pending'")
    @Column(nullable = false, length = 20)
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
