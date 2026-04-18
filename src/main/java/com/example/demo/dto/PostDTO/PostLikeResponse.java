package com.example.demo.dto.PostDTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostLikeResponse {
    private Long id;
    private Long postId;
    private Long userId;
    private LocalDateTime createdAt;
}
