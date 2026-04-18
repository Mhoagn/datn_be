package com.example.demo.dto.conversationDTO;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrGetConversationResult {
    private ConversationResponse conversationResponse;
    private boolean isNew;
}
