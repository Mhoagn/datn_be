package com.example.demo.mapper;

import com.example.demo.dto.messageDTO.MessageResponse;
import com.example.demo.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {
    public MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                // QUAN TRỌNG: Lấy từ relationship thay vì field conversationId
                // vì field conversationId có insertable=false nên sau save vẫn null
                .conversationId(message.getConversation() != null ? message.getConversation().getId() : null)
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullname())
                .senderAvatar(message.getSender().getAvatarUrl())
                .content(message.getIsDeleted() ? null : message.getContent())
                .messageType(message.getMessageType())
                .attachmentUrl(message.getIsDeleted() ? null : message.getAttachmentUrl())
                .attachmentPublicId(message.getIsDeleted() ? null : message.getAttachmentPublicId())
                .attachmentName(message.getIsDeleted() ? null : message.getAttachmentName())
                .attachmentSize(message.getIsDeleted() ? null : message.getAttachmentSize())
                .isRead(message.getIsRead())
                .isDeleted(message.getIsDeleted())
                .sentAt(message.getSentAt())
                .readAt(message.getReadAt())
                .build();
    }
}
