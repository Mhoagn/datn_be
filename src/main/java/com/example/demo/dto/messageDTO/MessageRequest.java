package com.example.demo.dto.messageDTO;

import com.example.demo.entity.Message;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequest {
    private String content;
    private String attachmentUrl;
    private String attachmentPublicId;
    private String attachmentName;
    private Long attachmentSize;
    private Message.MessageType messageType = Message.MessageType.TEXT;
}
