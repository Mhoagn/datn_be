package com.example.demo.dto.JoinGroupDTO;

import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
public class JoinGroupRequest {
    private String joinCode;
    @Size(max = 500, message = "Lời nhắn không quá 500 ký tự")
    private String message;
}
