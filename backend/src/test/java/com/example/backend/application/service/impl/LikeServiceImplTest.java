package com.example.backend.application.service.impl;

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
}

