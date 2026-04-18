package com.example.demo.service.interf;

import com.example.demo.dto.SliceResponse;
import com.example.demo.dto.conversationDTO.ConversationResponse;
import com.example.demo.dto.conversationDTO.CreateOrGetConversationResult;
import com.example.demo.dto.messageDTO.MessageResponse;

public interface ConversationInterface {
    CreateOrGetConversationResult createOrGetConversation(String targetEmail);

    SliceResponse<ConversationResponse> getConversations(int page, int size);

}
