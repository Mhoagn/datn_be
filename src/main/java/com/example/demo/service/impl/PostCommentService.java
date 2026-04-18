package com.example.demo.service.impl;

import com.example.demo.dto.PostDTO.CreatePostCommentRequest;
import com.example.demo.dto.PostDTO.PostCommentResponse;
import com.example.demo.dto.PostDTO.UpdatePostComment;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostComment;
import com.example.demo.entity.User;
import com.example.demo.exception.*;
import com.example.demo.mapper.PostCommentMapper;
import com.example.demo.repository.PostCommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.interf.PostCommentInterface;
import com.example.demo.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class PostCommentService implements PostCommentInterface {
    
    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostCommentMapper postCommentMapper;
    private final SecurityUtil securityUtil;
    private final PostWebSocketService postWebSocketService;
    
    @Override
    @Transactional
    public PostCommentResponse createComment(CreatePostCommentRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new InvalidRequestException("Nội dung comment không được để trống");
        }
        
        if (request.getContent().length() > 5000) {
            throw new InvalidRequestException("Nội dung comment không được vượt quá 5000 ký tự");
        }
        
        Long currentUserId = securityUtil.getCurrentUserId();
        
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new PostNotFoundException("Bài viết không tồn tại"));
        
        if (post.getIsDeleted()) {
            throw new PostIsDeletedException("Không thể comment vào bài viết đã bị xóa");
        }
        
        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));
        
        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setPostId(request.getPostId());
        comment.setAuthorId(currentUserId);
        comment.setContent(request.getContent().trim());
        comment.setIsDeleted(false);
        
        if (request.getParentCommentId() != null) {
            PostComment parentComment = postCommentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Comment cha không tồn tại"));
            
            if (parentComment.getIsDeleted()) {
                throw new InvalidRequestException("Không thể reply vào comment đã bị xóa");
            }
            
            if (!parentComment.getPostId().equals(request.getPostId())) {
                throw new InvalidRequestException("Comment cha không thuộc bài viết này");
            }
            
            comment.setParentComment(parentComment);
            comment.setParentCommentId(request.getParentCommentId());
        }
        
        PostComment savedComment = postCommentRepository.save(comment);
        
        PostCommentResponse response = postCommentMapper.toResponse(savedComment, false);
        
        postWebSocketService.broadcastNewComment(post.getGroupId(), post.getId(), response);
        
        return response;
    }
    
    @Override
    public List<PostCommentResponse> getCommentsByPostId(Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Bài viết không tồn tại"));
        
        List<PostComment> comments = postCommentRepository
                .findByPostIdAndIsDeletedFalseAndParentCommentIdIsNullOrderByCreatedAtAsc(postId);
        
        return postCommentMapper.toResponseList(comments, true);
    }
    
    @Override
    @Transactional
    public PostCommentResponse deleteComment(Long commentId) {
        Long currentUserId = securityUtil.getCurrentUserId();
        
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment không tồn tại"));
        
        if (!comment.getAuthorId().equals(currentUserId)) {
            throw new UserIsNotCommentAuthorException("Bạn không có quyền xóa comment này");
        }
        
        if (comment.getIsDeleted()) {
            throw new CommentIsDeletedException("Comment đã bị xóa trước đó");
        }
        
        comment.setIsDeleted(true);
        PostComment deletedComment = postCommentRepository.save(comment);
        
        List<PostComment> replies = postCommentRepository.findByParentCommentId(commentId);
        if (!replies.isEmpty()) {
            for (PostComment reply : replies) {
                reply.setParentComment(null);
                reply.setParentCommentId(null);
            }
            postCommentRepository.saveAll(replies);
        }
        
        PostCommentResponse response = postCommentMapper.toResponse(deletedComment, false);
        
        postWebSocketService.broadcastDeleteComment(
            deletedComment.getPost().getGroupId(),
            deletedComment.getPostId(),
            response
        );
        
        return response;
    }

    @Override
    @Transactional
    public PostCommentResponse updateComment(UpdatePostComment updatePostComment, Long commentId) {
        if (updatePostComment.getContent() == null || updatePostComment.getContent().trim().isEmpty()) {
            throw new InvalidRequestException("Nội dung comment không được để trống");
        }
        
        if (updatePostComment.getContent().length() > 5000) {
            throw new InvalidRequestException("Nội dung comment không được vượt quá 5000 ký tự");
        }
        
        Long currentUserId = securityUtil.getCurrentUserId();
        
        PostComment postComment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment không tồn tại"));

        if (!postComment.getAuthorId().equals(currentUserId)) {
            throw new UserIsNotCommentAuthorException("Bạn không có quyền sửa comment này");
        }
        
        if (postComment.getIsDeleted()) {
            throw new CommentIsDeletedException("Không thể sửa comment đã bị xóa");
        }

        postComment.setContent(updatePostComment.getContent().trim());

        PostComment updatedPostComment = postCommentRepository.save(postComment);
        
        PostCommentResponse response = postCommentMapper.toResponse(updatedPostComment, false);
        
        postWebSocketService.broadcastUpdateComment(
            updatedPostComment.getPost().getGroupId(), 
            updatedPostComment.getPostId(), 
            response
        );
        
        return response;
    }
}

