package com.example.demo.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.dto.MediaInfo;
import com.example.demo.dto.PostDTO.PostResponse;
import com.example.demo.entity.Post;

@Component
public class PostMapper {
    /**
     * Convert Post entity to PostResponse DTO
     *
     * @param post Post entity
     * @param currentUserId ID của user hiện tại (để check liked status)
     * @return PostResponse DTO
     */
    public PostResponse toResponse(Post post, Long currentUserId) {
        if (post == null) {
            return null;
        }

        PostResponse response = new PostResponse();

        // Basic info
        response.setId(post.getId());
        response.setGroupId(post.getGroupId());
        response.setAuthorId(post.getAuthorId());
        response.setContent(post.getContent());
        response.setMediaType(post.getMediaType() != null ? post.getMediaType().name() : "NONE");
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());

        // Author info (null-safe)
        if (post.getAuthor() != null) {
            response.setAuthorName(post.getAuthor().getFullname());
            response.setAuthorAvatar(post.getAuthor().getAvatarUrl());
        }

        // Media files info
        if (post.getMediaMetadata() != null && !post.getMediaMetadata().isEmpty()) {
            List<MediaInfo> mediaFiles = new ArrayList<>();

            for (java.util.Map<String, Object> metadata : post.getMediaMetadata()) {
                MediaInfo info = new MediaInfo();
                info.setUrl((String) metadata.get("url"));
                info.setPublicId((String) metadata.get("publicId"));
                info.setOriginalName((String) metadata.get("originalName"));
                
                Object sizeObj = metadata.get("size");
                if (sizeObj instanceof Number) {
                    info.setSize(((Number) sizeObj).longValue());
                }
                
                info.setMimeType((String) metadata.get("mimeType"));
                
                mediaFiles.add(info);
            }

            response.setMediaFiles(mediaFiles);
        } else if (post.getMediaUrls() != null && !post.getMediaUrls().isEmpty()) {
            List<MediaInfo> mediaFiles = new ArrayList<>();

            for (int i = 0; i < post.getMediaUrls().size(); i++) {
                MediaInfo info = new MediaInfo();
                info.setUrl(post.getMediaUrls().get(i));

                if (post.getMediaPublicIds() != null &&
                        i < post.getMediaPublicIds().size()) {
                    info.setPublicId(post.getMediaPublicIds().get(i));
                }

                mediaFiles.add(info);
            }

            response.setMediaFiles(mediaFiles);
        }

        // Statistics
        response.setLikesCount(post.getLikes() != null ? post.getLikes().size() : 0);
        
        int totalCommentsCount = 0;
        if (post.getComments() != null) {
            totalCommentsCount = (int) post.getComments().stream()
                    .filter(comment -> !comment.getIsDeleted())
                    .count();
        }
        response.setCommentsCount(totalCommentsCount);

        // Check if current user liked this post
        boolean isLiked = false;
        if (currentUserId != null && post.getLikes() != null) {
            isLiked = post.getLikes().stream()
                    .anyMatch(like -> like.getUserId().equals(currentUserId));
        }
        response.setIsLikedByCurrentUser(isLiked);

        return response;
    }

    /**
     * Convert list of Post entities to list of PostResponse DTOs
     *
     * @param posts List of Post entities
     * @param currentUserId ID của user hiện tại
     * @return List of PostResponse DTOs
     */
    public List<PostResponse> toResponseList(List<Post> posts, Long currentUserId) {
        if (posts == null || posts.isEmpty()) {
            return new ArrayList<>();
        }

        List<PostResponse> responses = new ArrayList<>();
        for (Post post : posts) {
            responses.add(toResponse(post, currentUserId));
        }

        return responses;
    }

    /**
     * Convert Post entity to PostResponse without checking liked status
     * (Useful for public APIs or when currentUserId is not available)
     *
     * @param post Post entity
     * @return PostResponse DTO
     */
    public PostResponse toResponseWithoutUser(Post post) {
        return toResponse(post, null);
    }
}
