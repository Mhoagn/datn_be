package com.example.demo.dto.messageDTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class MarkAsReadResponse {
    private Long conversationId;
    private Long readerId;
    private LocalDateTime readAt;
    private List<Long> messageIds; // danh sách tin nhắn đã được đánh dấu đọc
}
