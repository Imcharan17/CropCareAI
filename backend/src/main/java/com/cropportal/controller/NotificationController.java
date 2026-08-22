package com.cropportal.controller;

import com.cropportal.entity.Notification;
import com.cropportal.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository notificationRepository;

    @GetMapping
    public List<Notification> list(Authentication authentication) {
        return notificationRepository.findTop20ByUserEmailOrderByCreatedAtDesc(authentication.getName());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unread(Authentication authentication) {
        return Map.of("count", notificationRepository.countByUserEmailAndReadFlagFalse(authentication.getName()));
    }

    @PatchMapping("/{id}/read")
    public Notification read(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id).orElseThrow();
        notification.setReadFlag(true);
        return notificationRepository.save(notification);
    }
}
