package com.newshub.backend.interfaces.rest;

import com.newshub.backend.application.service.NotificationService;
import com.newshub.backend.interfaces.dto.NotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/notifications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest request) {
        if (Boolean.TRUE.equals(request.getIsGlobal())) {
            notificationService.sendNotificationToAllUsers(request.getTitle(), request.getContent(), request.getType());
        } else if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            notificationService.sendNotificationToUsers(request.getUserIds(), request.getTitle(), request.getContent(), request.getType());
        } else if (request.getUserId() != null) {
            notificationService.createNotification(request.getUserId(), request.getTitle(), request.getContent(), request.getType(), null);
        } else {
            return ResponseEntity.badRequest().body("Either userId, userIds or isGlobal=true must be provided");
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }
}
