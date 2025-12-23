package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.AuthorProfileUpdateRequest;
import com.example.backend.application.dto.response.AuthorProfileResponse;
import com.example.backend.domain.entity.AuthorProfile;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.AuthorProfileRepository;
import com.example.backend.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorProfileServiceImplTest {

    @Mock
    private AuthorProfileRepository authorProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthorProfileServiceImpl authorProfileService;

    @Test
    void createOrUpdate_shouldCreateNewProfileWhenNotExists() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("author");

        AuthorProfileUpdateRequest request = new AuthorProfileUpdateRequest();
        request.setBio("Test bio");
        request.setAvatarUrl("http://example.com/avatar.jpg");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(authorProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        AuthorProfile saved = new AuthorProfile();
        saved.setId(10L);
        saved.setUser(user);
        saved.setBio("Test bio");
        saved.setAvatarUrl("http://example.com/avatar.jpg");

        when(authorProfileRepository.save(any(AuthorProfile.class))).thenReturn(saved);

        AuthorProfileResponse response = authorProfileService.createOrUpdate(userId, request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Test bio", response.getBio());
        verify(authorProfileRepository, times(1)).save(any(AuthorProfile.class));
    }

    @Test
    void createOrUpdate_shouldUpdateExistingProfile() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("author");

        AuthorProfile existing = new AuthorProfile();
        existing.setId(10L);
        existing.setUser(user);
        existing.setBio("Old bio");

        AuthorProfileUpdateRequest request = new AuthorProfileUpdateRequest();
        request.setBio("New bio");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(authorProfileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        AuthorProfile saved = new AuthorProfile();
        saved.setId(10L);
        saved.setUser(user);
        saved.setBio("New bio");
        when(authorProfileRepository.save(any(AuthorProfile.class))).thenReturn(saved);

        AuthorProfileResponse response = authorProfileService.createOrUpdate(userId, request);

        assertNotNull(response);
        assertEquals("New bio", response.getBio());
        verify(authorProfileRepository, times(1)).save(any(AuthorProfile.class));
    }
}

