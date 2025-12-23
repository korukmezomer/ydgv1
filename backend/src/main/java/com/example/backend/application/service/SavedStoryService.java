package com.example.backend.application.service;

import com.example.backend.application.dto.response.StoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SavedStoryService {
    
    void saveStory(Long userId, Long storyId);
    
    void removeStory(Long userId, Long storyId);
    
    boolean isSaved(Long userId, Long storyId);
    
    Page<StoryResponse> findByUserId(Long userId, Pageable pageable);
}

