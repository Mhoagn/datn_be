package com.example.demo.dto.GroupDTO;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveGroupResponse {
    private Long groupId;
    private Long userId;
    private String message;
    private LocalDateTime leftAt;
}
