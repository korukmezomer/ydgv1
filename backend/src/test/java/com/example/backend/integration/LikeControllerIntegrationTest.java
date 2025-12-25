package com.example.backend.integration;

import com.example.backend.domain.entity.Like;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.LikeRepository;
import com.example.backend.domain.repository.RoleRepository;
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

class LikeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private LikeRepository likeRepository;

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
        user.setEmail("liker@test.com");
        user.setUsername("liker");
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
        story.setTitle("Like Test Story");
        story.setContent("Content");
        story.setSlug("like-test-story");
        story.setUser(writer);
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setIsActive(true);
        story = storyRepository.save(story);
        
        userToken = "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRoles());
    }

    @Test
    void testLikeStory() throws Exception {
        mockMvc.perform(post("/api/begeniler/haber/{haberId}", story.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        assertTrue(likeRepository.existsByUserIdAndStoryId(user.getId(), story.getId()));
    }

    @Test
    void testUnlikeStory() throws Exception {
        // Önce beğen
        Like like = new Like();
        like.setUser(user);
        like.setStory(story);
        like.setIsActive(true);
        likeRepository.save(like);

        mockMvc.perform(delete("/api/begeniler/haber/{haberId}", story.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isNoContent());

        assertFalse(likeRepository.existsByUserIdAndStoryId(user.getId(), story.getId()));
    }

    @Test
    void testIsLiked() throws Exception {
        Like like = new Like();
        like.setUser(user);
        like.setStory(story);
        like.setIsActive(true);
        likeRepository.save(like);

        mockMvc.perform(get("/api/begeniler/haber/{haberId}/durum", story.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void testGetLikeCount() throws Exception {
        Like like1 = new Like();
        like1.setUser(user);
        like1.setStory(story);
        like1.setIsActive(true);
        likeRepository.save(like1);

        mockMvc.perform(get("/api/begeniler/haber/{haberId}/sayi", story.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    void testLikeStoryUnauthorized() throws Exception {
        mockMvc.perform(post("/api/begeniler/haber/{haberId}", story.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void testLikeAlreadyLikedStory() throws Exception {
        Like like = new Like();
        like.setUser(user);
        like.setStory(story);
        like.setIsActive(true);
        likeRepository.save(like);

        mockMvc.perform(post("/api/begeniler/haber/{haberId}", story.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isBadRequest());
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

