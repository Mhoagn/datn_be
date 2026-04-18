package com.example.demo.dto.UserProfileDTO;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileRequest {
    
    @Size(max = 255, message = "Họ tên không được vượt quá 255 ký tự")
    private String fullname;
    
    private LocalDate birthday;
    
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    private String newPassword;
    
    private String currentPassword;
}
