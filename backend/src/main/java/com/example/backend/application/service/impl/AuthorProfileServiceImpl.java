package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.AuthorProfileUpdateRequest;
import com.example.backend.application.dto.response.AuthorProfileResponse;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.application.service.AuthorProfileService;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.entity.AuthorProfile;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.domain.repository.AuthorProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthorProfileServiceImpl implements AuthorProfileService {

    @Autowired
    private AuthorProfileRepository authorProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public AuthorProfileResponse findByUserId(Long userId) {
        return authorProfileRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    public AuthorProfileResponse createOrUpdate(Long userId, AuthorProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        AuthorProfile profile = authorProfileRepository.findByUserId(userId)
                .orElse(new AuthorProfile());

        if (profile.getId() == null) {
            profile.setUser(user);
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getWebsite() != null) {
            profile.setWebsite(request.getWebsite());
        }
        if (request.getTwitterHandle() != null) {
            profile.setTwitterHandle(request.getTwitterHandle());
        }
        if (request.getLinkedinUrl() != null) {
            profile.setLinkedinUrl(request.getLinkedinUrl());
        }

        profile = authorProfileRepository.save(profile);
        return toResponse(profile);
    }

    @Override
    public AuthorProfileResponse update(Long userId, AuthorProfileUpdateRequest request) {
        return createOrUpdate(userId, request);
    }

    private AuthorProfileResponse toResponse(AuthorProfile profile) {
        AuthorProfileResponse response = new AuthorProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUser().getId());
        response.setUsername(profile.getUser().getUsername());
        response.setBio(profile.getBio());
        response.setAvatarUrl(profile.getAvatarUrl());
        response.setWebsite(profile.getWebsite());
        response.setTwitterHandle(profile.getTwitterHandle());
        response.setLinkedinUrl(profile.getLinkedinUrl());
        response.setTotalViewCount(profile.getTotalViewCount());
        response.setTotalLikeCount(profile.getTotalLikeCount());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        return response;
    }
}

