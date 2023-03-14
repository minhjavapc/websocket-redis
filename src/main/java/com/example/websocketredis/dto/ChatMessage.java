package com.example.websocketredis.dto;

import com.example.websocketredis.enumf.MessageType;
import lombok.Data;

@Data
public class ChatMessage {
    private String content;
    private String sender;
    private MessageType type;

    // constructors, getters and setters
}