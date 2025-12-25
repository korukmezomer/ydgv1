package com.example.backend.integration;

import com.example.backend.application.dto.request.AuthorProfileUpdateRequest;
import com.example.backend.domain.entity.AuthorProfile;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.AuthorProfileRepository;
import com.example.backend.domain.repository.RoleRepository;
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

class AuthorProfileControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AuthorProfileRepository authorProfileRepository;

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
    private User writer;
    private String writerToken;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        
        Role userRole = createRoleIfNotExists("USER");
        Role writerRole = createRoleIfNotExists("WRITER");
        
        // User oluştur
        user = new User();
        user.setEmail("user@test.com");
        user.setUsername("testuser");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setIsActive(true);
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);
        
        // Writer oluştur
        writer = new User();
        writer.setEmail("writer@test.com");
        writer.setUsername("writer");
        writer.setFirstName("Writer");
        writer.setLastName("User");
        writer.setPassword(passwordEncoder.encode("password123"));
        writer.setIsActive(true);
        writer.setRoles(Set.of(writerRole));
        writer = userRepository.save(writer);
        
        Set<String> writerRoles = writer.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
        writerToken = "Bearer " + jwtUtil.generateToken(writer.getEmail(), writer.getId(), writerRoles);
    }

    @Test
    void testFindByKullaniciId() throws Exception {
        // Profil oluştur
        AuthorProfile profile = new AuthorProfile();
        profile.setUser(writer);
        profile.setBio("Test bio");
        profile = authorProfileRepository.save(profile);

        mockMvc.perform(get("/api/yazar-profilleri/kullanici/{kullaniciId}", writer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Test bio"));
    }

    @Test
    void testFindByKullaniciIdNotFound() throws Exception {
        // Profil olmayan kullanıcı için null döner ama 200 OK
        mockMvc.perform(get("/api/yazar-profilleri/kullanici/{kullaniciId}", user.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateOrUpdate() throws Exception {
        AuthorProfileUpdateRequest request = new AuthorProfileUpdateRequest();
        request.setBio("New bio");
        request.setWebsite("https://example.com");
        request.setTwitterHandle("@testuser");

        mockMvc.perform(post("/api/yazar-profilleri/kullanici/{kullaniciId}", writer.getId())
                        .header("Authorization", writerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("New bio"))
                .andExpect(jsonPath("$.website").value("https://example.com"));
    }

    @Test
    void testCreateOrUpdateUnauthorized() throws Exception {
        AuthorProfileUpdateRequest request = new AuthorProfileUpdateRequest();
        request.setBio("New bio");

        mockMvc.perform(post("/api/yazar-profilleri/kullanici/{kullaniciId}", writer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403, "Expected 401 or 403 but got " + status);
                });
    }

    @Test
    void testUpdate() throws Exception {
        // Önce profil oluştur
        AuthorProfile profile = new AuthorProfile();
        profile.setUser(writer);
        profile.setBio("Original bio");
        profile = authorProfileRepository.save(profile);

        AuthorProfileUpdateRequest request = new AuthorProfileUpdateRequest();
        request.setBio("Updated bio");
        request.setWebsite("https://updated.com");

        mockMvc.perform(put("/api/yazar-profilleri/kullanici/{kullaniciId}", writer.getId())
                        .header("Authorization", writerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Updated bio"))
                .andExpect(jsonPath("$.website").value("https://updated.com"));
    }

    @Test
    void testUpdateUnauthorized() throws Exception {
        AuthorProfileUpdateRequest request = new AuthorProfileUpdateRequest();
        request.setBio("Updated bio");

        mockMvc.perform(put("/api/yazar-profilleri/kullanici/{kullaniciId}", writer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403, "Expected 401 or 403 but got " + status);
                });
    }

    @Test
    void testCreateOrUpdateWithAllFields() throws Exception {
        AuthorProfileUpdateRequest request = new AuthorProfileUpdateRequest();
        request.setBio("Complete bio");
        request.setAvatarUrl("https://example.com/avatar.jpg");
        request.setWebsite("https://example.com");
        request.setTwitterHandle("@testuser");
        request.setLinkedinUrl("https://linkedin.com/in/testuser");

        mockMvc.perform(post("/api/yazar-profilleri/kullanici/{kullaniciId}", writer.getId())
                        .header("Authorization", writerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Complete bio"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.jpg"))
                .andExpect(jsonPath("$.website").value("https://example.com"))
                .andExpect(jsonPath("$.twitterHandle").value("@testuser"))
                .andExpect(jsonPath("$.linkedinUrl").value("https://linkedin.com/in/testuser"));
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

