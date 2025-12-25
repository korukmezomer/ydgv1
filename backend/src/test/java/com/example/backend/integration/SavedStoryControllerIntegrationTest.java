package com.example.backend.integration;

import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.SavedStory;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.RoleRepository;
import com.example.backend.domain.repository.SavedStoryRepository;
import com.example.backend.domain.repository.StoryRepository;
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

class SavedStoryControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private SavedStoryRepository savedStoryRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;
    private User user;
    private User writer;
    private Story story;
    private String userToken;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        Role userRole = createRoleIfNotExists("USER");
        Role writerRole = createRoleIfNotExists("WRITER");
        
        user = new User();
        user.setEmail("saver@test.com");
        user.setUsername("saver");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setIsActive(true);
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);
        
        writer = new User();
        writer.setEmail("writer@test.com");
        writer.setUsername("writer");
        writer.setPassword(passwordEncoder.encode("password123"));
        writer.setIsActive(true);
        writer.setRoles(Set.of(writerRole));
        writer = userRepository.save(writer);
        
        story = new Story();
        story.setTitle("Save Test Story");
        story.setContent("Content");
        story.setSlug("save-test-story");
        story.setUser(writer);
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setIsActive(true);
        story = storyRepository.save(story);
        
        Set<String> userRoles = user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
        userToken = "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getId(), userRoles);
    }

    @Test
    void testSaveStory() throws Exception {
        mockMvc.perform(post("/api/kayitli-haberler/haber/{haberId}", story.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        assertTrue(savedStoryRepository.existsByUserIdAndStoryId(user.getId(), story.getId()));
    }

    @Test
    void testRemoveStory() throws Exception {
        SavedStory savedStory = new SavedStory();
        savedStory.setUser(user);
        savedStory.setStory(story);
        savedStory.setIsActive(true);
        savedStoryRepository.save(savedStory);

        mockMvc.perform(delete("/api/kayitli-haberler/haber/{haberId}", story.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isNoContent());

        SavedStory removed = savedStoryRepository.findByUserIdAndStoryId(user.getId(), story.getId()).orElse(null);
        assertNotNull(removed);
        assertFalse(removed.getIsActive());
    }

    @Test
    void testIsSaved() throws Exception {
        SavedStory savedStory = new SavedStory();
        savedStory.setUser(user);
        savedStory.setStory(story);
        savedStory.setIsActive(true);
        savedStoryRepository.save(savedStory);

        mockMvc.perform(get("/api/kayitli-haberler/haber/{haberId}/durum", story.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void testGetAll() throws Exception {
        SavedStory savedStory = new SavedStory();
        savedStory.setUser(user);
        savedStory.setStory(story);
        savedStory.setIsActive(true);
        savedStoryRepository.save(savedStory);

        mockMvc.perform(get("/api/kayitli-haberler")
                        .header("Authorization", userToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testSaveStoryUnauthorized() throws Exception {
        mockMvc.perform(post("/api/kayitli-haberler/haber/{haberId}", story.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void testSaveAlreadySavedStory() throws Exception {
        SavedStory savedStory = new SavedStory();
        savedStory.setUser(user);
        savedStory.setStory(story);
        savedStory.setIsActive(true);
        savedStoryRepository.save(savedStory);

        // Tekrar kaydetmeyi dene (sessizce başarısız olmalı veya hata vermeli)
        mockMvc.perform(post("/api/kayitli-haberler/haber/{haberId}", story.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk()); // Service'de kontrol var, duplicate kayıt yapılmaz
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

