package com.example.backend.application.service;

public interface LikeService {
    
    void like(Long storyId, Long userId);
    
    void unlike(Long storyId, Long userId);
    
    boolean isLiked(Long storyId, Long userId);
    
    Long getLikeCount(Long storyId);
}

