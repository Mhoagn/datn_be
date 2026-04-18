package com.example.demo.service.interf;

import com.example.demo.dto.SliceResponse;
import com.example.demo.dto.messageDTO.DeleteMessageRequest;
import com.example.demo.dto.messageDTO.MarkAsReadResponse;
import com.example.demo.dto.messageDTO.MessageRequest;
import com.example.demo.dto.messageDTO.MessageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MessageInterface {
    SliceResponse<MessageResponse> getConversationMessages(Long conversationId, int page, int size);
    MessageResponse sendMessage(Long senderId, Long conversationId, MessageRequest request);
    MessageResponse deleteMessage(DeleteMessageRequest request);         // REST
    MessageResponse deleteMessage(Long currentUserId, Long messageId);  // WebSocket
    MarkAsReadResponse markAsRead(Long currentUserId, Long conversationId);
    MessageResponse uploadAttachment(Long conversationId, MultipartFile file);
    void notifyNewMessage(MessageResponse message);
}
