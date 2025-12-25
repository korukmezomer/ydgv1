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
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        testUser.setPassword(passwordEncoder.encode("password123"));
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

    @Test
    void testFindByCategoryId() {
        Story story = createTestStory("Category Story", testUser.getId());
        story.setCategory(testCategory);
        storyRepository.save(story);

        Pageable pageable = PageRequest.of(0, 10);
        Page<StoryResponse> stories = storyService.findByCategoryId(testCategory.getId(), pageable);

        assertTrue(stories.getTotalElements() >= 1);
    }

    @Test
    void testFindByStatus() {
        Story story = createTestStory("Status Story", testUser.getId());
        story.setStatus(Story.StoryStatus.YAYIN_BEKLIYOR);
        storyRepository.save(story);

        Pageable pageable = PageRequest.of(0, 10);
        Page<StoryResponse> stories = storyService.findByStatus(Story.StoryStatus.YAYIN_BEKLIYOR, pageable);

        assertTrue(stories.getTotalElements() >= 1);
    }

    @Test
    void testFindPublishedStories() {
        Story story = createTestStory("Published Story", testUser.getId());
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        storyRepository.save(story);

        Pageable pageable = PageRequest.of(0, 10);
        Page<StoryResponse> stories = storyService.findPublishedStories(pageable);

        assertTrue(stories.getTotalElements() >= 1);
    }

    @Test
    void testFindPopularStories() {
        Story story = createTestStory("Popular Story", testUser.getId());
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setLikeCount(100L);
        storyRepository.save(story);

        Pageable pageable = PageRequest.of(0, 10);
        Page<StoryResponse> stories = storyService.findPopularStories(pageable);

        assertTrue(stories.getTotalElements() >= 1);
    }

    @Test
    void testFindEditorPicks() {
        Story story = createTestStory("Editor Pick Story", testUser.getId());
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setIsEditorPick(true);
        storyRepository.save(story);

        Pageable pageable = PageRequest.of(0, 10);
        Page<StoryResponse> stories = storyService.findEditorPicks(pageable);

        assertTrue(stories.getTotalElements() >= 1);
    }

    @Test
    void testSearch() {
        Story story = createTestStory("Search Test Story", testUser.getId());
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        storyRepository.save(story);

        Pageable pageable = PageRequest.of(0, 10);
        Page<StoryResponse> stories = storyService.search("Search", pageable);

        assertTrue(stories.getTotalElements() >= 1);
    }

    @Test
    void testUpdateStory() {
        Story story = createTestStory("Update Story", testUser.getId());
        
        com.example.backend.application.dto.request.StoryUpdateRequest request = 
            new com.example.backend.application.dto.request.StoryUpdateRequest();
        request.setBaslik("Updated Title");
        request.setIcerik("Updated content");

        StoryResponse response = storyService.update(story.getId(), testUser.getId(), request);

        assertEquals("Updated Title", response.getBaslik());
        assertEquals("Updated content", response.getIcerik());
    }

    @Test
    void testDeleteStory() {
        Story story = createTestStory("Delete Story", testUser.getId());

        storyService.delete(story.getId(), testUser.getId());

        Story deletedStory = storyRepository.findById(story.getId()).orElse(null);
        assertNotNull(deletedStory);
        assertFalse(deletedStory.getIsActive());
    }

    @Test
    void testPublishStory() {
        Story story = createTestStory("Publish Story", testUser.getId());

        storyService.publish(story.getId(), testUser.getId());

        Story publishedStory = storyRepository.findById(story.getId()).orElse(null);
        assertNotNull(publishedStory);
        assertEquals(Story.StoryStatus.YAYIN_BEKLIYOR, publishedStory.getStatus());
    }

    @Test
    void testRejectStory() {
        Story story = createTestStory("Reject Story", testUser.getId());
        story.setStatus(Story.StoryStatus.YAYIN_BEKLIYOR);
        storyRepository.save(story);

        Long adminId = testUser.getId();
        storyService.reject(story.getId(), adminId, "Uygunsuz içerik");

        Story rejectedStory = storyRepository.findById(story.getId()).orElse(null);
        assertNotNull(rejectedStory);
        assertEquals(Story.StoryStatus.REDDEDILDI, rejectedStory.getStatus());
    }

    @Test
    void testFindBySlug() {
        Story story = createTestStory("Slug Test Story", testUser.getId());

        StoryResponse response = storyService.findBySlug(story.getSlug());

        assertNotNull(response);
        assertEquals(story.getSlug(), response.getSlug());
    }

    @Test
    void testFindAll() {
        createTestStory("All Story 1", testUser.getId());
        createTestStory("All Story 2", testUser.getId());

        Pageable pageable = PageRequest.of(0, 10);
        Page<StoryResponse> stories = storyService.findAll(pageable);

        assertTrue(stories.getTotalElements() >= 2);
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

