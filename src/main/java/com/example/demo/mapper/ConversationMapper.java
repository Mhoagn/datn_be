package com.example.demo.mapper;

import com.example.demo.dto.conversationDTO.ConversationResponse;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.Message;
import com.example.demo.entity.User;
import com.example.demo.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConversationMapper {
    private final MessageRepository messageRepository;

    public ConversationResponse toConversationResponse(
            Conversation conversation, Long currentUserId) {

        User otherUser = conversation.getUser1().getId().equals(currentUserId)
                ? conversation.getUser2()
                : conversation.getUser1();

        return buildResponse(conversation, currentUserId, otherUser);
    }

    public ConversationResponse toConversationResponse(
            Conversation conversation, Long currentUserId, User otherUser) {

        return buildResponse(conversation, currentUserId, otherUser);
    }

    private ConversationResponse buildResponse(
            Conversation conversation, Long currentUserId, User otherUser) {
        
        Message lastMessage = messageRepository
                .findTopByConversationIdOrderBySentAtDesc(conversation.getId())
                .orElse(null);

        int unreadCount = messageRepository
                .countUnreadMessages(conversation.getId(), currentUserId);

        return ConversationResponse.builder()
                .id(conversation.getId())
                .otherUserId(otherUser.getId())
                .otherUserName(otherUser.getFullname())
                .otherUserAvatar(otherUser.getAvatarUrl())
                .otherUserStatus(String.valueOf(otherUser.getOnlineStatus()))
                .lastMessageId(lastMessage != null ? lastMessage.getId() : null)
                .lastMessageContent(getDisplayContent(lastMessage))
                .lastMessageAt(conversation.getLastMessageAt())
                .unreadCount(unreadCount)
                .isLastMessageDeleted(lastMessage != null ? lastMessage.getIsDeleted() : null)
                .build();
    }

    private String getDisplayContent(Message message) {
        if (message == null) return null;
        if (message.getIsDeleted()) return "Tin nhắn đã bị xóa";
        if (message.getContent() != null && !message.getContent().trim().isEmpty()) {
            return message.getContent();
        }
        if (message.getMessageType() != null) {
            switch (message.getMessageType()) {
                case IMAGE: return "[Hình ảnh]";
                case VIDEO: return "[Video]";
                case AUDIO: return "[Âm thanh]";
                case FILE:  return "[Tệp đính kèm]";
                default: break;
            }
        }
        return (message.getAttachmentUrl() != null) ? "[Tệp đính kèm]" : null;
    }
}
