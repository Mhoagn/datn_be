package com.example.demo.controller;

import com.example.demo.dto.PostDTO.CreatePostCommentRequest;
import com.example.demo.dto.PostDTO.PostCommentResponse;
import com.example.demo.dto.PostDTO.UpdatePostComment;
import com.example.demo.service.interf.PostCommentInterface;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post-comment")
@AllArgsConstructor
public class PostCommentController {
    
    private final PostCommentInterface postCommentService;
    
    @PostMapping
    public ResponseEntity<PostCommentResponse> createComment(
            @RequestBody @Valid CreatePostCommentRequest request
    ) {
        PostCommentResponse response = postCommentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<PostCommentResponse>> getCommentsByPostId(
            @PathVariable Long postId
    ) {
        List<PostCommentResponse> comments = postCommentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<PostCommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestBody @Valid UpdatePostComment updatePostComment
    ) {
        PostCommentResponse postCommentResponse = postCommentService.updateComment(updatePostComment, commentId);
        return ResponseEntity.ok(postCommentResponse);
    }
    
    @DeleteMapping("/{commentId}")
    public ResponseEntity<PostCommentResponse> deleteComment(
            @PathVariable Long commentId
    ) {
        PostCommentResponse response = postCommentService.deleteComment(commentId);
        return ResponseEntity.ok(response);
    }
}
