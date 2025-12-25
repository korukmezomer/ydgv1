package com.example.backend.integration;

import com.example.backend.domain.entity.Follow;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.FollowRepository;
import com.example.backend.domain.repository.RoleRepository;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.infrastructure.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FollowControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;
    private User follower;
    private User followed;
    private String followerToken;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        Role userRole = createRoleIfNotExists("USER");
        
        follower = new User();
        follower.setEmail("follower@test.com");
        follower.setUsername("follower");
        follower.setPassword(passwordEncoder.encode("password123"));
        follower.setIsActive(true);
        follower.setRoles(Set.of(userRole));
        follower = userRepository.save(follower);
        
        followed = new User();
        followed.setEmail("followed@test.com");
        followed.setUsername("followed");
        followed.setPassword(passwordEncoder.encode("password123"));
        followed.setIsActive(true);
        followed.setRoles(Set.of(userRole));
        followed = userRepository.save(followed);
        
        Set<String> followerRoles = follower.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
        followerToken = "Bearer " + jwtUtil.generateToken(follower.getEmail(), follower.getId(), followerRoles);
    }

    @Test
    void testFollow() throws Exception {
        mockMvc.perform(post("/api/takip/{takipEdilenId}", followed.getId())
                        .header("Authorization", followerToken))
                .andExpect(status().isOk());

        assertTrue(followRepository.existsByFollowerIdAndFollowedId(follower.getId(), followed.getId()));
    }

    @Test
    void testUnfollow() throws Exception {
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        follow.setIsActive(true);
        followRepository.save(follow);

        mockMvc.perform(delete("/api/takip/{takipEdilenId}", followed.getId())
                        .header("Authorization", followerToken))
                .andExpect(status().isNoContent());

        assertFalse(followRepository.existsByFollowerIdAndFollowedId(follower.getId(), followed.getId()));
    }

    @Test
    void testIsFollowing() throws Exception {
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        follow.setIsActive(true);
        followRepository.save(follow);

        mockMvc.perform(get("/api/takip/{takipEdilenId}/durum", followed.getId())
                        .header("Authorization", followerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void testGetFollowerCount() throws Exception {
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        follow.setIsActive(true);
        followRepository.save(follow);

        mockMvc.perform(get("/api/takip/{kullaniciId}/takipci-sayisi", followed.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    void testGetFollowingCount() throws Exception {
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        follow.setIsActive(true);
        followRepository.save(follow);

        mockMvc.perform(get("/api/takip/{kullaniciId}/takip-edilen-sayisi", follower.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    void testGetFollowers() throws Exception {
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        follow.setIsActive(true);
        followRepository.save(follow);

        mockMvc.perform(get("/api/takip/{kullaniciId}/takipciler", followed.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(follower.getId()));
    }

    @Test
    void testGetFollowing() throws Exception {
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        follow.setIsActive(true);
        followRepository.save(follow);

        mockMvc.perform(get("/api/takip/{kullaniciId}/takip-edilenler", follower.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(followed.getId()));
    }

    @Test
    void testFollowUnauthorized() throws Exception {
        mockMvc.perform(post("/api/takip/{takipEdilenId}", followed.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testFollowSelf() throws Exception {
        mockMvc.perform(post("/api/takip/{takipEdilenId}", follower.getId())
                        .header("Authorization", followerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFollowAlreadyFollowing() throws Exception {
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        follow.setIsActive(true);
        followRepository.save(follow);

        mockMvc.perform(post("/api/takip/{takipEdilenId}", followed.getId())
                        .header("Authorization", followerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFollowAdmin() throws Exception {
        Role adminRole = createRoleIfNotExists("ADMIN");
        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setIsActive(true);
        admin.setRoles(Set.of(adminRole));
        admin = userRepository.save(admin);

        mockMvc.perform(post("/api/takip/{takipEdilenId}", admin.getId())
                        .header("Authorization", followerToken))
                .andExpect(status().isForbidden());
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

