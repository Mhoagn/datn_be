package com.example.demo.controller;

import com.example.demo.dto.MeetingDTO.*;
import com.example.demo.entity.Meeting;
import com.example.demo.service.interf.MeetingInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("Meeting Controller Tests")
class MeetingControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MeetingInterface meetingService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ================ POST /api/meetings (Create Meeting) Tests ================

    @Test
    @DisplayName("TC-MEET-001: Tạo meeting thành công")
    @WithMockUser
    void testCreateMeeting_Success() throws Exception {
        CreateMeetingRequest request = CreateMeetingRequest.builder()
                .groupId(1L)
                .build();

        MeetingResponse response = MeetingResponse.builder()
                .id(1L)
                .groupId(1L)
                .createdBy(1L)
                .startedAt(LocalDateTime.now())
                .status(Meeting.Status.ONGOING.name())
                .liveKitRoomName("room-12345")
                .build();

        when(meetingService.createMeeting(any(CreateMeetingRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/meetings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.groupId").value(1))
                .andExpect(jsonPath("$.status").value("ONGOING"))
                .andExpect(jsonPath("$.liveKitRoomName").value("room-12345"));
    }

    @Test
    @DisplayName("TC-MEET-002: Tạo meeting thất bại - Group không tồn tại")
    @WithMockUser
    void testCreateMeeting_GroupNotFound() throws Exception {
        CreateMeetingRequest request = CreateMeetingRequest.builder()
                .groupId(999L)
                .build();

        when(meetingService.createMeeting(any(CreateMeetingRequest.class)))
                .thenThrow(new com.example.demo.exception.GroupNotFoundException("Nhóm không tồn tại"));

        mockMvc.perform(post("/api/meetings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("TC-MEET-003: Tạo meeting thất bại - Không phải member của group")
    @WithMockUser
    void testCreateMeeting_NotGroupMember() throws Exception {
        CreateMeetingRequest request = CreateMeetingRequest.builder()
                .groupId(1L)
                .build();

        when(meetingService.createMeeting(any(CreateMeetingRequest.class)))
                .thenThrow(new com.example.demo.exception.UserNotInGroupException("Bạn không phải thành viên của nhóm"));

        mockMvc.perform(post("/api/meetings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-MEET-004: Tạo meeting thất bại - GroupId null")
    @WithMockUser
    void testCreateMeeting_NullGroupId() throws Exception {
        CreateMeetingRequest request = CreateMeetingRequest.builder()
                .groupId(null)
                .build();

        mockMvc.perform(post("/api/meetings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ================ POST /api/meetings/{meetingId}/join Tests ================

    @Test
    @DisplayName("TC-MEET-005: Join meeting thành công")
    @WithMockUser
    void testJoinMeeting_Success() throws Exception {
        Long meetingId = 1L;
        MeetingJoinResponse response = MeetingJoinResponse.builder()
                .id(1L)
                .userId(1L)
                .sessionIndex(1)
                .joinedAt(LocalDateTime.now())
                .serverUrl("wss://livekit.example.com")
                .participantToken("mock-participant-token")
                .build();

        when(meetingService.joinMeeting(eq(meetingId))).thenReturn(response);

        mockMvc.perform(post("/api/meetings/{meetingId}/join", meetingId)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.sessionIndex").value(1))
                .andExpect(jsonPath("$.participantToken").value("mock-participant-token"))
                .andExpect(jsonPath("$.serverUrl").value("wss://livekit.example.com"));
    }

    @Test
    @DisplayName("TC-MEET-006: Join meeting thất bại - Meeting không tồn tại")
    @WithMockUser
    void testJoinMeeting_MeetingNotFound() throws Exception {
        Long meetingId = 999L;

        when(meetingService.joinMeeting(eq(meetingId)))
                .thenThrow(new com.example.demo.exception.MeetingNotFoundException("Meeting không tồn tại"));

        mockMvc.perform(post("/api/meetings/{meetingId}/join", meetingId)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("TC-MEET-007: Join meeting thất bại - Meeting đã kết thúc")
    @WithMockUser
    void testJoinMeeting_MeetingEnded() throws Exception {
        Long meetingId = 1L;

        when(meetingService.joinMeeting(eq(meetingId)))
                .thenThrow(new com.example.demo.exception.InvalidRequestException("Meeting đã kết thúc"));

        mockMvc.perform(post("/api/meetings/{meetingId}/join", meetingId)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-MEET-008: Join meeting thất bại - Đã tham gia meeting")
    @WithMockUser
    void testJoinMeeting_AlreadyJoined() throws Exception {
        Long meetingId = 1L;

        when(meetingService.joinMeeting(eq(meetingId)))
                .thenThrow(new com.example.demo.exception.UserAlreadyInMeetingException("Bạn đã tham gia meeting này"));

        mockMvc.perform(post("/api/meetings/{meetingId}/join", meetingId)
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("TC-MEET-009: Join meeting thất bại - Không phải member của group")
    @WithMockUser
    void testJoinMeeting_NotGroupMember() throws Exception {
        Long meetingId = 1L;

        when(meetingService.joinMeeting(eq(meetingId)))
                .thenThrow(new com.example.demo.exception.UserNotInGroupException("Bạn không phải thành viên của nhóm"));

        mockMvc.perform(post("/api/meetings/{meetingId}/join", meetingId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ================ POST /api/meetings/{meetingId}/leave Tests ================

    @Test
    @DisplayName("TC-MEET-010: Leave meeting thành công")
    @WithMockUser
    void testLeaveMeeting_Success() throws Exception {
        Long meetingId = 1L;
        MeetingLeaveResponse response = MeetingLeaveResponse.builder()
                .id(1L)
                .userId(1L)
                .sessionIndex(1)
                .leftAt(LocalDateTime.now())
                .build();

        when(meetingService.leaveMeeting(eq(meetingId))).thenReturn(response);

        mockMvc.perform(post("/api/meetings/{meetingId}/leave", meetingId)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.sessionIndex").value(1))
                .andExpect(jsonPath("$.leftAt").exists());
    }

    @Test
    @DisplayName("TC-MEET-011: Leave meeting thất bại - Chưa join meeting")
    @WithMockUser
    void testLeaveMeeting_NotJoined() throws Exception {
        Long meetingId = 1L;

        when(meetingService.leaveMeeting(eq(meetingId)))
                .thenThrow(new com.example.demo.exception.UserNotInMeetingException("Bạn chưa tham gia meeting này"));

        mockMvc.perform(post("/api/meetings/{meetingId}/leave", meetingId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-MEET-012: Leave meeting thất bại - Meeting không tồn tại")
    @WithMockUser
    void testLeaveMeeting_MeetingNotFound() throws Exception {
        Long meetingId = 999L;

        when(meetingService.leaveMeeting(eq(meetingId)))
                .thenThrow(new com.example.demo.exception.MeetingNotFoundException("Meeting không tồn tại"));

        mockMvc.perform(post("/api/meetings/{meetingId}/leave", meetingId)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ================ POST /api/meetings/{meetingId}/end Tests ================

    @Test
    @DisplayName("TC-MEET-013: End meeting thành công bởi host")
    @WithMockUser
    void testEndMeeting_Success() throws Exception {
        Long meetingId = 1L;
        MeetingEndResponse response = MeetingEndResponse.builder()
                .id(1L)
                .groupId(1L)
                .createdBy(1L)
                .startedAt(LocalDateTime.now().minusHours(1))
                .status(Meeting.Status.END.name())
                .endedAt(LocalDateTime.now())
                .build();

        when(meetingService.endMeeting(eq(meetingId))).thenReturn(response);

        mockMvc.perform(post("/api/meetings/{meetingId}/end", meetingId)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("END"))
                .andExpect(jsonPath("$.endedAt").exists());
    }

    @Test
    @DisplayName("TC-MEET-014: End meeting thất bại - Không phải host")
    @WithMockUser
    void testEndMeeting_NotHost() throws Exception {
        Long meetingId = 1L;

        when(meetingService.endMeeting(eq(meetingId)))
                .thenThrow(new com.example.demo.exception.UserIsNotHostMeetingException("Chỉ host mới có thể kết thúc meeting"));

        mockMvc.perform(post("/api/meetings/{meetingId}/end", meetingId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-MEET-015: End meeting thất bại - Meeting đã ended")
    @WithMockUser
    void testEndMeeting_AlreadyEnded() throws Exception {
        Long meetingId = 1L;

        when(meetingService.endMeeting(eq(meetingId)))
                .thenThrow(new com.example.demo.exception.InvalidRequestException("Meeting đã kết thúc"));

        mockMvc.perform(post("/api/meetings/{meetingId}/end", meetingId)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-MEET-016: End meeting thất bại - Meeting không tồn tại")
    @WithMockUser
    void testEndMeeting_MeetingNotFound() throws Exception {
        Long meetingId = 999L;

        when(meetingService.endMeeting(eq(meetingId)))
                .thenThrow(new com.example.demo.exception.MeetingNotFoundException("Meeting không tồn tại"));

        mockMvc.perform(post("/api/meetings/{meetingId}/end", meetingId)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
