package com.myprojects.MyChatApp.dto;

import com.myprojects.MyChatApp.model.Users;

import java.util.List;

public class ConversationDTO {
    private Users user;
    private List<ChatMessageDTO> messages;

    // Constructors, getters, and setters

    public ConversationDTO(Users user, List<ChatMessageDTO> messages) {
        this.user = user;
        this.messages = messages;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public List<ChatMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessageDTO> messages) {
        this.messages = messages;
    }
}