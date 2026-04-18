package com.example.demo.service.interf;

import com.example.demo.dto.UserProfileDTO.UserStatusResponse;

public interface UserStatusInterface {
    UserStatusResponse getUserStatus(Long userId);
}
