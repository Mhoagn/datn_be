package com.example.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Mã OTP đặt lại mật khẩu");
            message.setText(
                    "Xin chào,\n\n" +
                            "Mã OTP của bạn là: " + otp + "\n\n" +
                            "Mã có hiệu lực trong 10 phút.\n" +
                            "Nếu bạn không yêu cầu, hãy bỏ qua email này."
            );
            mailSender.send(message);
            log.info("[Email] OTP sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("[Email] Failed to send OTP to {}: {}", toEmail, e.getMessage(), e);
        }
    }
}
