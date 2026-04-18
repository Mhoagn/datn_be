package com.example.demo.dto.JoinGroupDTO;

import com.example.demo.entity.GroupJoinRequest;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequestResponse {
    private Long id;
    private Long groupId;
    private String groupName;
    private Long userId;
    private String userName;
    private String userAvatar;
    private GroupJoinRequest.Status status;
    private String message;
    private Long reviewedBy;
    private String reviewerName;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
