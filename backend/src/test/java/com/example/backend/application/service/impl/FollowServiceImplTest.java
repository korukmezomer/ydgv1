package com.example.backend.application.service.impl;

import com.example.backend.application.dto.response.UserResponse;
import com.example.backend.application.exception.BadRequestException;
import com.example.backend.application.exception.ForbiddenException;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.application.service.NotificationService;
import com.example.backend.domain.entity.Follow;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.entity.Notification;
import com.example.backend.domain.repository.FollowRepository;
import com.example.backend.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FollowServiceImpl followService;

    @Test
    void follow_shouldCreateFollowAndSendNotification() {
        Long followerId = 1L;
        Long followedId = 2L;

        when(followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)).thenReturn(false);

        User follower = new User();
        follower.setId(followerId);
        follower.setUsername("takipci");

        User followed = new User();
        followed.setId(followedId);
        Role userRole = new Role();
        userRole.setName("USER");
        followed.setRoles(Set.of(userRole));

        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followedId)).thenReturn(Optional.of(followed));

        followService.follow(followerId, followedId);

        verify(followRepository, times(1)).save(any(Follow.class));
        verify(notificationService, times(1)).createNotification(
                eq(followedId),
                eq("Yeni Takipçi"),
                contains("takipci"),
                eq(Notification.NotificationType.YENI_TAKIPCI),
                isNull(),
                isNull()
        );
    }

    @Test
    void unfollow_shouldDeleteFollow() {
        Long followerId = 1L;
        Long followedId = 2L;

        Follow follow = new Follow();
        when(followRepository.findByFollowerIdAndFollowedId(followerId, followedId)).thenReturn(Optional.of(follow));

        followService.unfollow(followerId, followedId);

        verify(followRepository, times(1)).delete(follow);
    }

    @Test
    void getFollowers_shouldMapToUserResponseList() {
        Long userId = 1L;
        User follower = new User();
        follower.setId(2L);
        follower.setEmail("test@example.com");
        follower.setFirstName("Test");
        follower.setLastName("User");
        follower.setUsername("testuser");
        follower.setRoles(Collections.emptySet());

        when(followRepository.findFollowersByFollowedId(userId)).thenReturn(List.of(follower));

        List<UserResponse> responses = followService.getFollowers(userId);

        assertEquals(1, responses.size());
        assertEquals("test@example.com", responses.get(0).getEmail());
    }

    @Test
    void follow_shouldThrowExceptionWhenFollowingSelf() {
        Long userId = 1L;

        assertThrows(BadRequestException.class, () -> followService.follow(userId, userId));
        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    void follow_shouldThrowExceptionWhenAlreadyFollowing() {
        Long followerId = 1L;
        Long followedId = 2L;

        when(followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> followService.follow(followerId, followedId));
        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    void follow_shouldThrowExceptionWhenFollowerNotFound() {
        Long followerId = 999L;
        Long followedId = 2L;

        when(followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)).thenReturn(false);
        when(userRepository.findById(followerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> followService.follow(followerId, followedId));
        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    void follow_shouldThrowExceptionWhenFollowedNotFound() {
        Long followerId = 1L;
        Long followedId = 999L;

        User follower = new User();
        follower.setId(followerId);

        when(followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)).thenReturn(false);
        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followedId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> followService.follow(followerId, followedId));
        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    void follow_shouldThrowExceptionWhenFollowingAdmin() {
        Long followerId = 1L;
        Long followedId = 2L;

        User follower = new User();
        follower.setId(followerId);

        User admin = new User();
        admin.setId(followedId);
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        admin.setRoles(Set.of(adminRole));

        when(followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)).thenReturn(false);
        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followedId)).thenReturn(Optional.of(admin));

        assertThrows(ForbiddenException.class, () -> followService.follow(followerId, followedId));
        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    void follow_shouldHandleNullUsername() {
        Long followerId = 1L;
        Long followedId = 2L;

        User follower = new User();
        follower.setId(followerId);
        follower.setUsername(null);
        follower.setFirstName("John");

        User followed = new User();
        followed.setId(followedId);
        Role userRole = new Role();
        userRole.setName("USER");
        followed.setRoles(Set.of(userRole));

        when(followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)).thenReturn(false);
        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followedId)).thenReturn(Optional.of(followed));

        Follow savedFollow = new Follow();
        savedFollow.setId(1L);
        when(followRepository.save(any(Follow.class))).thenReturn(savedFollow);

        followService.follow(followerId, followedId);

        verify(notificationService, times(1)).createNotification(
                eq(followedId),
                eq("Yeni Takipçi"),
                contains("John"),
                eq(Notification.NotificationType.YENI_TAKIPCI),
                isNull(),
                isNull()
        );
    }

    @Test
    void follow_shouldHandleNullUsernameAndFirstName() {
        Long followerId = 1L;
        Long followedId = 2L;

        User follower = new User();
        follower.setId(followerId);
        follower.setUsername(null);
        follower.setFirstName(null);

        User followed = new User();
        followed.setId(followedId);
        Role userRole = new Role();
        userRole.setName("USER");
        followed.setRoles(Set.of(userRole));

        when(followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)).thenReturn(false);
        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followedId)).thenReturn(Optional.of(followed));

        Follow savedFollow = new Follow();
        savedFollow.setId(1L);
        when(followRepository.save(any(Follow.class))).thenReturn(savedFollow);

        followService.follow(followerId, followedId);

        verify(notificationService, times(1)).createNotification(
                eq(followedId),
                eq("Yeni Takipçi"),
                contains("Bir kullanıcı"),
                eq(Notification.NotificationType.YENI_TAKIPCI),
                isNull(),
                isNull()
        );
    }

    @Test
    void unfollow_shouldThrowExceptionWhenFollowNotFound() {
        Long followerId = 1L;
        Long followedId = 2L;

        when(followRepository.findByFollowerIdAndFollowedId(followerId, followedId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> followService.unfollow(followerId, followedId));
        verify(followRepository, never()).delete(any(Follow.class));
    }

    @Test
    void isFollowing_shouldReturnTrueWhenFollowing() {
        Long followerId = 1L;
        Long followedId = 2L;

        when(followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)).thenReturn(true);

        boolean result = followService.isFollowing(followerId, followedId);

        assertTrue(result);
        verify(followRepository, times(1)).existsByFollowerIdAndFollowedId(followerId, followedId);
    }

    @Test
    void isFollowing_shouldReturnFalseWhenNotFollowing() {
        Long followerId = 1L;
        Long followedId = 2L;

        when(followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)).thenReturn(false);

        boolean result = followService.isFollowing(followerId, followedId);

        assertFalse(result);
        verify(followRepository, times(1)).existsByFollowerIdAndFollowedId(followerId, followedId);
    }

    @Test
    void getFollowerCount_shouldReturnCount() {
        Long userId = 1L;
        Long expectedCount = 10L;

        when(followRepository.countByFollowedId(userId)).thenReturn(expectedCount);

        Long result = followService.getFollowerCount(userId);

        assertEquals(expectedCount, result);
        verify(followRepository, times(1)).countByFollowedId(userId);
    }

    @Test
    void getFollowingCount_shouldReturnCount() {
        Long userId = 1L;
        Long expectedCount = 5L;

        when(followRepository.countByFollowerId(userId)).thenReturn(expectedCount);

        Long result = followService.getFollowingCount(userId);

        assertEquals(expectedCount, result);
        verify(followRepository, times(1)).countByFollowerId(userId);
    }

    @Test
    void getFollowing_shouldReturnListOfFollowingUsers() {
        Long userId = 1L;
        User following = new User();
        following.setId(2L);
        following.setEmail("test@example.com");
        following.setUsername("testuser");
        following.setRoles(Collections.emptySet());

        when(followRepository.findFollowedByFollowerId(userId)).thenReturn(List.of(following));

        List<UserResponse> responses = followService.getFollowing(userId);

        assertEquals(1, responses.size());
        assertEquals("test@example.com", responses.get(0).getEmail());
        verify(followRepository, times(1)).findFollowedByFollowerId(userId);
    }
}

