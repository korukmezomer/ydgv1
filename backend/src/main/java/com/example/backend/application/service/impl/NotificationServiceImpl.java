package com.example.backend.application.service.impl;

import com.example.backend.application.dto.response.NotificationResponse;
import com.example.backend.application.exception.ForbiddenException;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.application.service.NotificationService;
import com.example.backend.domain.entity.Notification;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.repository.NotificationRepository;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.domain.repository.StoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Override
    public void createNotification(Long userId, String title, String message, 
                                Notification.NotificationType type, Long relatedStoryId, Long relatedCommentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);
        notification.setRelatedStoryId(relatedStoryId);
        notification.setRelatedCommentId(relatedCommentId);

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> findByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> findUnreadByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndIsRead(userId, false, pageable)
                .map(this::toResponse);
    }

    @Override
    public void markAsRead(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bildirim bulunamadı"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Bu bildirime erişim yetkiniz yok");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Long userId) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.Pageable.unpaged();
        notificationRepository.findByUserIdAndIsRead(userId, false, pageable)
                .forEach(notification -> {
                    notification.setIsRead(true);
                    notificationRepository.save(notification);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUnread(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setIsRead(notification.getIsRead());
        response.setRelatedStoryId(notification.getRelatedStoryId());
        response.setRelatedCommentId(notification.getRelatedCommentId());
        response.setCreatedAt(notification.getCreatedAt());
        
        // Story slug'ı ekle
        if (notification.getRelatedStoryId() != null) {
            try {
                Story story = storyRepository.findById(notification.getRelatedStoryId()).orElse(null);
                if (story != null) {
                    response.setRelatedStorySlug(story.getSlug());
                }
            } catch (Exception e) {
                // Story bulunamazsa slug null kalır
            }
        }
        
        return response;
    }
}

