package com.example.demo.service.interf;

import com.example.demo.dto.JoinGroupDTO.JoinGroupRequest;
import com.example.demo.dto.JoinGroupDTO.JoinRequestResponse;
import com.example.demo.dto.JoinGroupDTO.ReviewRequest;

import java.util.List;

public interface GroupJoinRequestInterface {
    JoinRequestResponse createJoinGroupRequest(JoinGroupRequest joinGroupRequest);
    List<JoinRequestResponse> getAllJoinGroupRequest(Long groupId);
    JoinRequestResponse updateStatusJoinRequest(ReviewRequest reviewRequest,Long requestId);
}
