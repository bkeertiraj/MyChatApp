package com.myprojects.MyChatApp.service;

import com.myprojects.MyChatApp.model.UserPrincipal;
import com.myprojects.MyChatApp.model.Users;
import com.myprojects.MyChatApp.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserService implements UserDetailsService {

    @Autowired
    private UsersRepository repo;

    private static final Logger logger = LoggerFactory.getLogger(MyUserService.class);



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to load user by the username {}", username);

        Users user = repo.findByUsername(username);

        if(user == null) {
            logger.error("User not found lol {}", username);
            throw new UsernameNotFoundException("user not found 123");
        }
        logger.info("User found oh yeah: {}", username);
        return new UserPrincipal(user);
    }


}
