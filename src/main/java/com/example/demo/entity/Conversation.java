package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Conversations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user1Id", "user2Id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user1", "user2", "messages"})
public class Conversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, insertable = false, updatable = false)
    private Long user1Id;
    
    @Column(nullable = false, insertable = false, updatable = false)
    private Long user2Id;
    
    @Column
    private LocalDateTime lastMessageAt;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // ========== Relationships ==========
    
    // Conversation giữa 2 Users (N-1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1Id", nullable = false)
    @JsonIgnore
    private User user1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2Id", nullable = false)
    @JsonIgnore
    private User user2;
    
    // Conversation có nhiều Messages (1-N)
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Message> messages = new ArrayList<>();
    
    /**
     * Helper method để đảm bảo user1Id < user2Id
     */
    @PrePersist
    @PreUpdate
    public void ensureUserOrder() {
        if (user1Id != null && user2Id != null && user1Id > user2Id) {
            Long temp = user1Id;
            user1Id = user2Id;
            user2Id = temp;
            
            // Swap user objects too
            User tempUser = user1;
            user1 = user2;
            user2 = tempUser;
        }
    }
}
