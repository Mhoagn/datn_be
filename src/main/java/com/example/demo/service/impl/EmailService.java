package com.example.demo.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.from-email}")
    private String fromEmail;

    @Value("${brevo.from-name:DATN App}")
    private String fromName;

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        String subject = "Mã OTP đặt lại mật khẩu";
        String content = "Xin chào,\n\n" +
                "Mã OTP của bạn là: " + otp + "\n\n" +
                "Mã có hiệu lực trong 10 phút.\n" +
                "Nếu bạn không yêu cầu, hãy bỏ qua email này.";
        sendEmail(toEmail, subject, content);
    }

    @Async
    public void sendMeetingScheduledEmail(
            String toEmail,
            String fullname,
            String groupName,
            LocalDateTime scheduledStartAt,
            String creatorName
    ) {
        String subject = "Lịch họp mới trong nhóm " + groupName;
        String content = "Xin chào " + fullname + ",\n\n" +
                "Nhóm \"" + groupName + "\" có cuộc họp được lên lịch:\n\n" +
                "Thời gian: " + scheduledStartAt.format(DATE_TIME_FORMATTER) + "\n" +
                "Người tạo: " + creatorName + "\n\n" +
                "Vui lòng chuẩn bị tham gia đúng giờ.";
        sendEmail(toEmail, subject, content);
    }

    @Async
    public void sendMeetingCreatorReminderEmail(
            String toEmail,
            String fullname,
            String groupName,
            LocalDateTime scheduledStartAt
    ) {
        String subject = "Nhắc nhở: Cuộc họp sắp bắt đầu trong 5 phút";
        String content = "Xin chào " + fullname + ",\n\n" +
                "Cuộc họp sắp diễn ra:\n\n" +
                "Nhóm: " + groupName + "\n" +
                "Thời gian: " + scheduledStartAt.format(DATE_TIME_FORMATTER) + "\n\n" +
                "Hãy mở ứng dụng và bắt đầu cuộc họp khi đến giờ.";
        sendEmail(toEmail, subject, content);
    }

    private void sendEmail(String toEmail, String subject, String textContent) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "sender", Map.of("name", fromName, "email", fromEmail),
                    "to", List.of(Map.of("email", toEmail)),
                    "subject", subject,
                    "textContent", textContent
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_API_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[Email] Sent successfully to {} - subject: {}", toEmail, subject);
            } else {
                log.error("[Email] Brevo API returned {}: {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("[Email] Failed to send to {}: {}", toEmail, e.getMessage(), e);
        }
    }
}
