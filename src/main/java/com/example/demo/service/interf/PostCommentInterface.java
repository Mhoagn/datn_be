package com.example.demo.service.interf;

import com.example.demo.dto.PostDTO.CreatePostCommentRequest;
import com.example.demo.dto.PostDTO.PostCommentResponse;
import com.example.demo.dto.PostDTO.UpdatePostComment;

import java.util.List;

public interface PostCommentInterface {
    PostCommentResponse createComment(CreatePostCommentRequest request);
    List<PostCommentResponse> getCommentsByPostId(Long postId);
    PostCommentResponse deleteComment(Long commentId);
    PostCommentResponse updateComment(UpdatePostComment updatePostComment, Long commentId);
}
