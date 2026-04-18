package com.example.demo.dto.MeetingDTO;

import jakarta.persistence.GeneratedValue;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@Builder
public class MeetingLeaveResponse {
    private Long id;
    private Long userId;
    private Integer sessionIndex;
    private LocalDateTime leftAt;
}
