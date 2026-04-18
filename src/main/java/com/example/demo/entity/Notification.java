package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Notifications", indexes = {
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_group_id", columnList = "group_id"),
        @Index(name = "idx_actor_id", columnList = "actor_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"actor", "group", "userNotifications"})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('NEW_POST', 'NEW_MEMBER', 'NEW_MEETING', 'NEW_SUMMARY', 'JOIN_REQUEST')")
    private Type type;

    @Column(name = "actor_id", insertable = false, updatable = false)
    private Long actorId;

    @Column(name = "group_id", insertable = false, updatable = false)
    private Long groupId;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ========== Relationships ==========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    @JsonIgnore
    private User actor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @JsonIgnore
    private Group group;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UserNotification> userNotifications = new ArrayList<>();

    public enum Type {
        NEW_POST,
        NEW_MEMBER,
        NEW_MEETING,
        NEW_SUMMARY,
        JOIN_REQUEST
    }
}
