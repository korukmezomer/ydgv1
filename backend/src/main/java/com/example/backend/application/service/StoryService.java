package com.example.backend.application.service;

import com.example.backend.application.dto.request.StoryCreateRequest;
import com.example.backend.application.dto.request.StoryUpdateRequest;
import com.example.backend.application.dto.response.StoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StoryService {
    
    StoryResponse create(Long userId, StoryCreateRequest request);
    
    StoryResponse findById(Long id);
    
    StoryResponse findBySlug(String slug);
    
    Page<StoryResponse> findAll(Pageable pageable);
    
    Page<StoryResponse> findByUserId(Long userId, Pageable pageable);
    
    Page<StoryResponse> findByCategoryId(Long categoryId, Pageable pageable);
    
    Page<StoryResponse> findByStatus(com.example.backend.domain.entity.Story.StoryStatus status, Pageable pageable);
    
    Page<StoryResponse> findPublishedStories(Pageable pageable);
    
    Page<StoryResponse> findPopularStories(Pageable pageable);
    
    Page<StoryResponse> findEditorPicks(Pageable pageable);
    
    void toggleEditorPick(Long storyId, Long adminId);
    
    Page<StoryResponse> search(String query, Pageable pageable);
    
    Page<StoryResponse> findByTagId(Long tagId, Pageable pageable);
    
    StoryResponse update(Long id, Long userId, StoryUpdateRequest request);
    
    void delete(Long id, Long userId);
    
    void publish(Long id, Long userId);
    
    void approve(Long id, Long adminId);
    
    void reject(Long id, Long adminId, String reason);
}

