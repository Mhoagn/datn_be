package com.example.demo.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.demo.dto.TranscriptDTO.AIServiceRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceClient {

    private final RestTemplate restTemplate;

    @Value("${ai-service.url}")
    private String aiServiceUrl;

    /**
     * Gửi yêu cầu xử lý video lên AI-service dưới dạng background job.
     * Trả về ngay lập tức với job_id — không giữ connection lâu,
     * tránh lỗi ngrok ERR_NGROK_3004 (timeout do xử lý quá lâu).
     */
    public AIServiceJobStartResponse startProcessingJob(AIServiceRequest request) {
        String url = aiServiceUrl + "/start-video-processing";
        log.info("Gửi yêu cầu start job đến AI-service: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AIServiceRequest> entity = new HttpEntity<>(request, headers);

        AIServiceJobStartResponse response = restTemplate.postForObject(url, entity, AIServiceJobStartResponse.class);
        log.info("AI-service đã nhận job, job_id = {}", response != null ? response.getJobId() : "null");
        return response;
    }

    /**
     * Kiểm tra trạng thái của một job đang xử lý.
     * Được gọi định kỳ (polling) từ TranscriptService.
     */
    public AIServiceJobStatusResponse getJobStatus(String jobId) {
        String url = aiServiceUrl + "/job-status/" + jobId;
        return restTemplate.getForObject(url, AIServiceJobStatusResponse.class);
    }
}
