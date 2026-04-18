package com.example.demo.service.impl;

import com.example.demo.dto.JoinGroupDTO.JoinGroupRequest;
import com.example.demo.dto.JoinGroupDTO.JoinRequestResponse;
import com.example.demo.dto.JoinGroupDTO.ReviewRequest;
import com.example.demo.entity.Group;
import com.example.demo.entity.GroupJoinRequest;
import com.example.demo.entity.GroupMember;
import com.example.demo.entity.User;
import com.example.demo.event.JoinRequestEvent;
import com.example.demo.event.NewMeetingEvent;
import com.example.demo.event.NewMemberEvent;
import com.example.demo.exception.*;
import com.example.demo.repository.GroupJoinRequestRepository;
import com.example.demo.repository.GroupMemberRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.interf.GroupJoinRequestInterface;
import com.example.demo.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class GroupJoinRequestService implements GroupJoinRequestInterface {
    private final GroupRepository groupRepository;
    private final GroupJoinRequestRepository groupJoinRequestRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final GroupWebSocketService groupWebSocketService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public JoinRequestResponse createJoinGroupRequest(JoinGroupRequest joinGroupRequest) {
        Group joinGroup = groupRepository.findByJoinCode(joinGroupRequest.getJoinCode());
        if (joinGroup == null) {
            throw new GroupNotFoundException("Nhóm không tồn tại.");
        }

        Long currentUserId = securityUtil.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại."));

        Long joinGroupId = joinGroup.getId();

        if (groupMemberRepository.existsByUserIdAndGroupId(currentUserId, joinGroupId)) {
            throw new UserIsMemberException("Người dùng đã là thành viên của nhóm đó");
        }

        if (groupJoinRequestRepository.existsByGroupIdAndUserId(joinGroupId, currentUserId)) {
            throw new JoinRequestIsPendingException("Bạn đã gửi yêu cầu tham gia nhóm này rồi.");
        }

        GroupJoinRequest req = new GroupJoinRequest();
        req.setStatus(GroupJoinRequest.Status.PENDING);

        if (joinGroupRequest.getMessage() != null && !joinGroupRequest.getMessage().isBlank()) {
            req.setMessage(joinGroupRequest.getMessage().trim());
        }

        req.setGroup(joinGroup);
        req.setUser(currentUser);

        GroupJoinRequest saved;
        try {
            saved = groupJoinRequestRepository.save(req);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new JoinRequestIsPendingException("Bạn đã gửi yêu cầu tham gia nhóm này rồi.");
        }
        eventPublisher.publishEvent(new JoinRequestEvent(currentUserId,joinGroup.getId(), saved.getId()));

        return JoinRequestResponse.builder()
                .id(saved.getId())
                .groupId(joinGroupId)
                .groupName(joinGroup.getGroupName())
                .userId(currentUser.getId())
                .userName(currentUser.getFullname())
                .userAvatar(currentUser.getAvatarUrl())
                .status(saved.getStatus())
                .message(saved.getMessage())
                .reviewedBy(saved.getReviewedBy())
                .reviewerName(saved.getReviewer() != null ? saved.getReviewer().getFullname() : null)
                .reviewedAt(saved.getReviewedAt())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    public List<JoinRequestResponse> getAllJoinGroupRequest(Long groupId) {
        Long currentUserId = securityUtil.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại."));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Nhóm không tồn tại"));

        Optional<GroupMember> groupMember = groupMemberRepository.findByUserIdAndGroupId(currentUserId,groupId);
        GroupMember checkGroup = groupMember.get();
        if(checkGroup.getRole() != GroupMember.Role.OWNER) {
            throw new UserIsNotOwnerException("Người dùng không phải trưởng nhóm");
        }

        List<GroupJoinRequest> joinRequestList = groupJoinRequestRepository.findByGroupIdAndStatus(groupId,GroupJoinRequest.Status.PENDING);
        return joinRequestList.stream()
                .map(req -> JoinRequestResponse.builder()
                        .id(req.getId())
                        .groupId(groupId)
                        .groupName(req.getGroup() != null ? req.getGroup().getGroupName() : null)
                        .userId(req.getUser() != null ? req.getUser().getId() : null)
                        .userName(req.getUser() != null ? req.getUser().getFullname() : null)
                        .userAvatar(req.getUser() != null ? req.getUser().getAvatarUrl() : null)
                        .status(req.getStatus())
                        .message(req.getMessage())
                        .reviewedBy(req.getReviewedBy())
                        .reviewerName(req.getReviewer() != null ? req.getReviewer().getFullname() : null)
                        .reviewedAt(req.getReviewedAt())
                        .createdAt(req.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public JoinRequestResponse updateStatusJoinRequest(ReviewRequest reviewRequest, Long joinRequestId) {
        Long currentUserId = securityUtil.getCurrentUserId();

        // 1) Lấy join request
        GroupJoinRequest req = groupJoinRequestRepository.findById(joinRequestId)
                .orElseThrow(() -> new JoinRequestNotFoundException("Request vào nhóm không tồn tại"));

        // 2) Lấy groupId an toàn
        Long groupId = (req.getGroup() != null) ? req.getGroup().getId() : req.getGroupId();
        if (groupId == null) {
            throw new GroupNotFoundException("Nhóm không tồn tại");
        }

        // 3) Check OWNER
        GroupMember ownerMember = groupMemberRepository.findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new UserNotInGroupException("Bạn không thuộc nhóm này."));
        if (ownerMember.getRole() != GroupMember.Role.OWNER) {
            throw new UserIsNotOwnerException("Người dùng không phải trưởng nhóm.");
        }

        // 4) Chỉ xử lý khi PENDING
        if (req.getStatus() != GroupJoinRequest.Status.PENDING) {
            throw new RuntimeException("Request này đã được xử lý trước đó.");
        }

        // 5) Validate status đầu vào (chỉ cho APPROVED / REJECTED)
        if (reviewRequest.getStatus() == null || reviewRequest.getStatus() == GroupJoinRequest.Status.PENDING) {
            throw new InvalidRequestException("Trạng thái review không hợp lệ.");
        }

        // 6) Update status + reviewer + reviewedAt
        req.setStatus(reviewRequest.getStatus());
        req.setReviewer(userRepository.getReferenceById(currentUserId));
        req.setReviewedAt(java.time.LocalDateTime.now());

        // 7) Nếu APPROVED -> add member cho người gửi request
        if (reviewRequest.getStatus() == GroupJoinRequest.Status.APPROVED) {
            Long targetUserId = req.getUser() != null ? req.getUser().getId() : req.getUserId();
            if (targetUserId == null) {
                throw new UserNotFoundException("Không xác định được user của request.");
            }

            boolean alreadyMember = groupMemberRepository.existsByUserIdAndGroupId(targetUserId, groupId);
            if (!alreadyMember) {
                GroupMember newMember = new GroupMember();
                newMember.setRole(GroupMember.Role.MEMBER);
                newMember.setIsActive(true);

                // Set relationship (khuyên dùng)
                newMember.setUser(req.getUser());
                newMember.setGroup(req.getGroup());

                groupMemberRepository.save(newMember);

                // Send WebSocket Notification
                groupWebSocketService.notifyUserGroupJoined(targetUserId, groupId, req.getGroup().getGroupName());
            }
        }

        GroupJoinRequest saved = groupJoinRequestRepository.save(req);

        eventPublisher.publishEvent(new NewMemberEvent(currentUserId,req.getGroupId(),req.getUserId()));

        // 8) Map response
        return JoinRequestResponse.builder()
                .id(saved.getId())
                .groupId(groupId)
                .groupName(saved.getGroup() != null ? saved.getGroup().getGroupName() : null)
                .userId(saved.getUser() != null ? saved.getUser().getId() : null)
                .userName(saved.getUser() != null ? saved.getUser().getFullname() : null)
                .userAvatar(saved.getUser() != null ? saved.getUser().getAvatarUrl() : null)
                .status(saved.getStatus())
                .message(saved.getMessage())
                .reviewedBy(saved.getReviewer() != null ? saved.getReviewer().getId() : saved.getReviewedBy())
                .reviewerName(saved.getReviewer() != null ? saved.getReviewer().getFullname() : null)
                .reviewedAt(saved.getReviewedAt())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
