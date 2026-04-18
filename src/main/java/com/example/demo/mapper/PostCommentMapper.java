package com.example.demo.mapper;

import com.example.demo.dto.PostDTO.PostCommentResponse;
import com.example.demo.entity.PostComment;
import com.example.demo.repository.PostCommentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class PostCommentMapper {
    
    private final PostCommentRepository postCommentRepository;
    
    public PostCommentResponse toResponse(PostComment comment, boolean includeReplies) {
        if (comment == null) {
            return null;
        }
        
        PostCommentResponse response = new PostCommentResponse();
        response.setId(comment.getId());
        response.setPostId(comment.getPostId());
        response.setAuthorId(comment.getAuthorId());
        response.setContent(comment.getContent());
        response.setParentCommentId(comment.getParentCommentId());
        response.setIsDeleted(comment.getIsDeleted());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        
        if (comment.getAuthor() != null) {
            response.setAuthorName(comment.getAuthor().getFullname());
            response.setAuthorAvatar(comment.getAuthor().getAvatarUrl());
        }
        
        Long repliesCount = postCommentRepository.countByParentCommentIdAndIsDeletedFalse(comment.getId());
        response.setRepliesCount(repliesCount.intValue());
        
        if (includeReplies && repliesCount > 0) {
            List<PostComment> replies = postCommentRepository
                    .findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(comment.getId());
            List<PostCommentResponse> replyResponses = new ArrayList<>();
            for (PostComment reply : replies) {
                replyResponses.add(toResponse(reply, false));
            }
            response.setReplies(replyResponses);
        }
        
        return response;
    }
    
    public List<PostCommentResponse> toResponseList(List<PostComment> comments, boolean includeReplies) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<PostCommentResponse> responses = new ArrayList<>();
        for (PostComment comment : comments) {
            responses.add(toResponse(comment, includeReplies));
        }
        
        return responses;
    }
}
