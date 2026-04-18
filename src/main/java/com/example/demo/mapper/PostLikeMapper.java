package com.example.demo.mapper;

import com.example.demo.dto.PostDTO.PostLikeResponse;
import com.example.demo.entity.PostLike;
import org.springframework.stereotype.Component;

@Component
public class PostLikeMapper {
    
    public PostLikeResponse toResponse(PostLike postLike) {
        if (postLike == null) {
            return null;
        }
        
        PostLikeResponse.PostLikeResponseBuilder builder = PostLikeResponse.builder()
                .id(postLike.getId())
                .postId(postLike.getPostId())
                .userId(postLike.getUserId())
                .createdAt(postLike.getCreatedAt());

//        if (postLike.getUser() != null) {
//            builder.userName(postLike.getUser().getFullname())
//                   .userAvatar(postLike.getUser().getAvatarUrl());
//        }
        return builder.build();
    }
}
