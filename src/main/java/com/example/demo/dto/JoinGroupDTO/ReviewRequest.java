package com.example.demo.dto.JoinGroupDTO;

import com.example.demo.entity.GroupJoinRequest;
import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
public class ReviewRequest {
    private GroupJoinRequest.Status status;
}
