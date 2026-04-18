package com.example.demo.service.impl;

import com.example.demo.dto.UserProfileDTO.UserStatusResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.interf.UserStatusInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserStatusService implements UserStatusInterface {

    private final UserRepository userRepository;

    @Override
    public UserStatusResponse getUserStatus(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));

        return UserStatusResponse.builder()
                .userId(user.getId())
                .status(user.getOnlineStatus().name())
                .lastActiveAt(user.getLastSeenAt())
                .build();
    }

    // Dùng cho WebSocket — đánh dấu ONLINE
    public void setOnline(Long userId) {
        userRepository.updateOnlineStatus(
                userId,
                User.OnlineStatus.ONLINE,
                LocalDateTime.now()
        );
    }

    // Dùng cho WebSocket — đánh dấu OFFLINE
    public void setOffline(Long userId) {
        userRepository.updateOnlineStatus(
                userId,
                User.OnlineStatus.OFFLINE,
                LocalDateTime.now()
        );
    }
}
