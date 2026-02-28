package com.newshub.backend.application.service;

import com.newshub.backend.domain.model.Notification;
import com.newshub.backend.domain.model.User;
import com.newshub.backend.infrastructure.persistence.NotificationMapper;
import com.newshub.backend.infrastructure.persistence.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public List<Notification> getUserNotifications(Long userId) {
        return notificationMapper.findByUserId(userId);
    }

    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    public void markAsRead(Long id, Long userId) {
        notificationMapper.markAsRead(id, userId);
    }

    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    public void createNotification(Long userId, String title, String content, String type, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(false);
        notification.setRelatedId(relatedId);
        notificationMapper.insert(notification);

        // Send notification via WebSocket
        // Topic: /topic/notifications/{userId}
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
    }

    public void sendNotificationToAllUsers(String title, String content, String type) {
        List<User> users = userMapper.findAll();
        for (User user : users) {
            createNotification(user.getId(), title, content, type, null);
        }
    }

    public void sendNotificationToUsers(List<Long> userIds, String title, String content, String type) {
        for (Long userId : userIds) {
            createNotification(userId, title, content, type, null);
        }
    }

    public List<Notification> getAllNotifications() {
        return notificationMapper.findAll();
    }
}
