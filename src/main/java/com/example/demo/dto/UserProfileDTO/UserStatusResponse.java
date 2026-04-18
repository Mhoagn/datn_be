package com.example.demo.dto.UserProfileDTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserStatusResponse {
    private Long userId;
    private String status;
    private LocalDateTime lastActiveAt;
}
