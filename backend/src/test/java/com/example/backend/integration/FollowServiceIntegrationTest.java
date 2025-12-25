package com.example.backend.integration;

import com.example.backend.application.service.FollowService;
import com.example.backend.domain.entity.*;
import com.example.backend.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FollowServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private FollowService followService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User follower;
    private User following;

    @BeforeEach
    void setUp() {
        Role userRole = createRoleIfNotExists("USER");
        
        // Follower kullanıcı
        follower = new User();
        follower.setEmail("follower@test.com");
        follower.setUsername("follower");
        follower.setFirstName("Follower");
        follower.setPassword(passwordEncoder.encode("password123"));
        follower.setIsActive(true);
        follower.setRoles(Set.of(userRole));
        follower = userRepository.save(follower);

        // Following kullanıcı
        following = new User();
        following.setEmail("following@test.com");
        following.setUsername("following");
        following.setFirstName("Following");
        following.setPassword(passwordEncoder.encode("password123"));
        following.setIsActive(true);
        following.setRoles(Set.of(userRole));
        following = userRepository.save(following);
    }

    @Test
    void testFollowUser() {
        followService.follow(follower.getId(), following.getId());

        // Follow kaydı oluşmalı
        Follow follow = followRepository.findByFollowerIdAndFollowedId(follower.getId(), following.getId()).orElse(null);
        assertNotNull(follow);
        assertEquals(follower.getId(), follow.getFollower().getId());
        assertEquals(following.getId(), follow.getFollowed().getId());
    }

    @Test
    void testUnfollowUser() {
        // Önce takip et
        followService.follow(follower.getId(), following.getId());

        // Takibi bırak
        followService.unfollow(follower.getId(), following.getId());

        // Follow kaydı silinmeli
        Follow follow = followRepository.findByFollowerIdAndFollowedId(follower.getId(), following.getId()).orElse(null);
        assertNull(follow);
    }

    @Test
    void testFollowSelf() {
        // Kendini takip etmeye çalış
        assertThrows(Exception.class, () -> {
            followService.follow(follower.getId(), follower.getId());
        });
    }

    @Test
    void testFollowAlreadyFollowing() {
        // İlk takip
        followService.follow(follower.getId(), following.getId());

        // Tekrar takip etmeye çalış (exception fırlatmalı)
        assertThrows(Exception.class, () -> {
            followService.follow(follower.getId(), following.getId());
        });

        // Sadece bir follow kaydı olmalı
        boolean exists = followRepository.existsByFollowerIdAndFollowedId(follower.getId(), following.getId());
        assertTrue(exists);
    }

    @Test
    void testGetFollowers() {
        // Birkaç kullanıcı oluştur ve following'i takip etsinler
        User follower1 = createTestUser("follower1@test.com", "follower1");
        User follower2 = createTestUser("follower2@test.com", "follower2");

        followService.follow(follower1.getId(), following.getId());
        followService.follow(follower2.getId(), following.getId());

        // Followers listesini al
        var followers = followService.getFollowers(following.getId());

        assertTrue(followers.size() >= 2);
        assertTrue(followers.stream().anyMatch(f -> f.getId().equals(follower1.getId())));
        assertTrue(followers.stream().anyMatch(f -> f.getId().equals(follower2.getId())));
    }

    private User createTestUser(String email, String username) {
        Role userRole = createRoleIfNotExists("USER");
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setFirstName(username);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setIsActive(true);
        user.setRoles(Set.of(userRole));
        return userRepository.save(user);
    }

    private Role createRoleIfNotExists(String roleName) {
        return roleRepository.findByName(roleName).orElseGet(() -> {
            Role role = new Role();
            role.setName(roleName);
            role.setIsActive(true);
            return roleRepository.save(role);
        });
    }
}

