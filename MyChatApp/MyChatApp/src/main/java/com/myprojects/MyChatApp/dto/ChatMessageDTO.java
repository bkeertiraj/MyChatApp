package com.myprojects.MyChatApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ChatMessageDTO {
    private String content;
    private String sender;
    private String receiver;
    private LocalDateTime timestamp;

    public ChatMessageDTO(String content, String sender, String receiver, LocalDateTime timestamp) {
        this.content = content;
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
    }
}
