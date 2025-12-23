package com.example.backend.application.service.impl;

import com.example.backend.application.dto.response.NotificationResponse;
import com.example.backend.domain.entity.Notification;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.NotificationRepository;
import com.example.backend.domain.repository.StoryRepository;
import com.example.backend.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoryRepository storyRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void createNotification_shouldSaveNotification() {
        Long userId = 1L;
        Long storyId = 2L;

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        notificationService.createNotification(
                userId,
                "Test Title",
                "Test Message",
                Notification.NotificationType.YENI_YORUM,
                storyId,
                null
        );

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void markAsRead_shouldSetNotificationAsRead() {
        Long notificationId = 1L;
        Long userId = 2L;

        User user = new User();
        user.setId(userId);

        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setUser(user);
        notification.setIsRead(false);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(notificationId, userId);

        assertTrue(notification.getIsRead());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void markAllAsRead_shouldMarkAllUnreadNotificationsAsRead() {
        Long userId = 1L;

        Notification notif1 = new Notification();
        notif1.setIsRead(false);
        Notification notif2 = new Notification();
        notif2.setIsRead(false);

        Page<Notification> page = new PageImpl<>(List.of(notif1, notif2));
        when(notificationRepository.findByUserIdAndIsRead(eq(userId), eq(false), any(Pageable.class)))
                .thenReturn(page);

        notificationService.markAllAsRead(userId);

        assertTrue(notif1.getIsRead());
        assertTrue(notif2.getIsRead());
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void countUnread_shouldReturnUnreadCount() {
        Long userId = 1L;
        Long expectedCount = 5L;

        when(notificationRepository.countUnreadByUserId(userId)).thenReturn(expectedCount);

        Long result = notificationService.countUnread(userId);

        assertEquals(expectedCount, result);
    }
}

