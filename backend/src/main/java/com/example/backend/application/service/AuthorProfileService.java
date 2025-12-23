package com.example.backend.application.service;

import com.example.backend.application.dto.request.AuthorProfileUpdateRequest;
import com.example.backend.application.dto.response.AuthorProfileResponse;

public interface AuthorProfileService {
    
    AuthorProfileResponse findByUserId(Long userId);
    
    AuthorProfileResponse createOrUpdate(Long userId, AuthorProfileUpdateRequest request);
    
    AuthorProfileResponse update(Long userId, AuthorProfileUpdateRequest request);
}

