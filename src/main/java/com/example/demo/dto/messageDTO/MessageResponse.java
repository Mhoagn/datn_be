package com.example.demo.dto.messageDTO;

import com.example.demo.entity.Message;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private Message.MessageType messageType;
    private String attachmentUrl;
    private String attachmentPublicId;
    private String attachmentName;
    private Long attachmentSize;
    @JsonProperty("isRead")
    private Boolean isRead;
    @JsonProperty("isDeleted")
    private Boolean isDeleted;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
}
