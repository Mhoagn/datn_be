package com.example.demo.controller;

import com.example.demo.service.impl.LiveKitWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook/livekit")
@RequiredArgsConstructor
@Slf4j
public class LiveKitWebhookController {

    private final LiveKitWebhookService webhookService;

    /**
     * Nhận webhook từ LiveKit khi có sự kiện egress
     * LiveKit sẽ gửi POST request với body chứa thông tin về egress
     */
    @PostMapping("/egress")
    public ResponseEntity<Map<String, String>> handleEgressWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Received LiveKit egress webhook: {}", payload);

        try {
            webhookService.handleEgressWebhook(payload);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            log.error("Error processing LiveKit webhook: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
