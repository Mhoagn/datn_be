package com.example.demo.controller;

import com.example.demo.dto.MessageResponse;
import com.example.demo.dto.SliceResponse;
import com.example.demo.dto.UserProfileDTO.ProfileRequest;
import com.example.demo.dto.UserProfileDTO.ProfileResponse;
import com.example.demo.service.interf.ProfileInterface;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/profile")
@AllArgsConstructor
public class ProfileController {
    
    private final ProfileInterface profileService;
    
    @GetMapping
    public ResponseEntity<ProfileResponse> getMyProfile() {
        ProfileResponse response = profileService.getMyProfile();
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody ProfileRequest request) {
        ProfileResponse response = profileService.updateProfile(request);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> updateAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        ProfileResponse response = profileService.updateAvatar(file);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/avatar")
    public ResponseEntity<MessageResponse> deleteAvatar() {
        profileService.deleteAvatar();
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Xóa avatar thành công")
                .build());
    }

    @GetMapping("/search")
    public ResponseEntity<SliceResponse<ProfileResponse>> searchUsers(
            @RequestParam String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(profileService.searchUsersByEmail(email, page, size));
    }
}
