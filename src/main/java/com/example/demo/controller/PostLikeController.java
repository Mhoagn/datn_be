package com.example.demo.controller;

import com.example.demo.dto.PostDTO.PostLikeRequest;
import com.example.demo.dto.PostDTO.PostLikeResponse;
import com.example.demo.service.interf.PostLikeInterface;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/post-like")
@AllArgsConstructor
public class PostLikeController {
    
    private final PostLikeInterface postLikeService;

    @PostMapping("/toggle")
    public ResponseEntity<?> toggleLike(@RequestBody @Valid PostLikeRequest request) {
        PostLikeResponse response = postLikeService.toggleLike(request);
        
        if (response == null) {
            return ResponseEntity.ok(Map.of(
                "message", "Unlike thành công",
                "liked", false
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                "message", "Like thành công",
                "liked", true,
                "data", response
            ));
        }
    }
}
