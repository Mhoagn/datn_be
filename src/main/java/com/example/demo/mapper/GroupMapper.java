package com.example.demo.mapper;

import com.example.demo.dto.GroupDTO.GroupResponse;
import com.example.demo.entity.Group;
import com.example.demo.entity.GroupMember;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

    public GroupResponse toResponse(Group group, GroupMember.Role currentUserRole, Integer memberCount) {
        return GroupResponse.builder()
                .id(group.getId())
                .groupName(group.getGroupName())
                .avatarUrl(group.getAvatarUrl())
                .joinCode(group.getJoinCode())
                .createdBy(group.getCreator() != null ? group.getCreator().getId() : null)
                .isDeleted(group.getIsDeleted())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .memberCount(memberCount)
                .currentUserRole(currentUserRole)
                .build();
    }

    // Overload cho trường hợp đơn giản
    public GroupResponse toResponse(Group group) {
        return GroupResponse.builder()
                .id(group.getId())
                .groupName(group.getGroupName())
                .avatarUrl(group.getAvatarUrl())
                .joinCode(group.getJoinCode())
                .createdBy(group.getCreator() != null ? group.getCreator().getId() : null)
                .isDeleted(group.getIsDeleted())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }
}