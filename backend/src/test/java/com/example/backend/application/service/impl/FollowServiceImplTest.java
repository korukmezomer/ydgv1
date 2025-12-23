package com.example.backend.application.service.impl;

import com.example.backend.application.dto.response.UserResponse;
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
}

