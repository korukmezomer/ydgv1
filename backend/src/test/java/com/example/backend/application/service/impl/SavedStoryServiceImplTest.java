package com.example.backend.application.service.impl;

import com.example.backend.application.dto.response.StoryResponse;
import com.example.backend.domain.entity.SavedStory;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.User;
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
}

