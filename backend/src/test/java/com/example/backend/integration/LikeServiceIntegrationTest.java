package com.example.backend.integration;

import com.example.backend.application.service.LikeService;
import com.example.backend.domain.entity.*;
import com.example.backend.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LikeServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Story testStory;

    @BeforeEach
    void setUp() {
        // Test kullanıcısı oluştur
        Role userRole = createRoleIfNotExists("USER");
        
        testUser = new User();
        testUser.setEmail("liker@test.com");
        testUser.setUsername("liker");
        testUser.setFirstName("Test");
        testUser.setLastName("Liker");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setIsActive(true);
        testUser.setRoles(Set.of(userRole));
        testUser = userRepository.save(testUser);

        // Test story oluştur
        User writer = createTestWriter();
        testStory = createTestStory("Test Story for Likes", writer.getId());
    }

    @Test
    void testLikeStory() {
        Long initialLikeCount = testStory.getLikeCount();

        likeService.like(testStory.getId(), testUser.getId());

        // Story'nin beğeni sayısı artmalı
        Story updatedStory = storyRepository.findById(testStory.getId()).orElse(null);
        assertNotNull(updatedStory);
        assertEquals(initialLikeCount + 1, updatedStory.getLikeCount());

        // Like kaydı oluşmalı
        Like like = likeRepository.findByUserIdAndStoryId(testUser.getId(), testStory.getId()).orElse(null);
        assertNotNull(like);
        assertEquals(testUser.getId(), like.getUser().getId());
        assertEquals(testStory.getId(), like.getStory().getId());
    }

    @Test
    void testUnlikeStory() {
        // Önce beğeni yap
        likeService.like(testStory.getId(), testUser.getId());
        Long afterLikeCount = storyRepository.findById(testStory.getId())
                .orElse(testStory).getLikeCount();

        // Beğeniyi geri al
        likeService.unlike(testUser.getId(), testStory.getId());

        // Story'nin beğeni sayısı azalmalı
        Story updatedStory = storyRepository.findById(testStory.getId()).orElse(null);
        assertNotNull(updatedStory);
        assertEquals(afterLikeCount - 1, updatedStory.getLikeCount());

        // Like kaydı silinmeli
        Like like = likeRepository.findByUserIdAndStoryId(testUser.getId(), testStory.getId()).orElse(null);
        assertNull(like);
    }

    @Test
    void testLikeAlreadyLikedStory() {
        // İlk beğeni
        likeService.like(testStory.getId(), testUser.getId());
        Long firstLikeCount = storyRepository.findById(testStory.getId())
                .orElse(testStory).getLikeCount();

        // Aynı story'yi tekrar beğenmeye çalış (exception fırlatmalı)
        assertThrows(Exception.class, () -> {
            likeService.like(testStory.getId(), testUser.getId());
        });
        Long secondLikeCount = storyRepository.findById(testStory.getId())
                .orElse(testStory).getLikeCount();

        // Beğeni sayısı değişmemeli
        assertEquals(firstLikeCount, secondLikeCount);
    }

    private Story createTestStory(String title, Long userId) {
        Story story = new Story();
        story.setTitle(title);
        story.setContent("Content for " + title);
        story.setSummary("Summary");
        story.setSlug(title.toLowerCase().replace(" ", "-"));
        story.setUser(userRepository.findById(userId).orElse(testUser));
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setViewCount(0L);
        story.setLikeCount(0L);
        story.setCommentCount(0L);
        story.setIsActive(true);
        return storyRepository.save(story);
    }

    private User createTestWriter() {
        Role writerRole = createRoleIfNotExists("WRITER");
        User writer = new User();
        writer.setEmail("writer@test.com");
        writer.setUsername("writer");
        writer.setFirstName("Writer");
        writer.setPassword(passwordEncoder.encode("password123"));
        writer.setIsActive(true);
        writer.setRoles(Set.of(writerRole));
        return userRepository.save(writer);
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

