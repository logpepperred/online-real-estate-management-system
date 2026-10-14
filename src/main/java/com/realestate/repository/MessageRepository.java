package com.realestate.repository;

import com.realestate.model.Message;
import com.realestate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByRecipientOrderBySentAtDesc(User recipient);

    List<Message> findBySenderOrderBySentAtDesc(User sender);

    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.recipient = :user2) " +
           "OR (m.sender = :user2 AND m.recipient = :user1) ORDER BY m.sentAt ASC")
    List<Message> findConversationBetween(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT m FROM Message m WHERE m.recipient = :user OR m.sender = :user ORDER BY m.sentAt DESC")
    List<Message> findAllUserMessages(@Param("user") User user);

    long countByRecipientAndIsReadFalse(User recipient);
}
