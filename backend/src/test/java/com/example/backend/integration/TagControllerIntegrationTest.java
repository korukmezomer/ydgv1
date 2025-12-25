package com.example.backend.integration;

import com.example.backend.application.dto.request.TagCreateRequest;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.Tag;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.RoleRepository;
import com.example.backend.domain.repository.TagRepository;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.infrastructure.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TagControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User user;
    private String userToken;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        
        Role userRole = createRoleIfNotExists("USER");
        
        user = new User();
        user.setEmail("taguser@test.com");
        user.setUsername("taguser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setIsActive(true);
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);
        
        Set<String> userRoles = user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
        userToken = "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getId(), userRoles);
    }

    @Test
    void testCreateTag() throws Exception {
        TagCreateRequest request = new TagCreateRequest();
        request.setName("Test Tag");

        mockMvc.perform(post("/api/etiketler")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Tag"));
    }

    @Test
    void testFindById() throws Exception {
        Tag tag = createTestTag("Find By Id Tag");

        mockMvc.perform(get("/api/etiketler/{id}", tag.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tag.getId()));
    }

    @Test
    void testFindBySlug() throws Exception {
        Tag tag = createTestTag("Find By Slug Tag");

        mockMvc.perform(get("/api/etiketler/slug/{slug}", tag.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value(tag.getSlug()));
    }

    @Test
    void testFindAll() throws Exception {
        createTestTag("Tag 1");
        createTestTag("Tag 2");

        mockMvc.perform(get("/api/etiketler"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testFindAllSayfali() throws Exception {
        createTestTag("Tag 1");
        createTestTag("Tag 2");

        mockMvc.perform(get("/api/etiketler/sayfali")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testUpdateTag() throws Exception {
        Tag tag = createTestTag("Update Tag");

        TagCreateRequest request = new TagCreateRequest();
        request.setName("Updated Tag");

        mockMvc.perform(put("/api/etiketler/{id}", tag.getId())
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Tag"));
    }

    @Test
    void testDeleteTag() throws Exception {
        Tag tag = createTestTag("Delete Tag");

        mockMvc.perform(delete("/api/etiketler/{id}", tag.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isNoContent());

        Tag deleted = tagRepository.findById(tag.getId()).orElse(null);
        assertNotNull(deleted);
        assertFalse(deleted.getIsActive());
    }

    @Test
    void testCreateTagDuplicateName() throws Exception {
        createTestTag("Duplicate Tag");

        TagCreateRequest request = new TagCreateRequest();
        request.setName("Duplicate Tag");

        mockMvc.perform(post("/api/etiketler")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFindByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/etiketler/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    private Tag createTestTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setSlug(name.toLowerCase().replace(" ", "-"));
        tag.setIsActive(true);
        return tagRepository.save(tag);
    }

    private Role createRoleIfNotExists(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(roleName);
                    role.setIsActive(true);
                    return roleRepository.save(role);
                });
    }
}

