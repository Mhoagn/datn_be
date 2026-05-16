package com.example.demo.dto.GroupDTO;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberResponse {
    private Long id;
    private Long userId;
    private String fullname;
    private String email;
    private String avatarUrl;
    private String role;
    private LocalDateTime joinedAt;
    private Boolean isActive;
}
