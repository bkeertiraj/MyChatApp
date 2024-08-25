package com.myprojects.MyChatApp.controller;

import com.myprojects.MyChatApp.model.Users;
import com.myprojects.MyChatApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Users register(@RequestBody Users user){
        System.out.println("Registration request received for user: " + user.getUsername());
        return userService.register(user);
    }

    @GetMapping("/users")
    public List<Users> getAllUsers() {
        // Get the currently logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        // Return a list of all users except the current user
        return userService.getAllUsersExcept(currentUsername);
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<Users>> searchUsers(@RequestParam("query") String query) {
        // Get the currently logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        // Perform search while excluding the current user
        List<Users> users = userService.searchUsers(query, currentUsername);
        return ResponseEntity.ok(users);
    }
}
