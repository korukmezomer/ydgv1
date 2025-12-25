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

    @Test
    void delete_shouldThrowExceptionWhenUserNotOwner() {
        Long storyId = 1L;
        Long ownerId = 2L;
        Long otherUserId = 3L;

        User owner = new User();
        owner.setId(ownerId);

        Story story = new Story();
        story.setId(storyId);
        story.setUser(owner);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

        assertThrows(RuntimeException.class, () -> storyService.delete(storyId, otherUserId));
        verify(storyRepository, never()).save(any(Story.class));
    }

    @Test
    void findById_shouldReturnStoryResponse() {
        Long storyId = 1L;
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        Role role = new Role();
        role.setName("USER");
        user.setRoles(Set.of(role));

        Story story = new Story();
        story.setId(storyId);
        story.setTitle("Test Story");
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setUser(user);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

        StoryResponse response = storyService.findById(storyId);

        assertNotNull(response);
        assertEquals(storyId, response.getId());
    }

    @Test
    void findById_shouldThrowExceptionWhenNotFound() {
        Long storyId = 999L;
        when(storyRepository.findById(storyId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> storyService.findById(storyId));
    }

    @Test
    void findBySlug_shouldReturnStoryResponse() {
        String slug = "test-story";
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        Role role = new Role();
        role.setName("USER");
        user.setRoles(Set.of(role));

        Story story = new Story();
        story.setId(1L);
        story.setTitle("Test Story");
        story.setSlug(slug);
        story.setUser(user);

        when(storyRepository.findBySlug(slug)).thenReturn(Optional.of(story));

        StoryResponse response = storyService.findBySlug(slug);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void publish_shouldSetStatusToYayinBekliyor() {
        Long storyId = 1L;
        Long userId = 2L;

        User owner = new User();
        owner.setId(userId);

        Story story = new Story();
        story.setId(storyId);
        story.setUser(owner);
        story.setStatus(Story.StoryStatus.TASLAK);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

        storyService.publish(storyId, userId);

        assertEquals(Story.StoryStatus.YAYIN_BEKLIYOR, story.getStatus());
        verify(storyRepository, times(1)).save(story);
    }

    @Test
    void publish_shouldThrowExceptionWhenUserNotOwner() {
        Long storyId = 1L;
        Long ownerId = 2L;
        Long otherUserId = 3L;

        User owner = new User();
        owner.setId(ownerId);

        Story story = new Story();
        story.setId(storyId);
        story.setUser(owner);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

        assertThrows(RuntimeException.class, () -> storyService.publish(storyId, otherUserId));
    }

    @Test
    void reject_shouldSetStatusToReddedildi() {
        Long storyId = 1L;
        Long adminId = 2L;
        String reason = "Uygunsuz içerik";

        Story story = new Story();
        story.setId(storyId);
        story.setStatus(Story.StoryStatus.YAYIN_BEKLIYOR);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

        storyService.reject(storyId, adminId, reason);

        assertEquals(Story.StoryStatus.REDDEDILDI, story.getStatus());
        verify(storyRepository, times(1)).save(story);
    }

    @Test
    void toggleEditorPick_shouldThrowExceptionWhenStoryNotPublished() {
        Long storyId = 1L;
        Long adminId = 2L;

        Story story = new Story();
        story.setId(storyId);
        story.setStatus(Story.StoryStatus.TASLAK);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

        assertThrows(RuntimeException.class, () -> storyService.toggleEditorPick(storyId, adminId));
        verify(storyRepository, never()).save(any(Story.class));
    }

    @Test
    void update_shouldUpdateStoryFields() {
        Long storyId = 1L;
        Long userId = 2L;

        User owner = new User();
        owner.setId(userId);

        Story story = new Story();
        story.setId(storyId);
        story.setUser(owner);
        story.setTitle("Old Title");

        StoryUpdateRequest request = new StoryUpdateRequest();
        request.setBaslik("New Title");
        request.setIcerik("New Content");

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(storyRepository.existsBySlug(anyString())).thenReturn(false);

        Story saved = new Story();
        saved.setId(storyId);
        saved.setTitle("New Title");
        when(storyRepository.save(any(Story.class))).thenReturn(saved);

        StoryResponse response = storyService.update(storyId, userId, request);

        assertNotNull(response);
        verify(storyRepository, times(1)).save(any(Story.class));
    }

    @Test
    void update_shouldThrowExceptionWhenUserNotOwner() {
        Long storyId = 1L;
        Long ownerId = 2L;
        Long otherUserId = 3L;

        User owner = new User();
        owner.setId(ownerId);

        Story story = new Story();
        story.setId(storyId);
        story.setUser(owner);

        StoryUpdateRequest request = new StoryUpdateRequest();
        request.setBaslik("New Title");

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

        assertThrows(RuntimeException.class, () -> storyService.update(storyId, otherUserId, request));
    }

    @Test
    void create_shouldCreateStoryWithCategory() {
        Long userId = 1L;
        Long categoryId = 10L;

        User user = new User();
        user.setId(userId);

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Technology");

        StoryCreateRequest request = new StoryCreateRequest();
        request.setBaslik("Test Story");
        request.setIcerik("Content");
        request.setKategoriId(categoryId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(storyRepository.existsBySlug(anyString())).thenReturn(false);

        Story saved = new Story();
        saved.setId(10L);
        saved.setTitle(request.getBaslik());
        saved.setUser(user);
        saved.setCategory(category);

        when(storyRepository.save(any(Story.class))).thenReturn(saved);

        StoryResponse response = storyService.create(userId, request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        verify(storyRepository, times(1)).save(any(Story.class));
    }
}

