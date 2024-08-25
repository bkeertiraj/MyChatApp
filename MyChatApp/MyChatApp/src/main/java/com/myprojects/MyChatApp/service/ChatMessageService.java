package com.myprojects.MyChatApp.service;

import com.myprojects.MyChatApp.dto.ChatMessageDTO;
import com.myprojects.MyChatApp.dto.ConversationDTO;
import com.myprojects.MyChatApp.model.ChatMessages;
import com.myprojects.MyChatApp.model.Users;
import com.myprojects.MyChatApp.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository repo;
//
//    public void saveMessage(ChatMessageDTO message) {
//        ChatMessages msg = new ChatMessages();
//        msg.setContent(message.getContent());
//        msg.setSender(message.getSender());
//        msg.setReceiver(message.getReceiver());
//        msg.setTimestamp(LocalDateTime.now());
//        repo.save(msg);
//    }

    public void sendMessage(ChatMessageDTO chatMessageDTO) {
        ChatMessages message = new ChatMessages();
        message.setSender(chatMessageDTO.getSender());
        message.setReceiver(chatMessageDTO.getReceiver());
        message.setContent(chatMessageDTO.getContent());
        message.setTimestamp(LocalDateTime.now());
        repo.save(message);
    }

    public List<ChatMessageDTO> getMessagesForReceiver(String receiver) {
        List<ChatMessages> messages = repo.findByReceiver(receiver);
        return messages.stream()
                .map(msg -> new ChatMessageDTO(msg.getContent(),
                        msg.getSender(),
                        msg.getReceiver(),
                        msg.getTimestamp()))
                .collect(Collectors.toList());
    }

    public List<ConversationDTO> getConversations(String currentUsername) {
        // Fetch distinct users that the current user has conversed with
        List<Users> users = repo.findDistinctUsersBySenderOrReceiver(currentUsername);

        // Fetch messages between the current user and each of these users
        return users.stream().map(user -> {
            List<ChatMessages> messages = repo.findBySenderAndReceiverOrReceiverAndSender(
                    currentUsername, user.getUsername());

            List<ChatMessageDTO> messageDTOs = messages.stream()
                    .map(msg -> new ChatMessageDTO(
                            msg.getContent(),
                            msg.getSender(),
                            msg.getReceiver(),
                            msg.getTimestamp()))
                    .collect(Collectors.toList());

            return new ConversationDTO(user, messageDTOs);  // Construct the ConversationDTO here
        }).collect(Collectors.toList());  // Return a list of ConversationDTO
    }

    public List<ChatMessageDTO> getMessagesForConversation(String currentUsername, String otherUserName) {
        List<ChatMessages> messages = repo.findBySenderAndReceiverOrReceiverAndSender(currentUsername, otherUserName);
        return messages.stream()
                .map(msg -> new ChatMessageDTO(
                        msg.getContent(),
                        msg.getSender(),
                        msg.getReceiver(),
                        msg.getTimestamp()))
                .collect(Collectors.toList());
    }

}



