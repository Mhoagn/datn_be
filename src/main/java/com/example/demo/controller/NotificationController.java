package com.example.demo.controller;

import com.example.demo.dto.NotificationDTO.NotificationResponse;
import com.example.demo.dto.NotificationDTO.UnreadNotificationResponse;
import com.example.demo.dto.NotificationDTO.UpdateNotificationResponse;
import com.example.demo.dto.SliceResponse;
import com.example.demo.service.interf.NotificationInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationInterface notificationService;

    @GetMapping
    public ResponseEntity<SliceResponse<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(notificationService.getNotifications(page, size));
    }

    @GetMapping("/unread")
    public ResponseEntity<UnreadNotificationResponse> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(notificationService.getUnreadNotifications(page, size));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<UpdateNotificationResponse> updateStatusNotification(
            @PathVariable Long notificationId) {

        return ResponseEntity.ok(notificationService.updateStatusNotification(notificationId));
    }
}
