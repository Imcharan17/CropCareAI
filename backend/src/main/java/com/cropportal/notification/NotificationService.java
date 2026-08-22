package com.cropportal.notification;

import com.cropportal.entity.Notification;
import com.cropportal.entity.User;
import com.cropportal.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MailService mailService;

    @Transactional
    public Notification notify(User user, String title, String body, boolean email) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setBody(body);
        Notification saved = notificationRepository.save(notification);
        messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/notifications", saved);
        if (email) {
            mailService.send(user.getEmail(), title, body);
        }
        return saved;
    }
}
