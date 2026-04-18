package com.example.demo.dto;

import com.example.demo.dto.PostDTO.PostResponse;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostWebSocketMessage {
    
    private String type;
    private Long groupId;
    private PostResponse data;
    private LocalDateTime timestamp;
    
    public enum MessageType {
        POST_CREATED,
        POST_UPDATED,
        POST_DELETED,
        POST_LIKED,
        POST_UNLIKED,
        COMMENT_CREATED,
        COMMENT_UPDATED,
        COMMENT_DELETED
    }
}
