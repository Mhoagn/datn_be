package com.example.demo.dto.GroupDTO;

import com.example.demo.entity.GroupMember;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupResponse {
    private Long id;
    private String groupName;
    private String avatarUrl;
    private String joinCode;
    private Long createdBy;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Thông tin bổ sung
    private Integer memberCount; // Số lượng thành viên
    private GroupMember.Role currentUserRole; // Role của user hiện tại
}
