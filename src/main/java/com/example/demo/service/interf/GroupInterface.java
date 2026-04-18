package com.example.demo.service.interf;

import com.example.demo.dto.*;
import com.example.demo.dto.GroupDTO.*;

public interface GroupInterface {
    GroupResponse createGroup(GroupRequest groupRequest);
    GroupResponse updateGroup(GroupProfileUpdate groupProfileUpdate, Long groupId);
    GroupResponse updateGroupAvatar(Long groupId, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException;
    SliceResponse<MyGroupResponse> getMyGroups(int page, int size, String search);
    JoinCodeResponse getJoinCode(Long groudId);
}
