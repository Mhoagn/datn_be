package com.example.demo.dto.UserProfileDTO;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {
    
    private Long id;
    private String email;
    private String fullname;
    private LocalDate birthday;
    private String avatarUrl;
    private String onlineStatus;
    private LocalDateTime lastSeenAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
