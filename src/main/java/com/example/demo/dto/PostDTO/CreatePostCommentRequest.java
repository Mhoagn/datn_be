package com.example.demo.dto.PostDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CreatePostCommentRequest {
    
    @NotNull(message = "Post ID không được để trống")
    private Long postId;
    
    @NotBlank(message = "Nội dung comment không được để trống")
    @Size(max = 5000, message = "Nội dung comment không được vượt quá 5000 ký tự")
    private String content;
    
    private Long parentCommentId;
}
