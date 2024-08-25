package com.myprojects.MyChatApp.repository;

import com.myprojects.MyChatApp.model.ChatMessages;
import com.myprojects.MyChatApp.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessages, Long> {

    List<ChatMessages> findByReceiver(String receiver);

    // Corrected query to return Users based on sender or receiver
    @Query("SELECT DISTINCT u FROM Users u " +
            "WHERE u.username IN (" +
            "SELECT m.sender FROM ChatMessages m WHERE m.receiver = :username " +
            "UNION " +
            "SELECT m.receiver FROM ChatMessages m WHERE m.sender = :username)")
    List<Users> findDistinctUsersBySenderOrReceiver(@Param("username") String username);

    @Query("SELECT m FROM ChatMessages m WHERE " +
            "(m.sender = :sender AND m.receiver = :receiver) " +
            "OR (m.sender = :receiver AND m.receiver = :sender)")
    List<ChatMessages> findBySenderAndReceiverOrReceiverAndSender(
            @Param("sender") String sender,
            @Param("receiver") String receiver);
}


