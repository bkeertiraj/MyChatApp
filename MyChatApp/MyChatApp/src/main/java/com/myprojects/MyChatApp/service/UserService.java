package com.myprojects.MyChatApp.service;

import com.myprojects.MyChatApp.model.Users;
import com.myprojects.MyChatApp.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UsersRepository repo;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public Users register(Users user){
        user.setPassword(encoder.encode(user.getPassword()));
        return repo.save(user);
    }

    public boolean authenticate(String username, String password) {
        Users user = repo.findByUsername(username);
        if (user == null) {
            return false;
        }
        return encoder.matches(password, user.getPassword());
    }

    public List<Users> getAllUsersExcept(String currentUsername) {
        // Fetch all users except the one with the given username
        return repo.findAllByUsernameNot(currentUsername);
    }

    public String getUsernameById(Long userId) {
        return repo.findById(userId)
                .map(Users::getUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<Users> searchUsers(String query, String currentUsername) {
        return repo.findByUsernameContainingIgnoreCaseAndUsernameNot(query, currentUsername);
    }
}
