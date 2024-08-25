package com.myprojects.MyChatApp.repository;

import com.myprojects.MyChatApp.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    Users findByUsername(String username);

    List<Users> findAllByUsernameNot(String currentUsername);

    List<Users> findByUsernameContainingIgnoreCaseAndUsernameNot(String query, String currentUsername);

//    Users findById(Long id);
}

