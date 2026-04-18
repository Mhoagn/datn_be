package com.example.demo.service.interf;

import com.example.demo.dto.UserProfileDTO.ProfileRequest;
import com.example.demo.dto.UserProfileDTO.ProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProfileInterface {
    
    ProfileResponse getMyProfile();
    
    ProfileResponse updateProfile(ProfileRequest request);
    
    ProfileResponse updateAvatar(MultipartFile file) throws IOException;
    
    void deleteAvatar();
    
    com.example.demo.dto.SliceResponse<ProfileResponse> searchUsersByEmail(String email, int page, int size);
}
