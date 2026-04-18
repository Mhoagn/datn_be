package com.example.demo.service.impl;

import com.example.demo.dto.TranscriptDTO.AIServiceRequest;
import com.example.demo.dto.TranscriptDTO.AIServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AIServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${ai-service.url}")
    private String aiServiceUrl;
    
    /**
     * Gọi AI service để xử lý video: transcript + summary
     */
    public AIServiceResponse processVideo(AIServiceRequest request) {
        String url = aiServiceUrl + "/process-video";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<AIServiceRequest> entity = new HttpEntity<>(request, headers);
        
        return restTemplate.postForObject(url, entity, AIServiceResponse.class);
    }
}
