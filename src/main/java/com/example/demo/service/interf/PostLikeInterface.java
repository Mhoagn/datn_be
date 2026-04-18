package com.example.demo.service.interf;

import com.example.demo.dto.PostDTO.PostLikeRequest;
import com.example.demo.dto.PostDTO.PostLikeResponse;

public interface PostLikeInterface {
    PostLikeResponse toggleLike(PostLikeRequest postLikeRequest);

}
