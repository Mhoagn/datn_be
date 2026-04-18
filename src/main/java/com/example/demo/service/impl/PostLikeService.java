package com.example.demo.service.impl;

import com.example.demo.dto.PostDTO.PostLikeRequest;
import com.example.demo.dto.PostDTO.PostLikeResponse;
import com.example.demo.entity.GroupMember;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostLike;
import com.example.demo.entity.User;
import com.example.demo.exception.PostIsDeletedException;
import com.example.demo.exception.PostNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.exception.UserNotInGroupException;
import com.example.demo.mapper.PostLikeMapper;
import com.example.demo.repository.*;
import com.example.demo.service.interf.PostLikeInterface;
import com.example.demo.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class PostLikeService implements PostLikeInterface {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final PostLikeMapper postLikeMapper;
    private final SecurityUtil securityUtil;
    private final PostWebSocketService postWebSocketService;

    @Override
    @Transactional
    public PostLikeResponse toggleLike(PostLikeRequest postLikeRequest) {
        Long currentUserId = securityUtil.getCurrentUserId();
        
        Post post = postRepository.findById(postLikeRequest.getPostId())
                .orElseThrow(() -> new PostNotFoundException("Bài viết không tồn tại"));

        if (post.getIsDeleted()) {
            throw new PostIsDeletedException("Không thể like bài viết đã bị xóa");
        }

        GroupMember groupMember = groupMemberRepository.findByUserIdAndGroupId(currentUserId, post.getGroupId())
                .orElseThrow(() -> new UserNotInGroupException("Bạn không phải thành viên của nhóm này"));

        if (!groupMember.getIsActive()) {
            throw new UserNotInGroupException("Bạn không còn là thành viên hoạt động của nhóm này");
        }

        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(
                postLikeRequest.getPostId(), 
                currentUserId
        );

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));
        
        if (existingLike.isPresent()) {
            postLikeRepository.deleteByPostIdAndUserId(postLikeRequest.getPostId(), currentUserId);
            
            postWebSocketService.broadcastPostLike(
                post.getGroupId(), 
                postLikeRequest.getPostId(), 
                false,
                currentUserId,
                user.getFullname()
            );
            
            return null;
        } else {
            PostLike postLike = new PostLike();
            postLike.setPost(post);
            postLike.setUser(user);
            postLike.setPostId(postLikeRequest.getPostId());
            postLike.setUserId(currentUserId);

            PostLike savedPostLike = postLikeRepository.save(postLike);
            
            postWebSocketService.broadcastPostLike(
                post.getGroupId(), 
                postLikeRequest.getPostId(), 
                true,
                currentUserId,
                user.getFullname()
            );
            
            return postLikeMapper.toResponse(savedPostLike);
        }
    }

}
