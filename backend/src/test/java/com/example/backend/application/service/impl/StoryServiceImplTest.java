package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.StoryCreateRequest;
import com.example.backend.application.dto.request.StoryUpdateRequest;
import com.example.backend.application.dto.response.StoryResponse;
import com.example.backend.application.exception.ResourceNotFoundException;
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
    void create_shouldThrowWhenUserNotFound() {
        Long userId = 999L;
        StoryCreateRequest request = new StoryCreateRequest();
        request.setBaslik("Test");
        request.setIcerik("Content");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> storyService.create(userId, request));
        verify(storyRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenCategoryNotFound() {
        Long userId = 1L;
        Long categoryId = 99L;
        User user = new User();
        user.setId(userId);
        StoryCreateRequest request = new StoryCreateRequest();
        request.setBaslik("Test");
        request.setIcerik("Content");
        request.setKategoriId(categoryId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> storyService.create(userId, request));
        verify(storyRepository, never()).save(any());
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
    void approve_shouldThrowWhenStoryNotFound() {
        Long storyId = 999L;
        Long adminId = 1L;
        when(storyRepository.findById(storyId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> storyService.approve(storyId, adminId));
        verify(storyRepository, never()).save(any());
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
    void publish_shouldThrowWhenStoryNotFound() {
        Long storyId = 999L;
        Long userId = 1L;
        when(storyRepository.findById(storyId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> storyService.publish(storyId, userId));
        verify(storyRepository, never()).save(any());
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
        owner.setUsername("owner");
        Role role = new Role();
        role.setName("WRITER");
        owner.setRoles(Set.of(role));

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
        saved.setUser(owner);
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

    @Test
    void create_shouldCreateStoryWithTags() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        StoryCreateRequest request = new StoryCreateRequest();
        request.setBaslik("Test Story");
        request.setIcerik("Content");
        request.setEtiketler(List.of("java", "spring", "test"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storyRepository.existsBySlug(anyString())).thenReturn(false);
        when(tagRepository.findByName("java")).thenReturn(Optional.empty());
        when(tagRepository.findByName("spring")).thenReturn(Optional.empty());
        when(tagRepository.findByName("test")).thenReturn(Optional.empty());
        when(tagRepository.existsBySlug(anyString())).thenReturn(false);

        Story saved = new Story();
        saved.setId(10L);
        saved.setTitle(request.getBaslik());
        saved.setUser(user);
        saved.setStatus(Story.StoryStatus.TASLAK);
        Set<Tag> tags = new HashSet<>();
        Tag tag1 = new Tag();
        tag1.setName("java");
        tags.add(tag1);
        saved.setTags(tags);

        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(storyRepository.save(any(Story.class))).thenReturn(saved);

        StoryResponse response = storyService.create(userId, request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        verify(tagRepository, atLeast(1)).save(any(Tag.class));
        verify(storyRepository, times(1)).save(any(Story.class));
    }

    @Test
    void create_shouldUseExistingTags() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        Tag existingTag = new Tag();
        existingTag.setId(1L);
        existingTag.setName("java");

        StoryCreateRequest request = new StoryCreateRequest();
        request.setBaslik("Test Story");
        request.setIcerik("Content");
        request.setEtiketler(List.of("java"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storyRepository.existsBySlug(anyString())).thenReturn(false);
        when(tagRepository.findByName("java")).thenReturn(Optional.of(existingTag));

        Story saved = new Story();
        saved.setId(10L);
        saved.setTitle(request.getBaslik());
        saved.setUser(user);
        saved.setStatus(Story.StoryStatus.TASLAK);

        when(storyRepository.save(any(Story.class))).thenReturn(saved);

        StoryResponse response = storyService.create(userId, request);

        assertNotNull(response);
        verify(tagRepository, never()).save(any(Tag.class));
        verify(storyRepository, times(1)).save(any(Story.class));
    }

    @Test
    void create_shouldThrowExceptionWhenUserNotFound() {
        Long userId = 999L;
        StoryCreateRequest request = new StoryCreateRequest();
        request.setBaslik("Test Story");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> storyService.create(userId, request));
        verify(storyRepository, never()).save(any(Story.class));
    }

    @Test
    void create_shouldThrowExceptionWhenCategoryNotFound() {
        Long userId = 1L;
        Long categoryId = 999L;
        User user = new User();
        user.setId(userId);

        StoryCreateRequest request = new StoryCreateRequest();
        request.setBaslik("Test Story");
        request.setKategoriId(categoryId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> storyService.create(userId, request));
        verify(storyRepository, never()).save(any(Story.class));
    }

    @Test
    void findAll_shouldReturnPageOfStories() {
        Pageable pageable = PageRequest.of(0, 10);
        Story story = new Story();
        story.setId(1L);
        story.setTitle("Test Story");
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        Role role = new Role();
        role.setName("USER");
        user.setRoles(Set.of(role));
        story.setUser(user);

        Page<Story> storyPage = new PageImpl<>(List.of(story));
        when(storyRepository.findAll(pageable)).thenReturn(storyPage);

        Page<StoryResponse> response = storyService.findAll(pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(storyRepository, times(1)).findAll(pageable);
    }

    @Test
    void findByUserId_shouldReturnPageOfStories() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Story story = new Story();
        story.setId(1L);
        story.setTitle("Test Story");
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        Role role = new Role();
        role.setName("USER");
        user.setRoles(Set.of(role));
        story.setUser(user);

        Page<Story> storyPage = new PageImpl<>(List.of(story));
        when(storyRepository.findByUserId(userId, pageable)).thenReturn(storyPage);

        Page<StoryResponse> response = storyService.findByUserId(userId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(storyRepository, times(1)).findByUserId(userId, pageable);
    }

    @Test
    void findByCategoryId_shouldReturnPageOfStories() {
        Long categoryId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Story story = new Story();
        story.setId(1L);
        story.setTitle("Test Story");
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        Role role = new Role();
        role.setName("USER");
        user.setRoles(Set.of(role));
        story.setUser(user);

        Page<Story> storyPage = new PageImpl<>(List.of(story));
        when(storyRepository.findByCategoryId(categoryId, pageable)).thenReturn(storyPage);

        Page<StoryResponse> response = storyService.findByCategoryId(categoryId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(storyRepository, times(1)).findByCategoryId(categoryId, pageable);
    }

    @Test
    void findByStatus_shouldReturnPageOfStories() {
        Story.StoryStatus status = Story.StoryStatus.YAYINLANDI;
        Pageable pageable = PageRequest.of(0, 10);
        Story story = new Story();
        story.setId(1L);
        story.setTitle("Test Story");
        story.setStatus(status);
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        Role role = new Role();
        role.setName("USER");
        user.setRoles(Set.of(role));
        story.setUser(user);

        Page<Story> storyPage = new PageImpl<>(List.of(story));
        when(storyRepository.findByStatus(status, pageable)).thenReturn(storyPage);

        Page<StoryResponse> response = storyService.findByStatus(status, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(storyRepository, times(1)).findByStatus(status, pageable);
    }

    @Test
    void findPublishedStories_shouldReturnPageOfPublishedStories() {
        Pageable pageable = PageRequest.of(0, 10);
        Story story = new Story();
        story.setId(1L);
        story.setTitle("Test Story");
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        Role role = new Role();
        role.setName("USER");
        user.setRoles(Set.of(role));
        story.setUser(user);

        Page<Story> storyPage = new PageImpl<>(List.of(story));
        when(storyRepository.findPublishedStories(Story.StoryStatus.YAYINLANDI, pageable)).thenReturn(storyPage);

        Page<StoryResponse> response = storyService.findPublishedStories(pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(storyRepository, times(1)).findPublishedStories(Story.StoryStatus.YAYINLANDI, pageable);
    }

    @Test
    void findPopularStories_shouldReturnPageOfPopularStories() {
        Pageable pageable = PageRequest.of(0, 10);
        Story story = new Story();
        story.setId(1L);
        story.setTitle("Test Story");
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        Role role = new Role();
        role.setName("USER");
        user.setRoles(Set.of(role));
        story.setUser(user);

        Page<Story> storyPage = new PageImpl<>(List.of(story));
        when(storyRepository.findPopularStories(Story.StoryStatus.YAYINLANDI, pageable)).thenReturn(storyPage);

        Page<StoryResponse> response = storyService.findPopularStories(pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(storyRepository, times(1)).findPopularStories(Story.StoryStatus.YAYINLANDI, pageable);
    }

    @Test
    void findEditorPicks_shouldReturnPageOfEditorPicks() {
        Pageable pageable = PageRequest.of(0, 10);
        Story story = new Story();
        story.setId(1L);
        story.setTitle("Test Story");
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setIsEditorPick(true);
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        Role role = new Role();
        role.setName("USER");
        user.setRoles(Set.of(role));
        story.setUser(user);

        Page<Story> storyPage = new PageImpl<>(List.of(story));
        when(storyRepository.findEditorPicks(Story.StoryStatus.YAYINLANDI, pageable)).thenReturn(storyPage);

        Page<StoryResponse> response = storyService.findEditorPicks(pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(storyRepository, times(1)).findEditorPicks(Story.StoryStatus.YAYINLANDI, pageable);
    }

    @Test
    void search_shouldReturnPageOfMatchingStories() {
        String query = "test";
        Pageable pageable = PageRequest.of(0, 10);
        Story story = new Story();
        story.setId(1L);
        story.setTitle("Test Story");
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        Role role = new Role();
        role.setName("USER");
        user.setRoles(Set.of(role));
        story.setUser(user);

        Page<Story> storyPage = new PageImpl<>(List.of(story));
        when(storyRepository.searchStories(query, Story.StoryStatus.YAYINLANDI, pageable)).thenReturn(storyPage);

        Page<StoryResponse> response = storyService.search(query, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(storyRepository, times(1)).searchStories(query, Story.StoryStatus.YAYINLANDI, pageable);
    }

    @Test
    void findByTagId_shouldReturnPageOfStories() {
        Long tagId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Story story = new Story();
        story.setId(1L);
        story.setTitle("Test Story");
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        Role role = new Role();
        role.setName("USER");
        user.setRoles(Set.of(role));
        story.setUser(user);

        Page<Story> storyPage = new PageImpl<>(List.of(story));
        when(storyRepository.findByTagId(tagId, Story.StoryStatus.YAYINLANDI, pageable)).thenReturn(storyPage);

        Page<StoryResponse> response = storyService.findByTagId(tagId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(storyRepository, times(1)).findByTagId(tagId, Story.StoryStatus.YAYINLANDI, pageable);
    }

    @Test
    void findBySlug_shouldThrowExceptionWhenNotFound() {
        String slug = "non-existent";
        when(storyRepository.findBySlug(slug)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> storyService.findBySlug(slug));
    }

    @Test
    void generateUniqueSlug_shouldHandleDuplicateSlugs() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        StoryCreateRequest request = new StoryCreateRequest();
        request.setBaslik("Test Story");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storyRepository.existsBySlug("test-story")).thenReturn(true);
        when(storyRepository.existsBySlug("test-story-1")).thenReturn(true);
        when(storyRepository.existsBySlug("test-story-2")).thenReturn(false);

        Story saved = new Story();
        saved.setId(10L);
        saved.setTitle(request.getBaslik());
        saved.setUser(user);
        saved.setSlug("test-story-2");

        when(storyRepository.save(any(Story.class))).thenReturn(saved);

        StoryResponse response = storyService.create(userId, request);

        assertNotNull(response);
        verify(storyRepository, atLeast(2)).existsBySlug(anyString());
    }

    @Test
    void approve_shouldHandleNullUsername() {
        Long storyId = 1L;
        Long adminId = 2L;
        Long authorId = 3L;

        User author = new User();
        author.setId(authorId);
        author.setUsername(null);
        author.setFirstName("John");

        Story story = new Story();
        story.setId(storyId);
        story.setUser(author);
        story.setStatus(Story.StoryStatus.YAYIN_BEKLIYOR);
        story.setTitle("Test Story");

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(followRepository.findFollowersByFollowedId(authorId)).thenReturn(List.of());

        storyService.approve(storyId, adminId);

        assertEquals(Story.StoryStatus.YAYINLANDI, story.getStatus());
        verify(storyRepository, times(1)).save(story);
    }

    @Test
    void approve_shouldHandleNullUsernameAndFirstName() {
        Long storyId = 1L;
        Long adminId = 2L;
        Long authorId = 3L;

        User author = new User();
        author.setId(authorId);
        author.setUsername(null);
        author.setFirstName(null);

        Story story = new Story();
        story.setId(storyId);
        story.setUser(author);
        story.setStatus(Story.StoryStatus.YAYIN_BEKLIYOR);
        story.setTitle("Test Story");

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(followRepository.findFollowersByFollowedId(authorId)).thenReturn(List.of());

        storyService.approve(storyId, adminId);

        assertEquals(Story.StoryStatus.YAYINLANDI, story.getStatus());
        verify(storyRepository, times(1)).save(story);
    }

    @Test
    void update_shouldUpdateTags() {
        Long storyId = 1L;
        Long userId = 2L;

        User owner = new User();
        owner.setId(userId);
        owner.setUsername("owner");
        Role role = new Role();
        role.setName("WRITER");
        owner.setRoles(Set.of(role));

        Story story = new Story();
        story.setId(storyId);
        story.setUser(owner);
        story.setTitle("Old Title");

        StoryUpdateRequest request = new StoryUpdateRequest();
        request.setBaslik("New Title");
        request.setEtiketler(List.of("new-tag"));

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(storyRepository.existsBySlug(anyString())).thenReturn(false);
        when(tagRepository.findByName("new-tag")).thenReturn(Optional.empty());
        when(tagRepository.existsBySlug(anyString())).thenReturn(false);

        Story saved = new Story();
        saved.setId(storyId);
        saved.setTitle("New Title");
        saved.setUser(owner);
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(storyRepository.save(any(Story.class))).thenReturn(saved);

        StoryResponse response = storyService.update(storyId, userId, request);

        assertNotNull(response);
        verify(tagRepository, atLeast(1)).save(any(Tag.class));
        verify(storyRepository, times(1)).save(any(Story.class));
    }

    @Test
    void update_shouldThrowExceptionWhenCategoryNotFound() {
        Long storyId = 1L;
        Long userId = 2L;
        Long categoryId = 999L;

        User owner = new User();
        owner.setId(userId);

        Story story = new Story();
        story.setId(storyId);
        story.setUser(owner);

        StoryUpdateRequest request = new StoryUpdateRequest();
        request.setKategoriId(categoryId);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> storyService.update(storyId, userId, request));
    }

    @Test
    void toggleEditorPick_shouldToggleFromTrueToFalse() {
        Long storyId = 1L;
        Long adminId = 2L;

        Story story = new Story();
        story.setId(storyId);
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setIsEditorPick(true);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

        storyService.toggleEditorPick(storyId, adminId);

        assertFalse(story.getIsEditorPick());
        verify(storyRepository, times(1)).save(story);
    }
}

