package com.example.demo.service.impl;

import com.example.demo.dto.SliceResponse;
import com.example.demo.dto.conversationDTO.ConversationResponse;
import com.example.demo.dto.conversationDTO.CreateOrGetConversationResult;
import com.example.demo.dto.messageDTO.MessageResponse;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.Message;
import com.example.demo.entity.User;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.mapper.ConversationMapper;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.interf.ConversationInterface;
import com.example.demo.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ConversationService implements ConversationInterface {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ConversationMapper conversationMapper;
    private final SecurityUtil securityUtil;

    @Override
    public CreateOrGetConversationResult createOrGetConversation(String targetEmail) {
        Long currentUserId = securityUtil.getCurrentUserId();

        User targetUser = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));

        if (currentUserId.equals(targetUser.getId())) {
            throw new RuntimeException("Không thể tạo cuộc trò chuyện với chính mình");
        }

        // Lấy currentUser object
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));

        // Enforce: user1 < user2 theo id
        User smallerUser = currentUserId < targetUser.getId() ? currentUser : targetUser;
        User largerUser  = currentUserId < targetUser.getId() ? targetUser : currentUser;

        Long smallerId = smallerUser.getId();
        Long largerId  = largerUser.getId();

        Optional<Conversation> existing =
                conversationRepository.findByTwoUsers(smallerId, largerId);

        if (existing.isPresent()) {
            return CreateOrGetConversationResult.builder()
                    .conversationResponse(conversationMapper.toConversationResponse(
                            existing.get(), currentUserId, targetUser))
                    .isNew(false)
                    .build();
        }

        Conversation conversation = new Conversation();
        conversation.setUser1(smallerUser);
        conversation.setUser2(largerUser);
        Conversation saved = conversationRepository.save(conversation);

        return CreateOrGetConversationResult.builder()
                .conversationResponse(conversationMapper.toConversationResponse(
                        saved, currentUserId, targetUser))
                .isNew(true)
                .build();
    }

    @Override
    public SliceResponse<ConversationResponse> getConversations(int page, int size) {
        Long currentUserId = securityUtil.getCurrentUserId();

        Pageable pageable = PageRequest.of(page, size);

        Slice<Conversation> slice = conversationRepository
                .findAllByUserId(currentUserId, pageable);

        List<ConversationResponse> content = slice.getContent()
                .stream()
                .map(conversation -> conversationMapper
                        .toConversationResponse(conversation, currentUserId))
                .collect(Collectors.toList());

        return SliceResponse.<ConversationResponse>builder()
                .content(content)
                .page(slice.getNumber())
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .build();
    }


}
