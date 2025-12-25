package com.example.backend.integration;

import com.example.backend.application.service.SavedStoryService;
import com.example.backend.domain.entity.*;
import com.example.backend.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SavedStoryServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SavedStoryService savedStoryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private SavedStoryRepository savedStoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Story testStory;

    @BeforeEach
    void setUp() {
        Role userRole = createRoleIfNotExists("USER");
        
        testUser = new User();
        testUser.setEmail("saver@test.com");
        testUser.setUsername("saver");
        testUser.setFirstName("Saver");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setIsActive(true);
        testUser.setRoles(Set.of(userRole));
        testUser = userRepository.save(testUser);

        User writer = createTestWriter();
        testStory = createTestStory("Story to Save", writer.getId());
    }

    @Test
    void testSaveStory() {
        savedStoryService.saveStory(testUser.getId(), testStory.getId());

        // SavedStory kaydı oluşmalı
        SavedStory savedStory = savedStoryRepository.findByUserIdAndStoryId(testUser.getId(), testStory.getId())
                .orElse(null);
        assertNotNull(savedStory);
        assertEquals(testUser.getId(), savedStory.getUser().getId());
        assertEquals(testStory.getId(), savedStory.getStory().getId());
    }

    @Test
    void testUnsaveStory() {
        // Önce kaydet
        savedStoryService.saveStory(testUser.getId(), testStory.getId());

        // Kaydı geri al
        savedStoryService.removeStory(testUser.getId(), testStory.getId());

        // Soft delete yapılıyor, kayıt hala veritabanında ama aktif değil
        SavedStory savedStory = savedStoryRepository.findByUserIdAndStoryId(testUser.getId(), testStory.getId())
                .orElse(null);
        assertNotNull(savedStory);
        assertFalse(savedStory.getIsActive());
    }

    @Test
    void testIsSaved() {
        // Önce kaydet
        savedStoryService.saveStory(testUser.getId(), testStory.getId());

        // Kaydedilmiş mi kontrol et
        boolean isSaved = savedStoryService.isSaved(testUser.getId(), testStory.getId());
        assertTrue(isSaved);

        // Kaydı geri al
        savedStoryService.removeStory(testUser.getId(), testStory.getId());

        // Artık kaydedilmemiş olmalı
        boolean isNotSaved = savedStoryService.isSaved(testUser.getId(), testStory.getId());
        assertFalse(isNotSaved);
    }

    private Story createTestStory(String title, Long userId) {
        Story story = new Story();
        story.setTitle(title);
        story.setContent("Content");
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

