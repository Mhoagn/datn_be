package com.example.demo.dto.messageDTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReadMessageRequest {
    private Long conversationId;
}
