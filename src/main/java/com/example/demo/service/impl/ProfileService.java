package com.example.demo.service.impl;

import com.example.demo.dto.SliceResponse;
import com.example.demo.dto.UserProfileDTO.ProfileRequest;
import com.example.demo.dto.UserProfileDTO.ProfileResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.interf.ProfileInterface;
import com.example.demo.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@AllArgsConstructor
public class ProfileService implements ProfileInterface {
    
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final CloudinaryService cloudinaryService;
    private final BCryptPasswordEncoder encoder;
    
    @Override
    public ProfileResponse getMyProfile() {
        User user = securityUtil.getCurrentUser();
        return buildProfileResponse(user);
    }
    
    @Override
    @Transactional
    public ProfileResponse updateProfile(ProfileRequest request) {
        User user = securityUtil.getCurrentUser();
        
        if (request.getFullname() != null && !request.getFullname().isBlank()) {
            user.setFullname(request.getFullname());
        }
        
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new InvalidCredentialsException("Vui lòng nhập mật khẩu hiện tại");
            }
            
            if (!encoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new InvalidCredentialsException("Mật khẩu hiện tại không đúng");
            }
            
            user.setPassword(encoder.encode(request.getNewPassword()));
        }
        
        user = userRepository.save(user);
        
        return buildProfileResponse(user);
    }
    
    @Override
    @Transactional
    public ProfileResponse updateAvatar(MultipartFile file) throws IOException {
        User user = securityUtil.getCurrentUser();
        
        if (user.getCloudinaryAvatarId() != null) {
            cloudinaryService.deleteImage(user.getCloudinaryAvatarId());
        }
        
        Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, "avatars");
        
        String avatarUrl = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");
        
        user.setAvatarUrl(avatarUrl);
        user.setCloudinaryAvatarId(publicId);
        
        user = userRepository.save(user);
        
        return buildProfileResponse(user);
    }
    
    @Override
    @Transactional
    public void deleteAvatar() {
        User user = securityUtil.getCurrentUser();
        
        if (user.getCloudinaryAvatarId() != null) {
            cloudinaryService.deleteImage(user.getCloudinaryAvatarId());
        }
        
        user.setAvatarUrl(null);
        user.setCloudinaryAvatarId(null);
        
        userRepository.save(user);
    }
    
    @Override
    public SliceResponse<ProfileResponse> searchUsersByEmail(String email, int page, int size) {
        User currentUser = securityUtil.getCurrentUser();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        
        org.springframework.data.domain.Slice<User> userSlice = userRepository
                .findByEmailContainingIgnoreCaseAndIdNot(email, currentUser.getId(), pageable);
        
        java.util.List<ProfileResponse> content = userSlice.getContent().stream()
                .map(this::buildProfileResponse)
                .collect(java.util.stream.Collectors.toList());
        
        return com.example.demo.dto.SliceResponse.<ProfileResponse>builder()
                .content(content)
                .page(userSlice.getNumber())
                .size(userSlice.getSize())
                .hasNext(userSlice.hasNext())
                .build();
    }

    private ProfileResponse buildProfileResponse(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .birthday(user.getBirthday())
                .avatarUrl(user.getAvatarUrl())
                .onlineStatus(user.getOnlineStatus().name())
                .lastSeenAt(user.getLastSeenAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
