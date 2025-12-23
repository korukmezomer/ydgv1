package com.example.backend.integration;

import com.example.backend.application.dto.request.StoryCreateRequest;
import com.example.backend.application.dto.response.StoryResponse;
import com.example.backend.application.service.StoryService;
import com.example.backend.domain.entity.Category;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.CategoryRepository;
import com.example.backend.domain.repository.RoleRepository;
import com.example.backend.domain.repository.StoryRepository;
import com.example.backend.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StoryServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private StoryService storyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StoryRepository storyRepository;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Test kullanıcısı oluştur
        Role writerRole = createRoleIfNotExists("WRITER");
        
        testUser = new User();
        testUser.setEmail("writer@test.com");
        testUser.setUsername("testwriter");
        testUser.setFirstName("Test");
        testUser.setLastName("Writer");
        testUser.setIsActive(true);
        testUser.setRoles(Set.of(writerRole));
        testUser = userRepository.save(testUser);

        // Test kategorisi oluştur
        testCategory = new Category();
        testCategory.setName("Technology");
        testCategory.setSlug("technology");
        testCategory.setDescription("Tech category");
        testCategory = categoryRepository.save(testCategory);
    }

    @Test
    void testCreateStory() {
        StoryCreateRequest request = new StoryCreateRequest();
        request.setBaslik("Test Story Title");
        request.setIcerik("Test story content");
        request.setOzet("Test summary");
        request.setKategoriId(testCategory.getId());

        StoryResponse response = storyService.create(testUser.getId(), request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Test Story Title", response.getBaslik());
        assertEquals("Test story content", response.getIcerik());
        assertEquals(Story.StoryStatus.TASLAK, response.getDurum());
        assertNotNull(response.getSlug());
        
        // Veritabanında kontrol et
        Story savedStory = storyRepository.findById(response.getId()).orElse(null);
        assertNotNull(savedStory);
        assertEquals(testUser.getId(), savedStory.getUser().getId());
    }

    @Test
    void testFindByUserId() {
        // Birkaç story oluştur
        createTestStory("Story 1", testUser.getId());
        createTestStory("Story 2", testUser.getId());

        Pageable pageable = PageRequest.of(0, 10);
        Page<StoryResponse> stories = storyService.findByUserId(testUser.getId(), pageable);

        assertTrue(stories.getTotalElements() >= 2);
        assertTrue(stories.getContent().stream()
                .anyMatch(s -> s.getBaslik().equals("Story 1")));
    }

    @Test
    void testApproveStory() {
        Story story = createTestStory("Pending Story", testUser.getId());
        story.setStatus(Story.StoryStatus.YAYIN_BEKLIYOR);
        story = storyRepository.save(story);

        Long adminId = testUser.getId(); // Test için aynı kullanıcıyı admin olarak kullan

        storyService.approve(story.getId(), adminId);

        Story approvedStory = storyRepository.findById(story.getId()).orElse(null);
        assertNotNull(approvedStory);
        assertEquals(Story.StoryStatus.YAYINLANDI, approvedStory.getStatus());
        assertNotNull(approvedStory.getPublishedAt());
    }

    @Test
    void testToggleEditorPick() {
        Story story = createTestStory("Editor Pick Story", testUser.getId());
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setIsEditorPick(false);
        story = storyRepository.save(story);

        Long adminId = testUser.getId();
        storyService.toggleEditorPick(story.getId(), adminId);

        Story updatedStory = storyRepository.findById(story.getId()).orElse(null);
        assertNotNull(updatedStory);
        assertTrue(updatedStory.getIsEditorPick());
    }

    private Story createTestStory(String title, Long userId) {
        Story story = new Story();
        story.setTitle(title);
        story.setContent("Content for " + title);
        story.setSummary("Summary for " + title);
        story.setSlug(title.toLowerCase().replace(" ", "-"));
        story.setUser(userRepository.findById(userId).orElse(testUser));
        story.setStatus(Story.StoryStatus.TASLAK);
        story.setViewCount(0L);
        story.setLikeCount(0L);
        story.setCommentCount(0L);
        story.setIsActive(true);
        return storyRepository.save(story);
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

