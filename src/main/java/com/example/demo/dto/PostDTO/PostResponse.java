package com.example.demo.dto.PostDTO;

import com.example.demo.dto.MediaInfo;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private Long id;
    private Long groupId;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private String content;
    private List<MediaInfo> mediaFiles;
    private String mediaType;
    private Integer likesCount;
    private Integer commentsCount;
    private Boolean isLikedByCurrentUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
