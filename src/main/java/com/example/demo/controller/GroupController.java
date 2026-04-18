package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.dto.GroupDTO.*;
import com.example.demo.service.interf.GroupInterface;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/group")
@AllArgsConstructor
public class GroupController {
    private final GroupInterface groupService;


    @GetMapping("/my-groups")
    public ResponseEntity<SliceResponse<MyGroupResponse>> getMyGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(groupService.getMyGroups(page, size, search));
    }


    @GetMapping("/join-code/{groupId}")
    public ResponseEntity<JoinCodeResponse> getJoinCodeInfo(@PathVariable Long groupId) {
        JoinCodeResponse result = groupService.getJoinCode(groupId);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @RequestBody @Valid GroupRequest request
    ) {
        GroupResponse response = groupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<GroupResponse> updateGroupInfo(
            @PathVariable Long groupId,
            @RequestBody @Valid GroupProfileUpdate request
    ) {
        GroupResponse response = groupService.updateGroup(request, groupId);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{groupId}/avatar")
    public ResponseEntity<GroupResponse> updateGroupAvatar(
            @PathVariable Long groupId,
            @RequestParam("file") MultipartFile file
    ) throws java.io.IOException {
        GroupResponse response = groupService.updateGroupAvatar(groupId, file);
        return ResponseEntity.ok(response);
    }
}
