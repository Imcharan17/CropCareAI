package com.cropportal.repository;

import com.cropportal.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop20ByUserEmailOrderByCreatedAtDesc(String email);
    long countByUserEmailAndReadFlagFalse(String email);
}
