package com.example.backend.application.service.impl;

import com.example.backend.application.dto.response.UserResponse;
import com.example.backend.application.service.FollowService;
import com.example.backend.application.exception.BadRequestException;
import com.example.backend.application.exception.ForbiddenException;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.application.service.NotificationService;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.entity.Follow;
import com.example.backend.domain.entity.Notification;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.domain.repository.FollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FollowServiceImpl implements FollowService {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void follow(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            throw new BadRequestException("Kendinizi takip edemezsiniz");
        }

        if (followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
            throw new BadRequestException("Bu kullanıcıyı zaten takip ediyorsunuz");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new ResourceNotFoundException("Takip edilecek kullanıcı bulunamadı"));

        // Admin kullanıcıları takip edilemez
        boolean isAdmin = followed.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getName()));
        if (isAdmin) {
            throw new ForbiddenException("Admin kullanıcıları takip edilemez");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        followRepository.save(follow);

        // Takip edilen kullanıcıya bildirim gönder
        String followerName = follower.getUsername() != null 
            ? follower.getUsername() 
            : (follower.getFirstName() != null ? follower.getFirstName() : "Bir kullanıcı");
        
        notificationService.createNotification(
            followedId,
            "Yeni Takipçi",
            followerName + " sizi takip etmeye başladı",
            Notification.NotificationType.YENI_TAKIPCI,
            null,
            null
        );
    }

    @Override
    public void unfollow(Long followerId, Long followedId) {
        Follow follow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId)
                .orElseThrow(() -> new ResourceNotFoundException("Takip kaydı bulunamadı"));
        followRepository.delete(follow);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followedId) {
        return followRepository.existsByFollowerIdAndFollowedId(followerId, followedId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getFollowerCount(Long userId) {
        return followRepository.countByFollowedId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getFollowingCount(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getFollowers(Long userId) {
        List<User> followers = followRepository.findFollowersByFollowedId(userId);
        return followers.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getFollowing(Long userId) {
        List<User> following = followRepository.findFollowedByFollowerId(userId);
        return following.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setUsername(user.getUsername());
        response.setRoles(user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet()));
        response.setCreatedAt(user.getCreatedAt());
        response.setIsActive(user.getIsActive());
        return response;
    }
}

