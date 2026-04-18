package com.example.demo.dto.PostDTO;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PostLikeRequest {
    @NotNull(message = "Post ID không được để trống")
    private Long postId;
}
