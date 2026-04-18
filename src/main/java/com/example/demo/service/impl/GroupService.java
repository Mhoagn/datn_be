package com.example.demo.service.impl;

import com.example.demo.dto.*;
import com.example.demo.dto.GroupDTO.*;
import com.example.demo.entity.Group;
import com.example.demo.entity.GroupMember;
import com.example.demo.entity.User;
import com.example.demo.exception.GroupNotFoundException;
import com.example.demo.exception.UserIsNotOwnerException;
import com.example.demo.exception.UserNotInGroupException;
import com.example.demo.mapper.GroupMapper;
import com.example.demo.repository.GroupMemberRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.interf.GroupInterface;
import com.example.demo.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GroupService implements GroupInterface {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final GroupMapper groupMapper;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public GroupResponse createGroup(GroupRequest groupRequest){
        Long currentUserId = securityUtil.getCurrentUserId();
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        String joinCode = generateUniqueJoinCode();

        Group group = new Group();
        group.setGroupName(groupRequest.getGroupName());
        group.setJoinCode(joinCode);
        group.setCreator(creator);
        group.setIsDeleted(false);

        Group savedGroup = groupRepository.save(group);

        GroupMember owner = new GroupMember();
        owner.setUser(creator);
        owner.setGroup(savedGroup);
        owner.setRole(GroupMember.Role.OWNER);
        owner.setIsActive(true);

        groupMemberRepository.save(owner);

        return groupMapper.toResponse(savedGroup, GroupMember.Role.OWNER, 1);
    }

    @Override
    @Transactional
    public GroupResponse updateGroup(GroupProfileUpdate groupProfileUpdate, Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Nhóm không tồn tại"));

        Long currentUserId = securityUtil.getCurrentUserId();
        
        GroupMember member = groupMemberRepository.findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new UserNotInGroupException("Bạn không phải thành viên của nhóm"));

        if (member.getRole() != GroupMember.Role.OWNER) {
            throw new UserIsNotOwnerException("Chỉ chủ nhóm mới có quyền cập nhật thông tin");
        }

        if (groupProfileUpdate.getGroupName() != null &&
                !groupProfileUpdate.getGroupName().trim().isEmpty()) {
            group.setGroupName(groupProfileUpdate.getGroupName().trim());
        }

        Group updatedGroup = groupRepository.save(group);

        long memberCount = groupMemberRepository.countByGroupIdAndIsActiveTrue(groupId);

        return groupMapper.toResponse(
                updatedGroup,
                member.getRole(),
                (int) memberCount
        );
    }
    
    @Override
    @Transactional
    public GroupResponse updateGroupAvatar(Long groupId, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Nhóm không tồn tại"));

        Long currentUserId = securityUtil.getCurrentUserId();
        
        GroupMember member = groupMemberRepository.findByUserIdAndGroupId(currentUserId, groupId)
                .orElseThrow(() -> new UserNotInGroupException("Bạn không phải thành viên của nhóm"));

        if (member.getRole() != GroupMember.Role.OWNER) {
            throw new UserIsNotOwnerException("Chỉ chủ nhóm mới có quyền cập nhật avatar");
        }

        if (group.getCloudinaryAvatarId() != null) {
            cloudinaryService.deleteImage(group.getCloudinaryAvatarId());
        }

        Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, "group-avatars");

        group.setAvatarUrl((String) uploadResult.get("secure_url"));
        group.setCloudinaryAvatarId((String) uploadResult.get("public_id"));

        Group updatedGroup = groupRepository.save(group);

        long memberCount = groupMemberRepository.countByGroupIdAndIsActiveTrue(groupId);

        return groupMapper.toResponse(
                updatedGroup,
                member.getRole(),
                (int) memberCount
        );
    }


    @Override
    public SliceResponse<MyGroupResponse> getMyGroups(int page, int size, String search) {
        Long currentUserId = securityUtil.getCurrentUserId();

        Pageable pageable = PageRequest.of(page, size);
        Slice<GroupMember> membershipSlice;
        
        if (search != null && !search.trim().isEmpty()) {
            membershipSlice = groupMemberRepository.findByUserIdAndGroupName(currentUserId, search.trim(), pageable);
        } else {
            membershipSlice = groupMemberRepository.findAllByUserIdAndIsActiveTrue(currentUserId, pageable);
        }

        List<MyGroupResponse> content = membershipSlice.getContent().stream()
                .map(membership -> {
                    Group group = membership.getGroup();
                    return MyGroupResponse.builder()
                            .id(group.getId())
                            .groupName(group.getGroupName())
                            .avatarUrl(group.getAvatarUrl())
                            .role(membership.getRole().name())
                            .build();
                })
                .collect(Collectors.toList());

        return SliceResponse.<MyGroupResponse>builder()
                .content(content)
                .page(membershipSlice.getNumber())
                .size(membershipSlice.getSize())
                .hasNext(membershipSlice.hasNext())
                .build();
    }

    @Override
    public JoinCodeResponse getJoinCode(Long groupId){
        Long currentUserId = securityUtil.getCurrentUserId();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Nhóm không tồn tại"));
        Optional<GroupMember> groupMember = groupMemberRepository.findByUserIdAndGroupId(currentUserId,groupId);
        if(groupMember.isEmpty()) {

        }
        return JoinCodeResponse.builder().joinCode(group.getJoinCode()).build();

    }

    private String generateUniqueJoinCode() {
        String joinCode;
        do {
            joinCode = generateRandomCode(8);
        } while (groupRepository.existsByJoinCode(joinCode));

        return joinCode;
    }

    private String generateRandomCode(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }

        return code.toString();
    }
}
