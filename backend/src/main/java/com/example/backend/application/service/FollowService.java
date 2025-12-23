package com.example.backend.application.service;

import com.example.backend.application.dto.response.UserResponse;

import java.util.List;

public interface FollowService {
    
    void follow(Long followerId, Long followedId);
    
    void unfollow(Long followerId, Long followedId);
    
    boolean isFollowing(Long followerId, Long followedId);
    
    Long getFollowerCount(Long userId);
    
    Long getFollowingCount(Long userId);
    
    List<UserResponse> getFollowers(Long userId);
    
    List<UserResponse> getFollowing(Long userId);
}

