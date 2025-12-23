package com.example.backend.application.service;

import com.example.backend.application.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    
    void createNotification(Long userId, String title, String message, com.example.backend.domain.entity.Notification.NotificationType type, Long relatedStoryId, Long relatedCommentId);
    
    Page<NotificationResponse> findByUserId(Long userId, Pageable pageable);
    
    Page<NotificationResponse> findUnreadByUserId(Long userId, Pageable pageable);
    
    void markAsRead(Long id, Long userId);
    
    void markAllAsRead(Long userId);
    
    Long countUnread(Long userId);
}

