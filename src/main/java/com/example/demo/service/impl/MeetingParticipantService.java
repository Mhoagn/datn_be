package com.example.demo.service.impl;

import com.example.demo.dto.MeetingParticipantDTO.MeetingParticipantResponse;
import com.example.demo.dto.SliceResponse;
import com.example.demo.entity.Meeting;
import com.example.demo.entity.MeetingParticipant;
import com.example.demo.exception.MeetingNotFoundException;
import com.example.demo.exception.UserNotInMeetingException;
import com.example.demo.mapper.MeetingParticipantMapper;
import com.example.demo.repository.GroupMemberRepository;
import com.example.demo.repository.MeetingParticipantRepository;
import com.example.demo.repository.MeetingRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.interf.MeetingParticipantInterface;
import com.example.demo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingParticipantService implements MeetingParticipantInterface {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MeetingParticipantMapper meetingParticipantMapper;
    private final SecurityUtil securityUtil;

    @Override
    public SliceResponse<MeetingParticipantResponse> getParticipants(Long meetingId, int page, int size) {
        Long currentUserId = securityUtil.getCurrentUserId();

        // 1. Validate meeting tồn tại và đang ONGOING
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException("Cuộc họp không tồn tại"));

        if (meeting.getStatus() != Meeting.Status.ONGOING) {
            throw new MeetingNotFoundException("Cuộc họp không tồn tại");
        }

        // 2. Validate user đang active trong meeting này
        meetingParticipantRepository
                .findActiveSession(currentUserId, meetingId)
                .orElseThrow(() -> new UserNotInMeetingException("Người dùng chưa tham gia cuộc họp"));

        // 3. Query danh sách participant còn active
        Pageable pageable = PageRequest.of(page, size);
        Slice<MeetingParticipant> slice =
                meetingParticipantRepository.findActiveParticipants(meetingId, pageable);

        // 4. Map và trả về SliceResponse
        return SliceResponse.<MeetingParticipantResponse>builder()
                .content(slice.getContent().stream()
                        .map(meetingParticipantMapper::toResponse)
                        .toList())
                .page(page)
                .size(size)
                .hasNext(slice.hasNext())
                .build();
    }
}
