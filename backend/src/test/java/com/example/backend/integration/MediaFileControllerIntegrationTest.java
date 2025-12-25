package com.example.backend.integration;

import com.example.backend.domain.entity.MediaFile;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.MediaFileRepository;
import com.example.backend.domain.repository.RoleRepository;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.infrastructure.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MediaFileControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MediaFileRepository mediaFileRepository;

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
    private String userToken;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        
        Role userRole = createRoleIfNotExists("USER");
        
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
        
        Set<String> userRoles = user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
        userToken = "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getId(), userRoles);
    }

    @Test
    void testUploadFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Test file content".getBytes()
        );

        mockMvc.perform(multipart("/api/dosyalar/yukle")
                        .file(file)
                        .header("Authorization", userToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").exists())
                .andExpect(jsonPath("$.url").exists());
    }

    @Test
    void testUploadFileUnauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Test file content".getBytes()
        );

        mockMvc.perform(multipart("/api/dosyalar/yukle")
                        .file(file))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403, "Expected 401 or 403 but got " + status);
                });
    }

    @Test
    void testFindById() throws Exception {
        MediaFile mediaFile = createTestMediaFile();

        mockMvc.perform(get("/api/dosyalar/{id}", mediaFile.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mediaFile.getId()))
                .andExpect(jsonPath("$.fileName").value(mediaFile.getFileName()));
    }

    @Test
    void testFindByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/dosyalar/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteFile() throws Exception {
        MediaFile mediaFile = createTestMediaFile();

        mockMvc.perform(delete("/api/dosyalar/{id}", mediaFile.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isNoContent());

        // MediaFileService.deleteFile() hard delete yapıyor, soft delete değil
        MediaFile deleted = mediaFileRepository.findById(mediaFile.getId()).orElse(null);
        assertNull(deleted); // Dosya tamamen silinmiş olmalı
    }

    @Test
    void testDeleteFileUnauthorized() throws Exception {
        MediaFile mediaFile = createTestMediaFile();

        mockMvc.perform(delete("/api/dosyalar/{id}", mediaFile.getId()))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403, "Expected 401 or 403 but got " + status);
                });
    }

    @Test
    void testUploadImageFile() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{1, 2, 3, 4, 5}
        );

        mockMvc.perform(multipart("/api/dosyalar/yukle")
                        .file(imageFile)
                        .header("Authorization", userToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").exists())
                .andExpect(jsonPath("$.mimeType").value(MediaType.IMAGE_JPEG_VALUE));
    }

    private MediaFile createTestMediaFile() {
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileName("test.txt");
        mediaFile.setOriginalFileName("test.txt");
        mediaFile.setMimeType("text/plain");
        mediaFile.setFileSize(100L);
        mediaFile.setFilePath("2024/12/test.txt");
        mediaFile.setUploaderUserId(user.getId());
        mediaFile.setFileType(MediaFile.FileType.DOKUMAN);
        mediaFile.setIsActive(true);
        return mediaFileRepository.save(mediaFile);
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

