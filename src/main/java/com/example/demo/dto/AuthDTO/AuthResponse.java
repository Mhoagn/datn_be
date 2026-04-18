package com.example.demo.dto.AuthDTO;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    
    private String accessToken;
    private String refreshToken;
    private UserInfo user;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String email;
        private String fullname;
        private LocalDate birthday;
        private String avatarUrl;
        private String onlineStatus;
        private LocalDateTime createdAt;
    }
}
