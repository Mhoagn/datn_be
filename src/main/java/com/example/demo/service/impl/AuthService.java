package com.example.demo.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.example.demo.dto.AuthDTO.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.exception.InvalidTokenException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import com.example.demo.service.interf.AuthInterface;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthService implements AuthInterface {
    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepo;
    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;

    private static final long OTP_EXPIRE_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;


    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email đã được sử dụng");
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setFullname(request.getFullname());
        user.setBirthday(request.getBirthday());
        user.setOnlineStatus(User.OnlineStatus.ONLINE);
        user.setIsActive(true);
        
        user = userRepo.save(user);
        
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.createRefreshToken(user);
        
        saveRefreshToken(user.getId(), refreshToken);
        
        return buildAuthResponse(user, accessToken, refreshToken);
    }
    
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email hoặc mật khẩu không đúng"));
        
        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Email hoặc mật khẩu không đúng");
        }
        
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidCredentialsException("Tài khoản đã bị vô hiệu hóa");
        }
        
        refreshTokenRepo.revokeAllByUserId(user.getId());
        
        user.setOnlineStatus(User.OnlineStatus.ONLINE);
        user.setLastSeenAt(LocalDateTime.now());
        user = userRepo.save(user);
        
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.createRefreshToken(user);
        
        saveRefreshToken(user.getId(), refreshToken);
        
        return buildAuthResponse(user, accessToken, refreshToken);
    }
    
    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken tokenEntity = refreshTokenRepo.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token không hợp lệ"));
        
        if (!tokenEntity.isValid()) {
            throw new InvalidTokenException("Refresh token đã hết hạn hoặc bị thu hồi");
        }
        
        try {
            Claims claims = jwtService.parseRefresh(refreshToken).getPayload();
            Long userId = Long.valueOf(claims.getSubject());
            
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));
            
            if (!Boolean.TRUE.equals(user.getIsActive())) {
                throw new InvalidCredentialsException("Tài khoản đã bị vô hiệu hóa");
            }
            
            String newAccessToken = jwtService.createAccessToken(user);
            
            return buildAuthResponse(user, newAccessToken, refreshToken);
            
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Refresh token không hợp lệ");
        }
    }
    
    @Override
    @Transactional
    public void logout(String refreshToken) {
        RefreshToken tokenEntity = refreshTokenRepo.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token không hợp lệ"));
        
        tokenEntity.setIsRevoked(true);
        refreshTokenRepo.save(tokenEntity);
        
        User user = tokenEntity.getUser();
        user.setOnlineStatus(User.OnlineStatus.OFFLINE);
        user.setLastSeenAt(LocalDateTime.now());
        userRepo.save(user);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();

        // Kiểm tra email tồn tại không
        if (!userRepo.existsByEmail(email)) {
            // Vẫn trả về bình thường, tránh lộ email có tồn tại không
            return;
        }

        // Tạo OTP 6 số
        String otp = String.valueOf(new SecureRandom().nextInt(900000) + 100000);

        // Lưu hash vào Redis
        String otpKey     = "otp:" + email;
        String attemptKey = "otp:attempts:" + email;

        redisTemplate.opsForValue()
                .set(otpKey, hashOtp(otp), OTP_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue()
                .set(attemptKey, "0", OTP_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // Gửi OTP qua email
        emailService.sendOtpEmail(email, otp);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email     = request.getEmail();
        String otpKey     = "otp:" + email;
        String attemptKey = "otp:attempts:" + email;

        // 1. Kiểm tra OTP còn tồn tại không (hết hạn Redis tự xóa)
        String savedHash = redisTemplate.opsForValue().get(otpKey);
        if (savedHash == null) {
            throw new RuntimeException("OTP đã hết hạn hoặc không tồn tại");
        }

        // 2. Kiểm tra số lần thử sai
        int attempts = Integer.parseInt(
                Optional.ofNullable(redisTemplate.opsForValue().get(attemptKey)).orElse("0")
        );
        if (attempts >= MAX_ATTEMPTS) {
            throw new RuntimeException("Quá nhiều lần thử sai, vui lòng yêu cầu OTP mới");
        }

        // 3. So sánh hash
        if (!savedHash.equals(hashOtp(request.getOtp()))) {
            redisTemplate.opsForValue().increment(attemptKey);
            throw new RuntimeException("OTP không đúng");
        }

        // 4. Cập nhật mật khẩu mới
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));
        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepo.save(user);

        // 5. Xóa OTP khỏi Redis
        redisTemplate.delete(otpKey);
        redisTemplate.delete(attemptKey);
    }

    private void saveRefreshToken(Long userId, String token) {
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                jwtService.refreshExpiresAt(), 
                ZoneId.systemDefault()
        );
        
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));
        
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(expiresAt)
                .isRevoked(false)
                .build();
        
        refreshTokenRepo.save(refreshToken);
    }
    
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .birthday(user.getBirthday())
                .avatarUrl(user.getAvatarUrl())
                .onlineStatus(user.getOnlineStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userInfo)
                .build();
    }

    private String hashOtp(String otp) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(otp.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
