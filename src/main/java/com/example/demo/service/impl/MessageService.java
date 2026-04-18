package com.example.demo.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.SliceResponse;
import com.example.demo.dto.messageDTO.DeleteMessageRequest;
import com.example.demo.dto.messageDTO.MarkAsReadResponse;
import com.example.demo.dto.messageDTO.MessageRequest;
import com.example.demo.dto.messageDTO.MessageResponse;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.Message;
import com.example.demo.entity.User;
import com.example.demo.exception.ConversationNotFoundException;
import com.example.demo.exception.MessageNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.exception.UserNotInConversationException;
import com.example.demo.exception.UserNotSendMessageException;
import com.example.demo.mapper.MessageMapper;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.interf.MessageInterface;
import com.example.demo.util.SecurityUtil;

import io.jsonwebtoken.io.IOException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MessageService implements MessageInterface {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final MessageRepository messageRepository;
    private final CloudinaryService cloudinaryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final org.springframework.messaging.simp.user.SimpUserRegistry simpUserRegistry;

    private final SecurityUtil securityUtil;

    @Override
    public SliceResponse<MessageResponse> getConversationMessages(
            Long conversationId, int page, int size) {

        Long currentUserId = securityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));

        // 1. Kiểm tra conversation tồn tại
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Đoạn chat không tồn tại"));

        // 2. Kiểm tra user có thuộc conversation không
        boolean isMember = conversation.getUser1().getId().equals(currentUserId)
                || conversation.getUser2().getId().equals(currentUserId);

        if (!isMember) {
            throw new UserNotInConversationException("Người dùng không tham gia đoạn chat");
        }

        // 3. Lấy messages
        Pageable pageable = PageRequest.of(page, size);
        Slice<Message> slice = messageRepository
                .findByConversationId(conversationId, pageable);

        List<MessageResponse> content = slice.getContent()
                .stream()
                .map(messageMapper::toMessageResponse)
                .collect(Collectors.toList());

        return SliceResponse.<MessageResponse>builder()
                .content(content)
                .page(slice.getNumber())
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .build();
    }

    @Override
    public MessageResponse sendMessage(Long senderId, Long conversationId, MessageRequest request) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Đoạn chat không tồn tại"));

        // 2. Kiểm tra sender có thuộc conversation không
        boolean isMember = conversation.getUser1().getId().equals(senderId)
                || conversation.getUser2().getId().equals(senderId);
        if (!isMember) {
            throw new UserNotInConversationException("Người dùng không tham gia đoạn chat");
        }

        // Lấy lại user (sender)
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));

        // 3. Lưu message
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType());
        message.setAttachmentUrl(request.getAttachmentUrl());
        message.setAttachmentPublicId(request.getAttachmentPublicId());
        message.setAttachmentName(request.getAttachmentName());
        message.setAttachmentSize(request.getAttachmentSize());
        Message saved = messageRepository.save(message);

        // 4. Cập nhật lastMessageAt của conversation
        conversation.setLastMessageAt(saved.getSentAt());
        conversationRepository.save(conversation);

        return messageMapper.toMessageResponse(saved);
    }

    @Override
    @Transactional
    public MessageResponse deleteMessage(DeleteMessageRequest request) {
        Long currentUserId = securityUtil.getCurrentUserId();
        return deleteMessageById(currentUserId, request.getMessageId());
    }

    // Dùng cho WebSocket — nhận userId từ ngoài truyền vào
    @Override
    @Transactional
    public MessageResponse deleteMessage(Long currentUserId, Long messageId) {
        return deleteMessageById(currentUserId, messageId);
    }

    // Logic chung
    @Transactional
    private MessageResponse deleteMessageById(Long currentUserId, Long messageId) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Tin nhắn không tồn tại"));

        if (!message.getSenderId().equals(currentUserId)) {
            throw new UserNotSendMessageException("Người dùng không phải người gửi tin nhắn");
        }

        message.setIsDeleted(true);
        message.setContent(null);
        message.setAttachmentUrl(null);
        message.setAttachmentPublicId(null);
        message.setAttachmentName(null);
        message.setAttachmentSize(null);
        Message saved = messageRepository.save(message);

        return messageMapper.toMessageResponse(saved);
    }

    @Override
    public MarkAsReadResponse markAsRead(Long currentUserId, Long conversationId) {

        // 1. Kiểm tra conversation tồn tại
        conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Đoạn chat không tồn tại"));

        // 2. Lấy tất cả tin nhắn chưa đọc của người kia gửi
        List<Message> unreadMessages = messageRepository
                .findUnreadMessages(conversationId, currentUserId);

        // 3. Không có tin nào chưa đọc → trả về luôn
        if (unreadMessages.isEmpty()) {
            return MarkAsReadResponse.builder()
                    .conversationId(conversationId)
                    .readerId(currentUserId)
                    .readAt(LocalDateTime.now())
                    .messageIds(List.of())
                    .build();
        }

        // 4. Đánh dấu tất cả là đã đọc
        LocalDateTime readAt = LocalDateTime.now();
        unreadMessages.forEach(message -> {
            message.setIsRead(true);
            message.setReadAt(readAt);
        });
        messageRepository.saveAll(unreadMessages);

        // 5. Lấy danh sách id đã đọc
        List<Long> messageIds = unreadMessages.stream()
                .map(Message::getId)
                .collect(Collectors.toList());

        return MarkAsReadResponse.builder()
                .conversationId(conversationId)
                .readerId(currentUserId)
                .readAt(readAt)
                .messageIds(messageIds)
                .build();
    }

    @Override
    @Transactional
    public MessageResponse uploadAttachment(Long conversationId, MultipartFile file) {

        Long currentUserId = securityUtil.getCurrentUserId();

        // 1. Kiểm tra conversation tồn tại
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Đoạn chat không tồn tại"));

        // 2. Kiểm tra user có thuộc conversation không
        boolean isMember = conversation.getUser1().getId().equals(currentUserId)
                || conversation.getUser2().getId().equals(currentUserId);
        if (!isMember) {
            throw new UserNotInConversationException("Người dùng không tham gia đoạn chat");
        }

        // 3. Upload lên Cloudinary — dùng method có sẵn
        Map<String, Object> uploadResult;
        String contentType = file.getContentType();

        try {
            if (contentType != null && contentType.startsWith("image/")) {
                uploadResult = cloudinaryService.uploadImage(file, "chat-attachments");

            } else if (contentType != null && contentType.startsWith("video/")) {
                uploadResult = cloudinaryService.uploadVideo(file, "chat-attachments");

            } else {
                uploadResult = cloudinaryService.uploadFile(file, "chat-attachments");
            }
        } catch (IOException | java.io.IOException e) {
            throw new RuntimeException("Upload file thất bại: " + e.getMessage());
        }

        // 4. Lấy thông tin từ kết quả upload
        String attachmentUrl      = (String) uploadResult.get("secure_url");
        String attachmentPublicId = (String) uploadResult.get("public_id");
        Long attachmentSize       = ((Number) uploadResult.get("bytes")).longValue();
        String attachmentName     = file.getOriginalFilename();

        // 5. Lấy User entity
        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));

        // 6. Lưu message vào DB
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setConversationId(conversationId);
        message.setSenderId(currentUserId);
        message.setContent(null);
        message.setMessageType(resolveMessageType(contentType));
        message.setAttachmentUrl(attachmentUrl);
        message.setAttachmentPublicId(attachmentPublicId);
        message.setAttachmentName(attachmentName);
        message.setAttachmentSize(attachmentSize);
        Message saved = messageRepository.save(message);

        // 7. Cập nhật lastMessageAt
        conversation.setLastMessageAt(saved.getSentAt());
        conversationRepository.save(conversation);

        // 8. Push WebSocket để người kia nhận file ngay
        MessageResponse response = messageMapper.toMessageResponse(saved);
        messagingTemplate.convertAndSend(
                "/topic/conversation." + conversationId,
                response
        );
        
        // 9. Push thông báo đến user cá nhân để cập nhật danh sách conversation
        notifyNewMessage(response);

        return response;
    }

    // Helper — xác định MessageType từ contentType
    private Message.MessageType resolveMessageType(String contentType) {
        if (contentType == null)                    return Message.MessageType.FILE;
        if (contentType.startsWith("image/"))       return Message.MessageType.IMAGE;
        if (contentType.startsWith("video/"))       return Message.MessageType.VIDEO;
        if (contentType.startsWith("audio/"))       return Message.MessageType.AUDIO;
        return Message.MessageType.FILE;
    }
    
    // Helper — push thông báo tin nhắn mới đến user cá nhân
    public void notifyNewMessage(MessageResponse message) {
        System.out.println("[Notify] Processing notification for message ID: " + message.getId());
        
        // Lấy conversation để biết ai là người nhận
        Conversation conversation = conversationRepository.findById(message.getConversationId())
                .orElse(null);
        
        if (conversation == null) {
            System.err.println("[Notify] Conversation not found: " + message.getConversationId());
            return;
        }
        
        // Xác định người nhận (không phải người gửi)
        Long receiverId = conversation.getUser1().getId().equals(message.getSenderId())
                ? conversation.getUser2().getId()
                : conversation.getUser1().getId();
        
        System.out.println("[Notify] Sender: " + message.getSenderId() + ", Receiver: " + receiverId);
        
        // Debug: Kiểm tra xem user có đang connected không
        var receiverUser = simpUserRegistry.getUser(receiverId.toString());
        if (receiverUser == null) {
            System.err.println("[Notify] WARNING: Receiver user " + receiverId + " not found in registry (not connected)");
            System.out.println("[Notify] Current connected users:");
            simpUserRegistry.getUsers().forEach(u -> 
                System.out.println("  - User: " + u.getName() + " with " + u.getSessions().size() + " sessions")
            );
        } else {
            System.out.println("[Notify] Receiver user " + receiverId + " found with " + receiverUser.getSessions().size() + " active sessions");
        }
        
        // Tạo payload thông báo
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("type", "NEW_MESSAGE");
        payload.put("message", message);
        
        // Push đến queue cá nhân của người nhận
        // QUAN TRỌNG: Dùng convertAndSendToUser() thay vì convertAndSend()
        // Spring STOMP sẽ tự động resolve user session và route đúng
        System.out.println("[Notify] Sending to user: " + receiverId + " at /queue/messages");
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/messages",
                payload
        );
        System.out.println("[Notify] Message sent via convertAndSendToUser");
    }
}

