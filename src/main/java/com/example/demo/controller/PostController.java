package com.example.demo.controller;

import com.example.demo.dto.PostDTO.CreatePostRequest;
import com.example.demo.dto.PostDTO.PostResponse;
import com.example.demo.dto.PostDTO.UpdatePostRequest;
import com.example.demo.service.interf.PostInterface;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/post")
@AllArgsConstructor
public class PostController {
    
    private final PostInterface postService;
    
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @RequestParam("groupId") Long groupId,
            @RequestParam("content") String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        CreatePostRequest request = CreatePostRequest.builder()
                .groupId(groupId)
                .content(content)
                .files(files)
                .build();
        
        PostResponse response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "keepMediaUrls", required = false) List<String> keepMediaUrls
    ) {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setContent(content);
        request.setMediaInfoList(files);
        request.setKeepMediaUrls(keepMediaUrls);
        
        PostResponse response = postService.updatePost(request, postId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<PostResponse> deletePost(@PathVariable Long postId) {
        PostResponse response = postService.deletePost(postId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{groupId}")
    public ResponseEntity<List<PostResponse>> getActivePosts(@PathVariable Long groupId) {
        List<PostResponse> posts = postService.getActivePostInGroup(groupId);
        return ResponseEntity.ok(posts);
    }
}
