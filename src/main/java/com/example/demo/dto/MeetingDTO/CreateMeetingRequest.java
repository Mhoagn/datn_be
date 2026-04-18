package com.example.demo.dto.MeetingDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreateMeetingRequest {
    @NotNull(message = "groupId không được để trống")
    private Long groupId;
}
