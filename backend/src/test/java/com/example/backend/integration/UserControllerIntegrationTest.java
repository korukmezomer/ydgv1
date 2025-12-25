package com.example.backend.integration;

import com.example.backend.application.dto.request.UserUpdateRequest;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.User;
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

class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

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
    private User admin;
    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        
        Role userRole = createRoleIfNotExists("USER");
        Role adminRole = createRoleIfNotExists("ADMIN");
        
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
        
        // Admin oluştur
        admin = new User();
        admin.setEmail("admin@test.com");
        admin.setUsername("admin");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setIsActive(true);
        admin.setRoles(Set.of(adminRole));
        admin = userRepository.save(admin);
        
        Set<String> userRoles = user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
        Set<String> adminRoles = admin.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
        userToken = "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getId(), userRoles);
        adminToken = "Bearer " + jwtUtil.generateToken(admin.getEmail(), admin.getId(), adminRoles);
    }

    @Test
    void testFindById() throws Exception {
        mockMvc.perform(get("/api/kullanicilar/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.username").value(user.getUsername()));
    }

    @Test
    void testFindByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/kullanicilar/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindByEmail() throws Exception {
        mockMvc.perform(get("/api/kullanicilar/email/{email}", user.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    void testFindByEmailNotFound() throws Exception {
        mockMvc.perform(get("/api/kullanicilar/email/{email}", "nonexistent@test.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindAll() throws Exception {
        mockMvc.perform(get("/api/kullanicilar")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    void testFindAllWithPagination() throws Exception {
        mockMvc.perform(get("/api/kullanicilar")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(1));
    }

    @Test
    void testFindAllWithSearch() throws Exception {
        mockMvc.perform(get("/api/kullanicilar")
                        .param("q", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testSearchEndpoint() throws Exception {
        mockMvc.perform(get("/api/kullanicilar/arama")
                        .param("q", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testUpdate() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail(user.getEmail());
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setUsername("updateduser");

        mockMvc.perform(put("/api/kullanicilar/{id}", user.getId())
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"));
    }

    @Test
    void testUpdateUnauthorized() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail(user.getEmail());
        request.setFirstName("Updated");

        mockMvc.perform(put("/api/kullanicilar/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403, "Expected 401 or 403 but got " + status);
                });
    }

    @Test
    void testDelete() throws Exception {
        User userToDelete = new User();
        userToDelete.setEmail("delete@test.com");
        userToDelete.setUsername("deleteuser");
        userToDelete.setPassword(passwordEncoder.encode("password123"));
        userToDelete.setIsActive(true);
        userToDelete.setRoles(Set.of(createRoleIfNotExists("USER")));
        userToDelete = userRepository.save(userToDelete);

        Set<String> deleteUserRoles = userToDelete.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
        String deleteUserToken = "Bearer " + jwtUtil.generateToken(userToDelete.getEmail(), userToDelete.getId(), deleteUserRoles);

        mockMvc.perform(delete("/api/kullanicilar/{id}", userToDelete.getId())
                        .header("Authorization", deleteUserToken))
                .andExpect(status().isNoContent());

        User deleted = userRepository.findById(userToDelete.getId()).orElse(null);
        assertNotNull(deleted);
        assertFalse(deleted.getIsActive());
    }

    @Test
    void testDeleteUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/kullanicilar/{id}", user.getId()))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403, "Expected 401 or 403 but got " + status);
                });
    }

    @Test
    void testSetActive() throws Exception {
        mockMvc.perform(patch("/api/kullanicilar/{id}/aktif", user.getId())
                        .header("Authorization", adminToken)
                        .param("aktif", "false"))
                .andExpect(status().isOk());

        User updated = userRepository.findById(user.getId()).orElse(null);
        assertNotNull(updated);
        assertFalse(updated.getIsActive());
    }

    @Test
    void testSetActiveUnauthorized() throws Exception {
        mockMvc.perform(patch("/api/kullanicilar/{id}/aktif", user.getId())
                        .param("aktif", "false"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403, "Expected 401 or 403 but got " + status);
                });
    }

    @Test
    void testFindAllDefaultPagination() throws Exception {
        mockMvc.perform(get("/api/kullanicilar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(10)); // Default size
    }

    @Test
    void testUpdateWithInvalidEmail() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("invalid-email"); // Invalid email format
        request.setFirstName("Test");

        mockMvc.perform(put("/api/kullanicilar/{id}", user.getId())
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateWithEmptyFirstName() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail(user.getEmail());
        request.setFirstName(""); // Empty first name

        mockMvc.perform(put("/api/kullanicilar/{id}", user.getId())
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSearchWithSpecialCharacters() throws Exception {
        mockMvc.perform(get("/api/kullanicilar")
                        .param("q", "test@#$%")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testFindAllWithNegativePage() throws Exception {
        mockMvc.perform(get("/api/kullanicilar")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest()); // Spring validation rejects negative pages
    }

    @Test
    void testFindAllWithZeroSize() throws Exception {
        mockMvc.perform(get("/api/kullanicilar")
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest()); // Spring validation rejects zero size
    }

    @Test
    void testFindAllWithLargeSize() throws Exception {
        mockMvc.perform(get("/api/kullanicilar")
                        .param("page", "0")
                        .param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
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

