package com.example.backend.integration;

import com.example.backend.application.dto.request.StoryCreateRequest;
import com.example.backend.application.dto.request.StoryUpdateRequest;
import com.example.backend.application.dto.response.StoryResponse;
import com.example.backend.domain.entity.Category;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.CategoryRepository;
import com.example.backend.domain.repository.RoleRepository;
import com.example.backend.domain.repository.StoryRepository;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.infrastructure.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StoryControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User writer;
    private User admin;
    private Category category;
    private String writerToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Rolleri oluştur
        Role writerRole = createRoleIfNotExists("WRITER");
        Role adminRole = createRoleIfNotExists("ADMIN");
        createRoleIfNotExists("USER");
        
        // Writer kullanıcı oluştur
        writer = new User();
        writer.setEmail("writer@test.com");
        writer.setUsername("writer");
        writer.setFirstName("Writer");
        writer.setLastName("Test");
        writer.setPassword(passwordEncoder.encode("password123"));
        writer.setIsActive(true);
        writer.setRoles(Set.of(writerRole));
        writer = userRepository.save(writer);
        
        // Admin kullanıcı oluştur
        admin = new User();
        admin.setEmail("admin@test.com");
        admin.setUsername("admin");
        admin.setFirstName("Admin");
        admin.setLastName("Test");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setIsActive(true);
        admin.setRoles(Set.of(adminRole));
        admin = userRepository.save(admin);
        
        // Token oluştur
        Set<String> writerRoles = writer.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
        Set<String> adminRoles = admin.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
        writerToken = "Bearer " + jwtUtil.generateToken(writer.getEmail(), writer.getId(), writerRoles);
        adminToken = "Bearer " + jwtUtil.generateToken(admin.getEmail(), admin.getId(), adminRoles);
        
        // Kategori oluştur
        category = new Category();
        category.setName("Technology");
        category.setSlug("technology");
        category.setDescription("Tech category");
        category = categoryRepository.save(category);
    }

    @Test
    void testCreateStory() throws Exception {
        StoryCreateRequest request = new StoryCreateRequest();
        request.setBaslik("Test Story");
        request.setIcerik("Test content with more than 100 characters to meet the minimum requirement for story creation. This content should be long enough.");
        request.setOzet("Test summary");
        request.setKategoriId(category.getId());

        MvcResult result = mockMvc.perform(post("/api/haberler")
                        .header("Authorization", writerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.baslik").value("Test Story"))
                .andReturn();

        StoryResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), StoryResponse.class);
        assertNotNull(response.getId());
    }

    @Test
    void testFindById() throws Exception {
        Story story = createTestStory("Find By Id Story", writer.getId());

        mockMvc.perform(get("/api/haberler/{id}", story.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(story.getId()))
                .andExpect(jsonPath("$.baslik").value("Find By Id Story"));
    }

    @Test
    void testFindBySlug() throws Exception {
        Story story = createTestStory("Find By Slug Story", writer.getId());

        mockMvc.perform(get("/api/haberler/slug/{slug}", story.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value(story.getSlug()));
    }

    @Test
    void testFindAll() throws Exception {
        createTestStory("Story 1", writer.getId());
        createTestStory("Story 2", writer.getId());

        mockMvc.perform(get("/api/haberler")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testFindByKullanici() throws Exception {
        createTestStory("User Story 1", writer.getId());
        createTestStory("User Story 2", writer.getId());

        mockMvc.perform(get("/api/haberler/kullanici/{kullaniciId}", writer.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testFindByKategori() throws Exception {
        Story story = createTestStory("Category Story", writer.getId());
        story.setCategory(category);
        storyRepository.save(story);

        mockMvc.perform(get("/api/haberler/kategori/{kategoriId}", category.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetPopular() throws Exception {
        Story story = createTestStory("Popular Story", writer.getId());
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setLikeCount(100L);
        storyRepository.save(story);

        mockMvc.perform(get("/api/haberler/populer")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetEditorPicks() throws Exception {
        Story story = createTestStory("Editor Pick Story", writer.getId());
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setIsEditorPick(true);
        storyRepository.save(story);

        mockMvc.perform(get("/api/haberler/editor-secimleri")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testSearch() throws Exception {
        Story story = createTestStory("Search Test Story", writer.getId());
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        storyRepository.save(story);

        mockMvc.perform(get("/api/haberler/arama")
                        .param("q", "Search")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testUpdateStory() throws Exception {
        Story story = createTestStory("Update Story", writer.getId());

        StoryUpdateRequest request = new StoryUpdateRequest();
        request.setBaslik("Updated Title");
        request.setIcerik("Updated content");

        mockMvc.perform(put("/api/haberler/{id}", story.getId())
                        .header("Authorization", writerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baslik").value("Updated Title"));
    }

    @Test
    void testDeleteStory() throws Exception {
        Story story = createTestStory("Delete Story", writer.getId());

        mockMvc.perform(delete("/api/haberler/{id}", story.getId())
                        .header("Authorization", writerToken))
                .andExpect(status().isNoContent());

        Story deletedStory = storyRepository.findById(story.getId()).orElse(null);
        assertNotNull(deletedStory);
        assertFalse(deletedStory.getIsActive());
    }

    @Test
    void testPublishStory() throws Exception {
        Story story = createTestStory("Publish Story", writer.getId());

        mockMvc.perform(post("/api/haberler/{id}/yayinla", story.getId())
                        .header("Authorization", writerToken))
                .andExpect(status().isOk());

        Story publishedStory = storyRepository.findById(story.getId()).orElse(null);
        assertNotNull(publishedStory);
        assertEquals(Story.StoryStatus.YAYIN_BEKLIYOR, publishedStory.getStatus());
    }

    @Test
    void testApproveStory() throws Exception {
        Story story = createTestStory("Approve Story", writer.getId());
        story.setStatus(Story.StoryStatus.YAYIN_BEKLIYOR);
        storyRepository.save(story);

        mockMvc.perform(post("/api/haberler/{id}/onayla", story.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());

        Story approvedStory = storyRepository.findById(story.getId()).orElse(null);
        assertNotNull(approvedStory);
        assertEquals(Story.StoryStatus.YAYINLANDI, approvedStory.getStatus());
    }

    @Test
    void testRejectStory() throws Exception {
        Story story = createTestStory("Reject Story", writer.getId());
        story.setStatus(Story.StoryStatus.YAYIN_BEKLIYOR);
        storyRepository.save(story);

        mockMvc.perform(post("/api/haberler/{id}/reddet", story.getId())
                        .header("Authorization", adminToken)
                        .param("sebep", "Uygunsuz içerik"))
                .andExpect(status().isOk());

        Story rejectedStory = storyRepository.findById(story.getId()).orElse(null);
        assertNotNull(rejectedStory);
        assertEquals(Story.StoryStatus.REDDEDILDI, rejectedStory.getStatus());
    }

    @Test
    void testToggleEditorPick() throws Exception {
        Story story = createTestStory("Editor Pick Toggle", writer.getId());
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setIsEditorPick(false);
        storyRepository.save(story);

        mockMvc.perform(post("/api/haberler/{id}/editor-secimi", story.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());

        Story updatedStory = storyRepository.findById(story.getId()).orElse(null);
        assertNotNull(updatedStory);
        assertTrue(updatedStory.getIsEditorPick());
    }

    @Test
    void testGetBekleyen() throws Exception {
        Story story = createTestStory("Pending Story", writer.getId());
        story.setStatus(Story.StoryStatus.YAYIN_BEKLIYOR);
        storyRepository.save(story);

        mockMvc.perform(get("/api/haberler/bekleyen")
                        .header("Authorization", adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testCreateStoryUnauthorized() throws Exception {
        StoryCreateRequest request = new StoryCreateRequest();
        request.setBaslik("Unauthorized Story");
        request.setIcerik("Content");

        // Token olmadan istek
        mockMvc.perform(post("/api/haberler")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403, 
                        "Expected 401 or 403 but got " + status);
                });
    }

    @Test
    void testUpdateStoryUnauthorized() throws Exception {
        Story story = createTestStory("Unauthorized Update", writer.getId());
        
        // Farklı kullanıcı token'ı ile güncelleme dene
        User otherUser = new User();
        otherUser.setEmail("other@test.com");
        otherUser.setUsername("other");
        otherUser.setPassword(passwordEncoder.encode("password123"));
        otherUser.setIsActive(true);
        Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
            Role role = new Role();
            role.setName("USER");
            role.setIsActive(true);
            return roleRepository.save(role);
        });
        otherUser.setRoles(Set.of(userRole));
        otherUser = userRepository.save(otherUser);
        Set<String> otherRoles = otherUser.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
        String otherToken = "Bearer " + jwtUtil.generateToken(otherUser.getEmail(), otherUser.getId(), otherRoles);

        StoryUpdateRequest request = new StoryUpdateRequest();
        request.setBaslik("Unauthorized Update");

        mockMvc.perform(put("/api/haberler/{id}", story.getId())
                        .header("Authorization", otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403, 
                        "Expected 401 or 403 but got " + status);
                });
    }

    private Story createTestStory(String title, Long userId) {
        Story story = new Story();
        story.setTitle(title);
        story.setContent("Content for " + title);
        story.setSummary("Summary");
        story.setSlug(title.toLowerCase().replace(" ", "-"));
        story.setUser(userRepository.findById(userId).orElse(writer));
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

