package com.example.demo.service.interf;

import com.example.demo.dto.PostDTO.CreatePostRequest;
import com.example.demo.dto.PostDTO.PostResponse;
import com.example.demo.dto.PostDTO.UpdatePostRequest;

import java.util.List;

public interface PostInterface {
    PostResponse createPost(CreatePostRequest createPostRequest);
    PostResponse updatePost(UpdatePostRequest updatePostRequest,Long postId);
    PostResponse deletePost(Long postId);
    List<PostResponse> getActivePostInGroup(Long groupId);
}
