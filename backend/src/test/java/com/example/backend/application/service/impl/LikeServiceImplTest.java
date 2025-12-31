package com.example.backend.application.service.impl;

import com.example.backend.application.exception.BadRequestException;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.domain.entity.Like;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.entity.Notification;
import com.example.backend.domain.repository.LikeRepository;
import com.example.backend.domain.repository.StoryRepository;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.application.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LikeServiceImpl likeService;

    @Test
    void like_shouldIncreaseLikeCountAndSendNotification() {
        Long storyId = 1L;
        Long userId = 2L;

        when(likeRepository.existsByUserIdAndStoryId(userId, storyId)).thenReturn(false);

        Story story = new Story();
        story.setId(storyId);
        story.setLikeCount(0L);
        story.setTitle("Test Story");
        User storyOwner = new User();
        storyOwner.setId(3L);
        story.setUser(storyOwner);

        User user = new User();
        user.setId(userId);
        user.setUsername("liker");

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        likeService.like(storyId, userId);

        assertEquals(1L, story.getLikeCount());
        verify(likeRepository, times(1)).save(any(Like.class));
        verify(storyRepository, times(1)).save(story);
        verify(notificationService, times(1)).createNotification(
                eq(3L),
                eq("Yazınız Beğenildi"),
                contains("liker"),
                eq(Notification.NotificationType.HABER_BEGENILDI),
                eq(storyId),
                isNull()
        );
    }

    @Test
    void unlike_shouldDecreaseLikeCountAndDeleteLike() {
        Long storyId = 1L;
        Long userId = 2L;

        Story story = new Story();
        story.setId(storyId);
        story.setLikeCount(5L);

        Like like = new Like();
        like.setStory(story);

        when(likeRepository.findByUserIdAndStoryId(userId, storyId)).thenReturn(Optional.of(like));

        likeService.unlike(storyId, userId);

        verify(likeRepository, times(1)).delete(like);
        assertEquals(4L, story.getLikeCount());
        verify(storyRepository, times(1)).save(story);
    }

    @Test
    void like_shouldThrowExceptionWhenAlreadyLiked() {
        Long storyId = 1L;
        Long userId = 2L;

        when(likeRepository.existsByUserIdAndStoryId(userId, storyId)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> likeService.like(storyId, userId));
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    void like_shouldNotSendNotificationWhenLikingOwnStory() {
        Long storyId = 1L;
        Long userId = 2L;

        when(likeRepository.existsByUserIdAndStoryId(userId, storyId)).thenReturn(false);

        Story story = new Story();
        story.setId(storyId);
        story.setLikeCount(0L);
        story.setTitle("Test Story");
        User storyOwner = new User();
        storyOwner.setId(userId); // Same user
        story.setUser(storyOwner);

        User user = new User();
        user.setId(userId);
        user.setUsername("owner");

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        likeService.like(storyId, userId);

        assertEquals(1L, story.getLikeCount());
        verify(likeRepository, times(1)).save(any(Like.class));
        verify(notificationService, never()).createNotification(anyLong(), anyString(), anyString(), any(), anyLong(), any());
    }

    @Test
    void unlike_shouldThrowExceptionWhenLikeNotFound() {
        Long storyId = 1L;
        Long userId = 2L;

        when(likeRepository.findByUserIdAndStoryId(userId, storyId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> likeService.unlike(storyId, userId));
        verify(likeRepository, never()).delete(any(Like.class));
    }

    @Test
    void isLiked_shouldReturnTrueWhenLiked() {
        Long storyId = 1L;
        Long userId = 2L;

        when(likeRepository.existsByUserIdAndStoryId(userId, storyId)).thenReturn(true);

        boolean result = likeService.isLiked(storyId, userId);

        assertTrue(result);
        verify(likeRepository, times(1)).existsByUserIdAndStoryId(userId, storyId);
    }

    @Test
    void isLiked_shouldReturnFalseWhenNotLiked() {
        Long storyId = 1L;
        Long userId = 2L;

        when(likeRepository.existsByUserIdAndStoryId(userId, storyId)).thenReturn(false);

        boolean result = likeService.isLiked(storyId, userId);

        assertFalse(result);
        verify(likeRepository, times(1)).existsByUserIdAndStoryId(userId, storyId);
    }

    @Test
    void getLikeCount_shouldReturnCount() {
        Long storyId = 1L;
        Long expectedCount = 10L;

        when(likeRepository.countActiveByStoryId(storyId)).thenReturn(expectedCount);

        Long result = likeService.getLikeCount(storyId);

        assertEquals(expectedCount, result);
        verify(likeRepository, times(1)).countActiveByStoryId(storyId);
    }

    @Test
    void like_shouldThrowExceptionWhenStoryNotFound() {
        Long storyId = 999L;
        Long userId = 2L;

        when(likeRepository.existsByUserIdAndStoryId(userId, storyId)).thenReturn(false);
        when(storyRepository.findById(storyId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> likeService.like(storyId, userId));
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    void like_shouldThrowExceptionWhenUserNotFound() {
        Long storyId = 1L;
        Long userId = 999L;

        Story story = new Story();
        story.setId(storyId);

        when(likeRepository.existsByUserIdAndStoryId(userId, storyId)).thenReturn(false);
        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> likeService.like(storyId, userId));
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    void like_shouldHandleNullUsername() {
        Long storyId = 1L;
        Long userId = 2L;

        when(likeRepository.existsByUserIdAndStoryId(userId, storyId)).thenReturn(false);

        Story story = new Story();
        story.setId(storyId);
        story.setLikeCount(0L);
        story.setTitle("Test Story");
        User storyOwner = new User();
        storyOwner.setId(3L);
        story.setUser(storyOwner);

        User user = new User();
        user.setId(userId);
        user.setUsername(null);
        user.setFirstName("John");

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Like savedLike = new Like();
        savedLike.setId(1L);
        when(likeRepository.save(any(Like.class))).thenReturn(savedLike);

        likeService.like(storyId, userId);

        verify(notificationService, times(1)).createNotification(
                eq(3L),
                eq("Yazınız Beğenildi"),
                contains("John"),
                eq(Notification.NotificationType.HABER_BEGENILDI),
                eq(storyId),
                isNull()
        );
    }

    @Test
    void like_shouldHandleNullUsernameAndFirstName() {
        Long storyId = 1L;
        Long userId = 2L;

        when(likeRepository.existsByUserIdAndStoryId(userId, storyId)).thenReturn(false);

        Story story = new Story();
        story.setId(storyId);
        story.setLikeCount(0L);
        story.setTitle("Test Story");
        User storyOwner = new User();
        storyOwner.setId(3L);
        story.setUser(storyOwner);

        User user = new User();
        user.setId(userId);
        user.setUsername(null);
        user.setFirstName(null);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Like savedLike = new Like();
        savedLike.setId(1L);
        when(likeRepository.save(any(Like.class))).thenReturn(savedLike);

        likeService.like(storyId, userId);

        verify(notificationService, times(1)).createNotification(
                eq(3L),
                eq("Yazınız Beğenildi"),
                contains("Bir kullanıcı"),
                eq(Notification.NotificationType.HABER_BEGENILDI),
                eq(storyId),
                isNull()
        );
    }

    @Test
    void like_shouldTruncateLongTitle() {
        Long storyId = 1L;
        Long userId = 2L;

        when(likeRepository.existsByUserIdAndStoryId(userId, storyId)).thenReturn(false);

        Story story = new Story();
        story.setId(storyId);
        story.setLikeCount(0L);
        String longTitle = "a".repeat(100);
        story.setTitle(longTitle);
        User storyOwner = new User();
        storyOwner.setId(3L);
        story.setUser(storyOwner);

        User user = new User();
        user.setId(userId);
        user.setUsername("liker");

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Like savedLike = new Like();
        savedLike.setId(1L);
        when(likeRepository.save(any(Like.class))).thenReturn(savedLike);

        likeService.like(storyId, userId);

        verify(notificationService, times(1)).createNotification(
                eq(3L),
                eq("Yazınız Beğenildi"),
                argThat(message -> message.contains("...") && message.length() < longTitle.length() + 50),
                eq(Notification.NotificationType.HABER_BEGENILDI),
                eq(storyId),
                isNull()
        );
    }

    @Test
    void unlike_shouldNotDecreaseBelowZero() {
        Long storyId = 1L;
        Long userId = 2L;

        Story story = new Story();
        story.setId(storyId);
        story.setLikeCount(0L); // Already at zero

        Like like = new Like();
        like.setStory(story);

        when(likeRepository.findByUserIdAndStoryId(userId, storyId)).thenReturn(Optional.of(like));

        likeService.unlike(storyId, userId);

        assertEquals(0L, story.getLikeCount()); // Should stay at 0, not go negative
        verify(storyRepository, times(1)).save(story);
    }
}

