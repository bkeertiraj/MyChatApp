package com.myprojects.MyChatApp.controller;

import com.myprojects.MyChatApp.dto.ChatMessageDTO;
import com.myprojects.MyChatApp.dto.ConversationDTO;
import com.myprojects.MyChatApp.model.Users;
import com.myprojects.MyChatApp.service.ChatMessageService;
import com.myprojects.MyChatApp.service.MyUserService;
import com.myprojects.MyChatApp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageService.class);

    // Endpoint for sending a message
    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody ChatMessageDTO chatMessageDTO) {
        logger.info("Sending message from {} to {}", chatMessageDTO.getSender(), chatMessageDTO.getReceiver());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        // Ensure the sender in the DTO matches the authenticated user
        if (!currentUsername.equals(chatMessageDTO.getSender())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to send messages as another user.");
        }

        // Send the message using the authenticated user as the sender
        chatMessageService.sendMessage(new ChatMessageDTO(
                chatMessageDTO.getContent(),
                currentUsername,  // Use authenticated username
                chatMessageDTO.getReceiver(),
                LocalDateTime.now()
        ));

        // Send the message to the receiver (and the sender) via WebSocket
        messagingTemplate.convertAndSendToUser(chatMessageDTO.getReceiver(), "/queue/messages", chatMessageDTO);
        messagingTemplate.convertAndSendToUser(currentUsername, "/queue/messages", chatMessageDTO);

        return ResponseEntity.ok("Message sent");
    }

//    // Endpoint for retrieving messages for a specific user
//    @GetMapping("/messages/{receiver}")
//    public ResponseEntity<List<ChatMessageDTO>> getMessages(@PathVariable String receiver) {
//        List<ChatMessageDTO> messages = chatMessageService.getMessagesForReceiver(receiver);
//        return ResponseEntity.ok(messages);
//    }

    // Default greeting endpoint
    @GetMapping
    public String greet(HttpServletRequest request) {
        return "Hello world, session ID: " + request.getSession().getId();
    }

    @GetMapping("/conversations")
    public List<ConversationDTO> getConversations() {
        // Get the currently logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        // Return a list of conversations that the current user has had
        return chatMessageService.getConversations(currentUsername);
    }


    @GetMapping("/messages/{userId}")
    public ResponseEntity<List<ChatMessageDTO>> getMessagesForConversation(@PathVariable Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        String otherUsername = userService.getUsernameById(userId);
        System.out.println("currentUsername: "+currentUsername);
        System.out.println("otherUsername: "+otherUsername);
        List<ChatMessageDTO> messages = chatMessageService.getMessagesForConversation(currentUsername, otherUsername);
        return ResponseEntity.ok(messages);
    }
}
