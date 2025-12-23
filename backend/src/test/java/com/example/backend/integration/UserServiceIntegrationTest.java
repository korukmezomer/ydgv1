package com.example.backend.integration;

import com.example.backend.application.dto.request.UserRegistrationRequest;
import com.example.backend.application.dto.response.UserResponse;
import com.example.backend.application.service.UserService;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.RoleRepository;
import com.example.backend.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        createRoleIfNotExists("USER");
        createRoleIfNotExists("WRITER");
        createRoleIfNotExists("ADMIN");
    }

    @Test
    void testRegisterUser() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("newuser@test.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");
        request.setUsername("newuser");
        request.setRoleName("USER");

        UserResponse response = userService.register(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("newuser@test.com", response.getEmail());
        assertEquals("newuser", response.getUsername());

        // Veritabanında kontrol et
        User savedUser = userRepository.findByEmail("newuser@test.com").orElse(null);
        assertNotNull(savedUser);
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
        assertTrue(savedUser.getRoles().stream().anyMatch(r -> r.getName().equals("USER")));
    }

    @Test
    void testFindById() {
        User user = createTestUser("findbyid@test.com", "findbyid");
        Long userId = user.getId();

        UserResponse response = userService.findById(userId);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("findbyid@test.com", response.getEmail());
    }

    @Test
    void testFindByEmail() {
        User user = createTestUser("findbyemail@test.com", "findbyemail");

        UserResponse response = userService.findByEmail("findbyemail@test.com");

        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals("findbyemail@test.com", response.getEmail());
    }

    @Test
    void testToggleUserActivity() {
        User user = createTestUser("toggle@test.com", "toggle");
        assertTrue(user.getIsActive());

        userService.setActive(user.getId(), false);

        User updatedUser = userRepository.findById(user.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertFalse(updatedUser.getIsActive());
    }

    @Test
    void testDeleteUser() {
        User user = createTestUser("delete@test.com", "delete");
        Long userId = user.getId();

        userService.delete(userId);

        User deletedUser = userRepository.findById(userId).orElse(null);
        assertNull(deletedUser);
    }

    private User createTestUser(String email, String username) {
        Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
            Role role = new Role();
            role.setName("USER");
            role.setIsActive(true);
            return roleRepository.save(role);
        });

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setFirstName(username);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setIsActive(true);
        user.setRoles(Set.of(userRole));
        return userRepository.save(user);
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

