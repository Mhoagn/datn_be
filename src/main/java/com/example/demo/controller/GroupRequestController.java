package com.example.demo.controller;

import com.example.demo.dto.JoinGroupDTO.JoinGroupRequest;
import com.example.demo.dto.JoinGroupDTO.JoinRequestResponse;
import com.example.demo.dto.JoinGroupDTO.ReviewRequest;
import com.example.demo.service.interf.GroupJoinRequestInterface;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("group-request")
@AllArgsConstructor
public class GroupRequestController {
    private GroupJoinRequestInterface groupJoinRequestInterface;

    @PostMapping
    public ResponseEntity<JoinRequestResponse> createJoinRequest(@RequestBody JoinGroupRequest joinGroupRequest){
        JoinRequestResponse joinRequestResponse = groupJoinRequestInterface.createJoinGroupRequest(joinGroupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(joinRequestResponse);
    }

    @GetMapping("{groupId}")
    public ResponseEntity<List<JoinRequestResponse>> getAllJoinRequest(@PathVariable Long groupId){
        List<JoinRequestResponse> joinRequestResponseList = groupJoinRequestInterface.getAllJoinGroupRequest(groupId);
        return ResponseEntity.ok(joinRequestResponseList);
    }

    @PatchMapping("{requestId}")
    public ResponseEntity<JoinRequestResponse> updateJoinRequestStatus(@PathVariable Long requestId,
                                                                       @RequestBody ReviewRequest reviewRequest){
        JoinRequestResponse joinRequestResponse = groupJoinRequestInterface.updateStatusJoinRequest(reviewRequest,requestId);
        return ResponseEntity.ok(joinRequestResponse);
    }
}
