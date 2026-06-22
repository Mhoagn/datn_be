package com.example.demo.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from-email:onboarding@resend.dev}")
    private String fromEmail;

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "from", fromEmail,
                    "to", List.of(toEmail),
                    "subject", "Mã OTP đặt lại mật khẩu",
                    "text", "Xin chào,\n\n" +
                            "Mã OTP của bạn là: " + otp + "\n\n" +
                            "Mã có hiệu lực trong 10 phút.\n" +
                            "Nếu bạn không yêu cầu, hãy bỏ qua email này."
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(RESEND_API_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[Email] OTP sent successfully to {}", toEmail);
            } else {
                log.error("[Email] Resend API returned {}: {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("[Email] Failed to send OTP to {}: {}", toEmail, e.getMessage(), e);
        }
    }
}
