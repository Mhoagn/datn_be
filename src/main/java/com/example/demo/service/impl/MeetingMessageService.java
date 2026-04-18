package com.example.demo.service.impl;

import com.example.demo.dto.MeetingMessage.MeetingMessageResponse;
import com.example.demo.dto.MeetingMessage.SendMessageRequest;
import com.example.demo.dto.SliceResponse;
import com.example.demo.entity.Meeting;
import com.example.demo.entity.MeetingMessage;
import com.example.demo.entity.User;
import com.example.demo.exception.MeetingNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.exception.UserNotInMeetingException;
import com.example.demo.mapper.MeetingMessageMapper;
import com.example.demo.repository.MeetingMessageRepository;
import com.example.demo.repository.MeetingParticipantRepository;
import com.example.demo.repository.MeetingRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.interf.MeetingMessageInterface;
import com.example.demo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeetingMessageService implements MeetingMessageInterface {
    private final MeetingRepository meetingRepository;
    private final MeetingMessageRepository messageRepository;
    private final MeetingParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final MeetingMessageMapper messageMapper;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public MeetingMessageResponse sendMessage(Long meetingId, SendMessageRequest request) {
        Long currentUserId = securityUtil.getCurrentUserId();

        // 1. Validate meeting tồn tại và ONGOING
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException("Cuộc họp không tồn tại"));

        if (meeting.getStatus() != Meeting.Status.ONGOING) {
            throw new MeetingNotFoundException("Cuộc họp không tồn tại");
        }

        // 2. Validate user đang active trong meeting
        participantRepository.findActiveSession(currentUserId, meetingId)
                .orElseThrow(() -> new UserNotInMeetingException("Người dùng chưa tham gia cuộc họp"));

        // 3. Fetch user
        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));

        // 4. Lưu message vào DB
        MeetingMessage message = new MeetingMessage();
        message.setMeeting(meeting);
        message.setAuthor(author);
        message.setContent(request.getContent());
        message.setMessageType(MeetingMessage.MessageType.TEXT);

        message = messageRepository.save(message);

        return messageMapper.toResponse(message);
    }

    @Override
    public SliceResponse<MeetingMessageResponse> getMessages(Long meetingId, int page, int size) {
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException("Cuộc họp không tồn tại"));

        Pageable pageable = PageRequest.of(page, size);
        Slice<MeetingMessage> slice = messageRepository.findByMeetingId(meetingId, pageable);

        return SliceResponse.<MeetingMessageResponse>builder()
                .content(slice.getContent().stream()
                        .map(messageMapper::toResponse)
                        .toList())
                .page(page)
                .size(size)
                .hasNext(slice.hasNext())
                .build();
    }
}

