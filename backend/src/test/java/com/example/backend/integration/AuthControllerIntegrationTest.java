package com.example.backend.integration;

import com.example.backend.application.dto.request.UserLoginRequest;
import com.example.backend.application.dto.request.UserRegistrationRequest;
import com.example.backend.application.dto.response.JwtResponse;
import com.example.backend.application.dto.response.UserResponse;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.RoleRepository;
import com.example.backend.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // ObjectMapper'ı manuel olarak oluştur (Spring Boot 4.0.0'da otomatik bean olmayabilir)
        this.objectMapper = new ObjectMapper();
        // Java 8 time module'ünü ekle (LocalDateTime, LocalDate vb. için)
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // MockMvc'yi oluştur
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        
        // Test için gerekli rolleri oluştur
        createRoleIfNotExists("USER");
        createRoleIfNotExists("WRITER");
        createRoleIfNotExists("ADMIN");
    }

    @Test
    void testUserRegistration() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setUsername("testuser");
        request.setRoleName("USER");

        MvcResult result = mockMvc.perform(post("/api/auth/kayit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        UserResponse response = objectMapper.readValue(responseBody, UserResponse.class);

        assertNotNull(response.getId());
        assertEquals("test@example.com", response.getEmail());

        // Veritabanında kullanıcının oluşturulduğunu doğrula
        User savedUser = userRepository.findByEmail("test@example.com").orElse(null);
        assertNotNull(savedUser);
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
    }

    @Test
    void testUserLogin() throws Exception {
        // Önce bir kullanıcı oluştur
        User user = new User();
        user.setEmail("login@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setFirstName("Login");
        user.setLastName("User");
        user.setUsername("loginuser");
        user.setIsActive(true);
        
        Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
            Role role = new Role();
            role.setName("USER");
            role.setIsActive(true);
            return roleRepository.save(role);
        });
        user.setRoles(Set.of(userRole));
        userRepository.save(user);

        // Login isteği gönder
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("login@example.com");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/giris")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("login@example.com"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JwtResponse response = objectMapper.readValue(responseBody, JwtResponse.class);

        assertNotNull(response.getToken());
        assertFalse(response.getToken().isEmpty());
    }

    @Test
    void testUserLoginWithWrongPassword() throws Exception {
        // Önce bir kullanıcı oluştur
        User user = new User();
        user.setEmail("wrongpass@example.com");
        user.setPassword(passwordEncoder.encode("correctpassword"));
        user.setFirstName("Wrong");
        user.setUsername("wronguser");
        user.setIsActive(true);
        
        Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
            Role role = new Role();
            role.setName("USER");
            role.setIsActive(true);
            return roleRepository.save(role);
        });
        user.setRoles(Set.of(userRole));
        userRepository.save(user);

        // Yanlış şifre ile login dene
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("wrongpass@example.com");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/giris")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()); // 401 Unauthorized (doğru HTTP status code)
    }

    @Test
    void testUserRegistrationMissingEmail_shouldReturnBadRequest() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail(""); // invalid
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setUsername("nouseremail");
        request.setRoleName("USER");

        mockMvc.perform(post("/api/auth/kayit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private void createRoleIfNotExists(String roleName) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role();
            role.setName(roleName);
            role.setIsActive(true);
            roleRepository.save(role);
        }
    }
}

