package com.example.backend.application.service.impl;

import com.example.backend.application.dto.response.StoryResponse;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.domain.entity.SavedStory;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.repository.SavedStoryRepository;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavedStoryServiceImplTest {

    @Mock
    private SavedStoryRepository savedStoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoryRepository storyRepository;

    @InjectMocks
    private SavedStoryServiceImpl savedStoryService;

    @Test
    void saveStory_shouldCreateSavedStory() {
        Long userId = 1L;
        Long storyId = 2L;

        when(savedStoryRepository.existsByUserIdAndStoryId(userId, storyId)).thenReturn(false);

        User user = new User();
        user.setId(userId);

        Story story = new Story();
        story.setId(storyId);
        story.setTitle("Test Story");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

        savedStoryService.saveStory(userId, storyId);

        verify(savedStoryRepository, times(1)).save(any(SavedStory.class));
    }

    @Test
    void saveStory_shouldNotCreateWhenAlreadySaved() {
        Long userId = 1L;
        Long storyId = 2L;

        when(savedStoryRepository.existsByUserIdAndStoryId(userId, storyId)).thenReturn(true);

        savedStoryService.saveStory(userId, storyId);

        verify(savedStoryRepository, never()).save(any(SavedStory.class));
    }

    @Test
    void removeStory_shouldSetSavedStoryInactive() {
        Long userId = 1L;
        Long storyId = 2L;

        SavedStory savedStory = new SavedStory();
        savedStory.setIsActive(true);

        when(savedStoryRepository.findActiveByUserIdAndStoryId(userId, storyId))
                .thenReturn(Optional.of(savedStory));

        savedStoryService.removeStory(userId, storyId);

        assertFalse(savedStory.getIsActive());
        verify(savedStoryRepository, times(1)).save(savedStory);
    }

    @Test
    void isSaved_shouldReturnTrueWhenStoryIsSaved() {
        Long userId = 1L;
        Long storyId = 2L;

        when(savedStoryRepository.existsByUserIdAndStoryId(userId, storyId)).thenReturn(true);

        boolean result = savedStoryService.isSaved(userId, storyId);

        assertTrue(result);
    }

    @Test
    void isSaved_shouldReturnFalseWhenStoryIsNotSaved() {
        Long userId = 1L;
        Long storyId = 2L;

        when(savedStoryRepository.existsByUserIdAndStoryId(userId, storyId)).thenReturn(false);

        boolean result = savedStoryService.isSaved(userId, storyId);

        assertFalse(result);
    }

    @Test
    void saveStory_shouldThrowExceptionWhenUserNotFound() {
        Long userId = 999L;
        Long storyId = 2L;

        when(savedStoryRepository.existsByUserIdAndStoryId(userId, storyId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> savedStoryService.saveStory(userId, storyId));
        verify(savedStoryRepository, never()).save(any(SavedStory.class));
    }

    @Test
    void saveStory_shouldThrowExceptionWhenStoryNotFound() {
        Long userId = 1L;
        Long storyId = 999L;

        User user = new User();
        user.setId(userId);

        when(savedStoryRepository.existsByUserIdAndStoryId(userId, storyId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storyRepository.findById(storyId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> savedStoryService.saveStory(userId, storyId));
        verify(savedStoryRepository, never()).save(any(SavedStory.class));
    }

    @Test
    void removeStory_shouldThrowExceptionWhenSavedStoryNotFound() {
        Long userId = 1L;
        Long storyId = 2L;

        when(savedStoryRepository.findActiveByUserIdAndStoryId(userId, storyId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> savedStoryService.removeStory(userId, storyId));
        verify(savedStoryRepository, never()).save(any(SavedStory.class));
    }

    @Test
    void findByUserId_shouldReturnPageOfSavedStories() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        Role role = new Role();
        role.setName("USER");
        user.setRoles(Set.of(role));

        Story story = new Story();
        story.setId(1L);
        story.setTitle("Test Story");
        story.setUser(user);

        SavedStory savedStory = new SavedStory();
        savedStory.setId(1L);
        savedStory.setUser(user);
        savedStory.setStory(story);
        savedStory.setIsActive(true);

        Page<SavedStory> savedStoryPage = new PageImpl<>(List.of(savedStory));
        when(savedStoryRepository.findByUserId(userId, pageable)).thenReturn(savedStoryPage);

        Page<StoryResponse> response = savedStoryService.findByUserId(userId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1L, response.getContent().get(0).getId());
        verify(savedStoryRepository, times(1)).findByUserId(userId, pageable);
    }
}

