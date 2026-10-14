package com.realestate.service;

import com.realestate.model.Notification;
import com.realestate.model.User;
import com.realestate.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification sendNotification(User recipient, String title, String message, String link) {
        Notification notification = new Notification(recipient, title, message, link);
        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(User recipient) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
    }

    public List<Notification> getRecentUserNotifications(User recipient) {
        return notificationRepository.findTop5ByRecipientOrderByCreatedAtDesc(recipient);
    }

    public long getUnreadCount(User recipient) {
        return notificationRepository.countByRecipientAndIsReadFalse(recipient);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    public void markAllAsRead(User recipient) {
        List<Notification> list = notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
        for (Notification n : list) {
            if (!n.isRead()) {
                n.setRead(true);
            }
        }
        notificationRepository.saveAll(list);
    }
}
