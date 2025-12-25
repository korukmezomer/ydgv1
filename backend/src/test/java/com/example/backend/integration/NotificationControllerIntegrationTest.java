package com.example.backend.integration;

import com.example.backend.domain.entity.Notification;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.NotificationRepository;
import com.example.backend.domain.repository.RoleRepository;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.infrastructure.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private NotificationRepository notificationRepository;

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
        
        user = new User();
        user.setEmail("notifuser@test.com");
        user.setUsername("notifuser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setIsActive(true);
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);
        
        Set<String> userRoles = user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
        userToken = "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getId(), userRoles);
    }

    @Test
    void testFindAll() throws Exception {
        createTestNotification("Notification 1", user.getId(), false);
        createTestNotification("Notification 2", user.getId(), false);

        mockMvc.perform(get("/api/bildirimler")
                        .header("Authorization", userToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testFindUnread() throws Exception {
        createTestNotification("Unread Notification", user.getId(), false);
        createTestNotification("Read Notification", user.getId(), true);

        mockMvc.perform(get("/api/bildirimler/okunmamis")
                        .header("Authorization", userToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetUnreadCount() throws Exception {
        createTestNotification("Unread 1", user.getId(), false);
        createTestNotification("Unread 2", user.getId(), false);

        mockMvc.perform(get("/api/bildirimler/okunmamis-sayi")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2));
    }

    @Test
    void testMarkAsRead() throws Exception {
        Notification notification = createTestNotification("Mark Read", user.getId(), false);

        mockMvc.perform(patch("/api/bildirimler/{id}/okundu", notification.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        Notification updated = notificationRepository.findById(notification.getId()).orElse(null);
        assertNotNull(updated);
        assertTrue(updated.getIsRead());
    }

    @Test
    void testMarkAllAsRead() throws Exception {
        createTestNotification("Unread 1", user.getId(), false);
        createTestNotification("Unread 2", user.getId(), false);

        mockMvc.perform(patch("/api/bildirimler/tumunu-okundu")
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        Long unreadCount = notificationRepository.countUnreadByUserId(user.getId());
        assertEquals(0L, unreadCount);
    }

    @Test
    void testFindAllUnauthorized() throws Exception {
        mockMvc.perform(get("/api/bildirimler")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403, 
                        "Expected 401 or 403 but got " + status);
                });
    }

    private Notification createTestNotification(String message, Long userId, boolean isRead) {
        Notification notification = new Notification();
        notification.setUser(userRepository.findById(userId).orElse(user));
        notification.setTitle("Test Notification");
        notification.setMessage(message);
        notification.setType(Notification.NotificationType.YENI_YORUM);
        notification.setIsRead(isRead);
        notification.setIsActive(true);
        return notificationRepository.save(notification);
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

