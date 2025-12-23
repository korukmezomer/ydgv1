package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.StoryCreateRequest;
import com.example.backend.application.dto.request.StoryUpdateRequest;
import com.example.backend.application.dto.response.StoryResponse;
import com.example.backend.domain.entity.*;
import com.example.backend.domain.repository.*;
import com.example.backend.application.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoryServiceImplTest {

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private StoryServiceImpl storyService;

    @Test
    void create_shouldCreateStoryWithSlug() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        StoryCreateRequest request = new StoryCreateRequest();
        request.setBaslik("Test Story");
        request.setIcerik("Content");
        request.setOzet("Summary");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storyRepository.existsBySlug(anyString())).thenReturn(false);

        Story saved = new Story();
        saved.setId(10L);
        saved.setTitle(request.getBaslik());
        saved.setUser(user);
        saved.setStatus(Story.StoryStatus.TASLAK);

        when(storyRepository.save(any(Story.class))).thenReturn(saved);

        StoryResponse response = storyService.create(userId, request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        verify(storyRepository, times(1)).save(any(Story.class));
    }

    @Test
    void approve_shouldPublishStoryAndNotifyFollowers() {
        Long storyId = 1L;
        Long adminId = 2L;
        Long authorId = 3L;

        User author = new User();
        author.setId(authorId);
        author.setUsername("author");

        Story story = new Story();
        story.setId(storyId);
        story.setUser(author);
        story.setStatus(Story.StoryStatus.YAYIN_BEKLIYOR);
        story.setTitle("Test Story");

        User follower = new User();
        follower.setId(4L);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(followRepository.findFollowersByFollowedId(authorId)).thenReturn(List.of(follower));

        storyService.approve(storyId, adminId);

        assertEquals(Story.StoryStatus.YAYINLANDI, story.getStatus());
        assertNotNull(story.getPublishedAt());
        verify(storyRepository, times(1)).save(story);
        verify(notificationService, times(1)).createNotification(
                eq(4L),
                eq("Yeni İçerik"),
                contains("author"),
                eq(Notification.NotificationType.HABER_YAYINLANDI),
                eq(storyId),
                isNull()
        );
    }

    @Test
    void toggleEditorPick_shouldToggleEditorPickStatus() {
        Long storyId = 1L;
        Long adminId = 2L;

        Story story = new Story();
        story.setId(storyId);
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setIsEditorPick(false);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

        storyService.toggleEditorPick(storyId, adminId);

        assertTrue(story.getIsEditorPick());
        verify(storyRepository, times(1)).save(story);
    }

    @Test
    void delete_shouldSetStoryInactive() {
        Long storyId = 1L;
        Long userId = 2L;

        User owner = new User();
        owner.setId(userId);

        Story story = new Story();
        story.setId(storyId);
        story.setUser(owner);
        story.setIsActive(true);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

        storyService.delete(storyId, userId);

        assertFalse(story.getIsActive());
        verify(storyRepository, times(1)).save(story);
    }
}

