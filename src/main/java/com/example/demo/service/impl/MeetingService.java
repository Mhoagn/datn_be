package com.example.demo.service.impl;

import com.example.demo.dto.MeetingDTO.*;
import com.example.demo.dto.MeetingRecordDTO.RecordResponse;
import com.example.demo.dto.MeetingRecordDTO.RecordStopResponse;
import com.example.demo.dto.SliceResponse;
import com.example.demo.entity.*;
import com.example.demo.event.NewMeetingEvent;
import com.example.demo.exception.*;
import com.example.demo.mapper.MeetingMapper;
import com.example.demo.mapper.MeetingRecordMapper;
import com.example.demo.repository.*;
import com.example.demo.service.interf.MeetingInterface;
import com.example.demo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingService implements MeetingInterface {
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MeetingRecordRepository meetingRecordRepository;
    private final LiveKitService liveKitService;
    private final MeetingMapper meetingMapper;
    private final MeetingRecordMapper meetingRecordMapper;
    private final SecurityUtil securityUtil;
    private final ApplicationEventPublisher eventPublisher;
    private final TranscriptService transcriptService;
    private final GroupWebSocketService groupWebSocketService;

    @Value("${livekit.s3.bucket}")
    private String s3Bucket;

    @Value("${livekit.s3.region}")
    private String s3Region;

    @Override
    @Transactional
    public MeetingResponse createMeeting(CreateMeetingRequest request) {
        Long currentUserId = securityUtil.getCurrentUserId();

        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException("Nhóm không tồn tại"));

        GroupMember findMember = groupMemberRepository
                .findByUserIdAndGroupId(currentUserId, request.getGroupId())
                .orElseThrow(() -> new UserNotInGroupException("Người dùng không ở trong nhóm"));

        if (!findMember.getIsActive()) {
            throw new UserNotInGroupException("Người dùng không còn ở trong nhóm");
        }

        if (participantRepository.existsActiveSessionByUserId(currentUserId)) {
            throw new UserAlreadyInMeetingException("Người dùng đang trong một cuộc họp khác");
        }

        String roomName = liveKitService.generateRoomName(request.getGroupId());

        Meeting meeting = new Meeting();
        meeting.setGroup(group);
        meeting.setCreator(creator);
        meeting.setLiveKitRoomName(roomName);
        meeting.setStatus(Meeting.Status.ONGOING);
        meeting = meetingRepository.save(meeting);

        MeetingParticipant host = new MeetingParticipant();
        host.setMeeting(meeting);
        host.setUser(creator);
        host.setLiveKitIdentity("user-" + currentUserId);
        host.setRole(MeetingParticipant.Role.HOST);
        host.setSessionIndex(1);
        participantRepository.save(host);

        eventPublisher.publishEvent(new NewMeetingEvent(currentUserId, request.getGroupId(), meeting.getId()));

        MeetingResponse response = meetingMapper.toResponse(meeting);
        
        // Broadcast meeting mới đến tất cả members đang xem group
        groupWebSocketService.broadcastNewMeeting(request.getGroupId(), response);

        return response;
    }

    @Override
    public SliceResponse<MeetingResponse> getMeetings(Long groupId, int page, int size) {
        Long currentUserId = securityUtil.getCurrentUserId();

        groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Nhóm không tồn tại"));

        GroupMember member = groupMemberRepository
                .findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new UserNotInGroupException("Người dùng không ở trong nhóm"));

        if (!member.getIsActive()) {
            throw new UserNotInGroupException("Người dùng không còn ở trong nhóm");
        }

        Pageable pageable = PageRequest.of(page, size);
        Slice<Meeting> slice = meetingRepository.findByGroupId(groupId, pageable);

        return SliceResponse.<MeetingResponse>builder()
                .content(slice.getContent().stream()
                        .map(meetingMapper::toResponse)
                        .toList())
                .page(page)
                .size(size)
                .hasNext(slice.hasNext())
                .build();
    }

    @Override
    @Transactional
    public MeetingJoinResponse joinMeeting(Long meetingId) {
        Long currentUserId = securityUtil.getCurrentUserId();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException("Cuộc họp không tồn tại"));

        if (meeting.getStatus() != Meeting.Status.ONGOING) {
            throw new MeetingNotFoundException("Cuộc họp không tồn tại");
        }

        Long groupId = meeting.getGroup().getId();
        GroupMember member = groupMemberRepository
                .findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new UserNotInGroupException("Người dùng không ở trong nhóm"));

        if (!member.getIsActive()) {
            throw new UserNotInGroupException("Người dùng không còn ở trong nhóm");
        }

        // Re-join: user đã có session active trong meeting này
        if (participantRepository.existsActiveSessionInMeeting(currentUserId, meetingId)) {
            MeetingParticipant activeSession = participantRepository
                    .findActiveSession(currentUserId, meetingId).orElseThrow();
            boolean isHost = activeSession.getRole() == MeetingParticipant.Role.HOST;
            String token = liveKitService.generateToken(
                    meeting.getLiveKitRoomName(), activeSession.getLiveKitIdentity(), isHost);

            return MeetingJoinResponse.builder()
                    .id(activeSession.getId())
                    .userId(currentUserId)
                    .sessionIndex(activeSession.getSessionIndex())
                    .joinedAt(activeSession.getJoinedAt())
                    .serverUrl(liveKitService.getWsUrl())
                    .participantToken(token)
                    .build();
        }

        if (participantRepository.existsActiveSessionByUserId(currentUserId)) {
            throw new UserAlreadyInMeetingException("Người dùng đang trong một cuộc họp khác");
        }

        int nextSession = participantRepository.findMaxSessionIndex(currentUserId, meetingId) + 1;
        String identity = "user-" + currentUserId + "-s" + nextSession;
        boolean isHost = meeting.getCreator().getId().equals(currentUserId);
        String token = liveKitService.generateToken(meeting.getLiveKitRoomName(), identity, isHost);

        MeetingParticipant participant = new MeetingParticipant();
        participant.setMeeting(meeting);
        participant.setUser(user);
        participant.setLiveKitIdentity(identity);
        participant.setRole(isHost ? MeetingParticipant.Role.HOST : MeetingParticipant.Role.PARTICIPANT);
        participant.setSessionIndex(nextSession);
        participant = participantRepository.save(participant);

        return MeetingJoinResponse.builder()
                .id(participant.getId())
                .userId(currentUserId)
                .sessionIndex(participant.getSessionIndex())
                .joinedAt(participant.getJoinedAt())
                .serverUrl(liveKitService.getWsUrl())
                .participantToken(token)
                .build();
    }

    @Override
    @Transactional
    public MeetingLeaveResponse leaveMeeting(Long meetingId) {
        Long currentUserId = securityUtil.getCurrentUserId();

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException("Cuộc họp không tồn tại"));

        if (meeting.getStatus() != Meeting.Status.ONGOING) {
            throw new MeetingNotFoundException("Cuộc họp không tồn tại");
        }

        MeetingParticipant participant = participantRepository
                .findActiveSession(currentUserId, meetingId)
                .orElseThrow(() -> new UserNotInMeetingException("Người dùng chưa tham gia cuộc họp"));

        // HOST leave → tự động end meeting luôn
        if (participant.getRole() == MeetingParticipant.Role.HOST) {
            endMeeting(meetingId);
            return MeetingLeaveResponse.builder()
                    .id(participant.getId())
                    .userId(currentUserId)
                    .sessionIndex(participant.getSessionIndex())
                    .leftAt(LocalDateTime.now())
                    .build();
        }

        LocalDateTime leftAt = LocalDateTime.now();
        participant.setLeftAt(leftAt);
        participant.setDurationSeconds((int) Duration.between(participant.getJoinedAt(), leftAt).getSeconds());
        participantRepository.save(participant);

        return MeetingLeaveResponse.builder()
                .id(participant.getId())
                .userId(currentUserId)
                .sessionIndex(participant.getSessionIndex())
                .leftAt(participant.getLeftAt())
                .build();
    }

    @Override
    @Transactional
    public MeetingEndResponse endMeeting(Long meetingId) {
        Long currentUserId = securityUtil.getCurrentUserId();

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException("Cuộc họp không tồn tại"));

        if (meeting.getStatus() != Meeting.Status.ONGOING) {
            throw new MeetingNotFoundException("Cuộc họp đã kết thúc hoặc không còn diễn ra");
        }

        MeetingParticipant participant = participantRepository
                .findActiveSession(currentUserId, meetingId)
                .orElseThrow(() -> new UserNotInMeetingException("Người dùng chưa tham gia cuộc họp"));

        if (participant.getRole() != MeetingParticipant.Role.HOST) {
            throw new UserIsNotHostMeetingException("Chỉ host mới có quyền kết thúc cuộc họp");
        }

        LocalDateTime endedAt = LocalDateTime.now();
        List<MeetingParticipant> activeParticipants =
                participantRepository.findAllActiveByMeetingId(meetingId);

        activeParticipants.forEach(p -> {
            p.setLeftAt(endedAt);
            p.setDurationSeconds((int) Duration.between(p.getJoinedAt(), endedAt).getSeconds());
        });
        participantRepository.saveAll(activeParticipants);

        meeting.setStatus(Meeting.Status.END);
        meeting.setEndedAt(endedAt);
        meeting = meetingRepository.save(meeting);

        liveKitService.deleteRoom(meeting.getLiveKitRoomName());

        MeetingEndResponse response = meetingMapper.toEndResponse(meeting);
        
        // Broadcast meeting status update đến tất cả members đang xem group
        MeetingResponse updatedMeeting = meetingMapper.toResponse(meeting);
        groupWebSocketService.broadcastMeetingUpdate(meeting.getGroup().getId(), updatedMeeting);

        return response;
    }

    @Override
    @Transactional
    public RecordResponse startRecord(Long meetingId) {
        Long currentUserId = securityUtil.getCurrentUserId();

        // 1. Validate meeting tồn tại và đang ONGOING
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException("Cuộc họp không tồn tại"));

        if (meeting.getStatus() != Meeting.Status.ONGOING) {
            throw new MeetingNotFoundException("Cuộc họp không tồn tại");
        }

        // 2. Validate user đang active trong meeting
        participantRepository.findActiveSession(currentUserId, meetingId)
                .orElseThrow(() -> new UserNotInMeetingException("Người dùng chưa tham gia cuộc họp"));

        // 3. Kiểm tra meeting đang có record active chưa
        if (meetingRecordRepository.existsActiveRecordByMeetingId(meetingId)) {
            throw new RecordAlreadyActiveException("Cuộc họp đang được record bởi người khác");
        }

        // 4. Tạo fileName unique
        String fileName = "meeting-" + meetingId + "-" + System.currentTimeMillis() + ".mp4";

        // 5. Gọi LiveKit Egress bắt đầu record
        String egressId = liveKitService.startRecord(meeting.getLiveKitRoomName(), fileName);

        // 6. Lưu MeetingRecord vào DB
        MeetingRecord record = new MeetingRecord();
        record.setMeeting(meeting);
        record.setRecordedBy(currentUserId);
        record.setEgressId(egressId);
        record.setFileName(fileName);
        record.setStatus(MeetingRecord.Status.PROCESSING);

        record = meetingRecordRepository.save(record);

        // 7. Trả về response
        return meetingRecordMapper.toRecordResponse(record);
    }

    @Override
    @Transactional
    public RecordStopResponse stopRecord(Long meetingId) {
        Long currentUserId = securityUtil.getCurrentUserId();

        // 1. Validate meeting tồn tại và đang ONGOING
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException("Cuộc họp không tồn tại"));

        if (meeting.getStatus() != Meeting.Status.ONGOING) {
            throw new MeetingNotFoundException("Cuộc họp không tồn tại");
        }

        // 2. Validate user đang active trong meeting
        participantRepository.findActiveSession(currentUserId, meetingId)
                .orElseThrow(() -> new UserNotInMeetingException("Người dùng chưa tham gia cuộc họp"));

        // 3. Lấy record đang PROCESSING
        MeetingRecord record = meetingRecordRepository.findActiveRecordByMeetingId(meetingId)
                .orElseThrow(() -> new RecordNotFoundException("Không có record nào đang chạy"));

        // 4. Chỉ người bắt đầu record mới được stop
        if (!record.getRecordedBy().equals(currentUserId)) {
            throw new UserCannotStopRecordException("Bạn không có quyền dừng record này");
        }

        // 5. Gọi LiveKit dừng egress
        liveKitService.stopRecord(record.getEgressId());

        // 6. Tính durationSeconds từ createdAt đến now
        int durationSeconds = (int) Duration.between(record.getCreatedAt(), LocalDateTime.now()).getSeconds();

        // 7. Build storageUrl từ s3Bucket + fileName
        // Sau khi stop, LiveKit sẽ upload file lên S3 theo fileName đã set lúc start
        // storageUrl có dạng: https://<bucket>.s3.<region>.amazonaws.com/<fileName>
        String storageUrl = "https://" + s3Bucket + ".s3." + s3Region
                + ".amazonaws.com/" + record.getFileName();

        // 8. Cập nhật MeetingRecord (tạm thời set COMPLETED, webhook sẽ confirm sau)
        // Nếu webhook đã xử lý trước (COMPLETED/FAILED) thì giữ nguyên status đó
        if (record.getStatus() == MeetingRecord.Status.PROCESSING) {
            record.setStatus(MeetingRecord.Status.COMPLETED);
            record.setStorageUrl(storageUrl);
            record.setDurationSeconds(durationSeconds);
            record.setS3Key(record.getFileName());
            record.setS3Bucket(s3Bucket);
        }

        record = meetingRecordRepository.save(record);

        // 9. Gọi AI service để xử lý video (async) - chỉ khi COMPLETED
        if (record.getStatus() == MeetingRecord.Status.COMPLETED) {
            transcriptService.processRecordedVideo(record.getId());
        }

        // 10. Trả về response
        return meetingRecordMapper.toRecordStopResponse(record);
    }

    @Override
    public SliceResponse<com.example.demo.dto.MeetingRecordDTO.RecordListResponse> getRecordsByGroupId(Long groupId, int page, int size) {
        Long currentUserId = securityUtil.getCurrentUserId();

        // 1. Validate group tồn tại
        groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Nhóm không tồn tại"));

        // 2. Validate user là member của group
        GroupMember member = groupMemberRepository
                .findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new UserNotInGroupException("Người dùng không ở trong nhóm"));

        if (!member.getIsActive()) {
            throw new UserNotInGroupException("Người dùng không còn ở trong nhóm");
        }

        // 3. Lấy danh sách records với phân trang
        Pageable pageable = PageRequest.of(page, size);
        Slice<MeetingRecord> slice = meetingRecordRepository.findByGroupId(groupId, pageable);

        // 4. Map sang DTO
        return SliceResponse.<com.example.demo.dto.MeetingRecordDTO.RecordListResponse>builder()
                .content(slice.getContent().stream()
                        .map(meetingRecordMapper::toRecordListResponse)
                        .toList())
                .page(page)
                .size(size)
                .hasNext(slice.hasNext())
                .build();
    }
}
