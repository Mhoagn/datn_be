package com.example.demo.service.impl;

import com.example.demo.dto.PostDTO.PostCommentResponse;
import com.example.demo.dto.PostDTO.PostResponse;
import com.example.demo.dto.PostWebSocketMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class PostWebSocketService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public void broadcastNewPost(Long groupId, PostResponse postResponse) {
        PostWebSocketMessage message = PostWebSocketMessage.builder()
                .type(PostWebSocketMessage.MessageType.POST_CREATED.name())
                .groupId(groupId)
                .data(postResponse)
                .timestamp(LocalDateTime.now())
                .build();
        
        String destination = "/topic/group." + groupId + ".posts";
        messagingTemplate.convertAndSend(destination, message);
        
        log.info("Broadcasted new post {} to group {}", postResponse.getId(), groupId);
    }
    
    public void broadcastUpdatePost(Long groupId, PostResponse postResponse) {
        PostWebSocketMessage message = PostWebSocketMessage.builder()
                .type(PostWebSocketMessage.MessageType.POST_UPDATED.name())
                .groupId(groupId)
                .data(postResponse)
                .timestamp(LocalDateTime.now())
                .build();
        
        String destination = "/topic/group." + groupId + ".posts";
        messagingTemplate.convertAndSend(destination, message);
        
        log.info("Broadcasted updated post {} to group {}", postResponse.getId(), groupId);
    }
    
    public void broadcastDeletePost(Long groupId, PostResponse postResponse) {
        PostWebSocketMessage message = PostWebSocketMessage.builder()
                .type(PostWebSocketMessage.MessageType.POST_DELETED.name())
                .groupId(groupId)
                .data(postResponse)
                .timestamp(LocalDateTime.now())
                .build();
        
        String destination = "/topic/group." + groupId + ".posts";
        messagingTemplate.convertAndSend(destination, message);
        
        log.info("Broadcasted deleted post {} to group {}", postResponse.getId(), groupId);
    }
    
    public void broadcastPostLike(Long groupId, Long postId, boolean liked, Long userId, String userName) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", liked ? "POST_LIKED" : "POST_UNLIKED");
        payload.put("postId", postId);
        payload.put("userId", userId);
        payload.put("userName", userName);
        payload.put("timestamp", LocalDateTime.now());
        
        String destination = "/topic/group." + groupId + ".post." + postId + ".likes";
        messagingTemplate.convertAndSend(destination, (Object) payload);
        
        log.info("Broadcasted post {} {} by user {} to group {}", postId, liked ? "liked" : "unliked", userId, groupId);
    }
    
    public void broadcastNewComment(Long groupId, Long postId, PostCommentResponse commentResponse) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "COMMENT_CREATED");
        payload.put("postId", postId);
        payload.put("groupId", groupId);
        payload.put("data", commentResponse);
        payload.put("timestamp", LocalDateTime.now());
        
        String destination = "/topic/group." + groupId + ".post." + postId + ".comments";
        messagingTemplate.convertAndSend(destination, (Object) payload);
        
        log.info("Broadcasted new comment {} to post {} in group {}", commentResponse.getId(), postId, groupId);
    }
    
    public void broadcastUpdateComment(Long groupId, Long postId, PostCommentResponse commentResponse) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "COMMENT_UPDATED");
        payload.put("postId", postId);
        payload.put("groupId", groupId);
        payload.put("data", commentResponse);
        payload.put("timestamp", LocalDateTime.now());
        
        String destination = "/topic/group." + groupId + ".post." + postId + ".comments";
        messagingTemplate.convertAndSend(destination, (Object) payload);
        
        log.info("Broadcasted updated comment {} to post {} in group {}", commentResponse.getId(), postId, groupId);
    }
    
    public void broadcastDeleteComment(Long groupId, Long postId, PostCommentResponse commentResponse) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "COMMENT_DELETED");
        payload.put("postId", postId);
        payload.put("groupId", groupId);
        payload.put("data", commentResponse);
        payload.put("timestamp", LocalDateTime.now());
        
        String destination = "/topic/group." + groupId + ".post." + postId + ".comments";
        messagingTemplate.convertAndSend(destination, (Object) payload);
        
        log.info("Broadcasted deleted comment {} to post {} in group {}", commentResponse.getId(), postId, groupId);
    }
}
