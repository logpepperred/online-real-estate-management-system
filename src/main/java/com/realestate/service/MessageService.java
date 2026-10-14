package com.realestate.service;

import com.realestate.model.Message;
import com.realestate.model.Property;
import com.realestate.model.User;
import com.realestate.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public MessageService(MessageRepository messageRepository,
                          NotificationService notificationService,
                          ActivityLogService activityLogService) {
        this.messageRepository = messageRepository;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    public Message sendMessage(User sender, User recipient, Property property, String subject, String content) {
        Message message = new Message(sender, recipient, property, subject, content);
        Message saved = messageRepository.save(message);

        activityLogService.logActivity(sender, "MESSAGE_SENT",
                "Sent message to " + recipient.getName() + " regarding " + (property != null ? property.getTitle() : "inquiry"));

        notificationService.sendNotification(
                recipient,
                "New Message from " + sender.getName(),
                subject != null && !subject.isEmpty() ? subject : "You received a new direct message.",
                "/messages?with=" + sender.getId()
        );

        return saved;
    }

    public List<Message> getConversation(User user1, User user2) {
        List<Message> conversation = messageRepository.findConversationBetween(user1, user2);
        // Mark messages addressed to user1 as read
        for (Message m : conversation) {
            if (m.getRecipient().getId().equals(user1.getId()) && !m.isRead()) {
                m.setRead(true);
                messageRepository.save(m);
            }
        }
        return conversation;
    }

    public List<Message> getAllUserMessages(User user) {
        return messageRepository.findAllUserMessages(user);
    }

    public long getUnreadCount(User user) {
        return messageRepository.countByRecipientAndIsReadFalse(user);
    }
}
