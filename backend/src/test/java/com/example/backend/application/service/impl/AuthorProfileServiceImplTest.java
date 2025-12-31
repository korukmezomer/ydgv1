package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.AuthorProfileUpdateRequest;
import com.example.backend.application.dto.response.AuthorProfileResponse;
import com.example.backend.application.exception.ResourceNotFoundException;
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

    @Test
    void findByUserId_shouldReturnAuthorProfileResponse() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("author");

        AuthorProfile profile = new AuthorProfile();
        profile.setId(10L);
        profile.setUser(user);
        profile.setBio("Test bio");

        when(authorProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        AuthorProfileResponse response = authorProfileService.findByUserId(userId);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Test bio", response.getBio());
    }

    @Test
    void findByUserId_shouldReturnNullWhenProfileNotFound() {
        Long userId = 1L;

        when(authorProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        AuthorProfileResponse response = authorProfileService.findByUserId(userId);

        assertNull(response);
    }

    @Test
    void createOrUpdate_shouldThrowExceptionWhenUserNotFound() {
        Long userId = 999L;
        AuthorProfileUpdateRequest request = new AuthorProfileUpdateRequest();
        request.setBio("Test bio");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authorProfileService.createOrUpdate(userId, request));
        verify(authorProfileRepository, never()).save(any(AuthorProfile.class));
    }

    @Test
    void createOrUpdate_shouldUpdateOnlyProvidedFields() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        AuthorProfile existing = new AuthorProfile();
        existing.setId(10L);
        existing.setUser(user);
        existing.setBio("Old bio");
        existing.setAvatarUrl("old-avatar.jpg");

        AuthorProfileUpdateRequest request = new AuthorProfileUpdateRequest();
        request.setBio("New bio");
        // avatarUrl not provided, should remain unchanged

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(authorProfileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        AuthorProfile saved = new AuthorProfile();
        saved.setId(10L);
        saved.setUser(user);
        saved.setBio("New bio");
        saved.setAvatarUrl("old-avatar.jpg");
        when(authorProfileRepository.save(any(AuthorProfile.class))).thenReturn(saved);

        AuthorProfileResponse response = authorProfileService.createOrUpdate(userId, request);

        assertNotNull(response);
        assertEquals("New bio", response.getBio());
        verify(authorProfileRepository, times(1)).save(any(AuthorProfile.class));
    }

    @Test
    void createOrUpdate_shouldSetAllFields() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        AuthorProfileUpdateRequest request = new AuthorProfileUpdateRequest();
        request.setBio("Test bio");
        request.setAvatarUrl("avatar.jpg");
        request.setWebsite("https://example.com");
        request.setTwitterHandle("@test");
        request.setLinkedinUrl("https://linkedin.com/in/test");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(authorProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        AuthorProfile saved = new AuthorProfile();
        saved.setId(10L);
        saved.setUser(user);
        saved.setBio("Test bio");
        saved.setAvatarUrl("avatar.jpg");
        saved.setWebsite("https://example.com");
        saved.setTwitterHandle("@test");
        saved.setLinkedinUrl("https://linkedin.com/in/test");
        when(authorProfileRepository.save(any(AuthorProfile.class))).thenReturn(saved);

        AuthorProfileResponse response = authorProfileService.createOrUpdate(userId, request);

        assertNotNull(response);
        assertEquals("Test bio", response.getBio());
        assertEquals("avatar.jpg", response.getAvatarUrl());
        assertEquals("https://example.com", response.getWebsite());
        assertEquals("@test", response.getTwitterHandle());
        assertEquals("https://linkedin.com/in/test", response.getLinkedinUrl());
    }

    @Test
    void update_shouldCallCreateOrUpdate() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        AuthorProfileUpdateRequest request = new AuthorProfileUpdateRequest();
        request.setBio("Test bio");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(authorProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        AuthorProfile saved = new AuthorProfile();
        saved.setId(10L);
        saved.setUser(user);
        saved.setBio("Test bio");
        when(authorProfileRepository.save(any(AuthorProfile.class))).thenReturn(saved);

        AuthorProfileResponse response = authorProfileService.update(userId, request);

        assertNotNull(response);
        assertEquals("Test bio", response.getBio());
    }
}

