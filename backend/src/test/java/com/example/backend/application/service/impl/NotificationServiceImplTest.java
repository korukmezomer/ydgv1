package com.example.backend.application.service.impl;

import com.example.backend.application.dto.response.NotificationResponse;
import com.example.backend.application.exception.ForbiddenException;
import com.example.backend.application.exception.ResourceNotFoundException;
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

    @Test
    void createNotification_shouldThrowExceptionWhenUserNotFound() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.createNotification(
                userId, "Title", "Message", Notification.NotificationType.YENI_YORUM, null, null));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void findByUserId_shouldReturnPageOfNotifications() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(userId);

        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUser(user);
        notification.setTitle("Test");
        notification.setMessage("Test message");

        Page<Notification> notificationPage = new PageImpl<>(List.of(notification));
        when(notificationRepository.findByUserId(userId, pageable)).thenReturn(notificationPage);

        Page<NotificationResponse> response = notificationService.findByUserId(userId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(notificationRepository, times(1)).findByUserId(userId, pageable);
    }

    @Test
    void findUnreadByUserId_shouldReturnPageOfUnreadNotifications() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(userId);

        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUser(user);
        notification.setIsRead(false);

        Page<Notification> notificationPage = new PageImpl<>(List.of(notification));
        when(notificationRepository.findByUserIdAndIsRead(userId, false, pageable))
                .thenReturn(notificationPage);

        Page<NotificationResponse> response = notificationService.findUnreadByUserId(userId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(notificationRepository, times(1)).findByUserIdAndIsRead(userId, false, pageable);
    }

    @Test
    void markAsRead_shouldThrowExceptionWhenNotificationNotFound() {
        Long notificationId = 999L;
        Long userId = 1L;

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(notificationId, userId));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAsRead_shouldThrowExceptionWhenUserNotOwner() {
        Long notificationId = 1L;
        Long ownerId = 1L;
        Long otherUserId = 2L;

        User owner = new User();
        owner.setId(ownerId);

        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setUser(owner);
        notification.setIsRead(false);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        assertThrows(ForbiddenException.class, () -> notificationService.markAsRead(notificationId, otherUserId));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void findByUserId_shouldIncludeStorySlugWhenStoryExists() {
        Long userId = 1L;
        Long storyId = 10L;
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(userId);

        Story story = new Story();
        story.setId(storyId);
        story.setSlug("test-story");

        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUser(user);
        notification.setRelatedStoryId(storyId);

        Page<Notification> notificationPage = new PageImpl<>(List.of(notification));
        when(notificationRepository.findByUserId(userId, pageable)).thenReturn(notificationPage);
        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

        Page<NotificationResponse> response = notificationService.findByUserId(userId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("test-story", response.getContent().get(0).getRelatedStorySlug());
    }

    @Test
    void findByUserId_shouldHandleStoryNotFound() {
        Long userId = 1L;
        Long storyId = 999L;
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(userId);

        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUser(user);
        notification.setRelatedStoryId(storyId);

        Page<Notification> notificationPage = new PageImpl<>(List.of(notification));
        when(notificationRepository.findByUserId(userId, pageable)).thenReturn(notificationPage);
        when(storyRepository.findById(storyId)).thenReturn(Optional.empty());

        Page<NotificationResponse> response = notificationService.findByUserId(userId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertNull(response.getContent().get(0).getRelatedStorySlug());
    }
}

