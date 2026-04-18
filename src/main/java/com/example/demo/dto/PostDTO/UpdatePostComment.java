package com.example.demo.dto.PostDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@Getter
@Builder
@Setter
@NoArgsConstructor
public class UpdatePostComment {
    @NotBlank(message = "Nội dung comment không được để trống")
    @Size(max = 5000, message = "Nội dung comment không được vượt quá 5000 ký tự")
    private String content;
}
