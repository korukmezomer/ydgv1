package com.example.backend.integration;

import com.example.backend.application.dto.request.CommentCreateRequest;
import com.example.backend.domain.entity.Comment;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.CommentRepository;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CommentRepository commentRepository;

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
    private ObjectMapper objectMapper;
    private User user;
    private User admin;
    private Story story;
    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        Role userRole = createRoleIfNotExists("USER");
        Role adminRole = createRoleIfNotExists("ADMIN");
        Role writerRole = createRoleIfNotExists("WRITER");
        
        // User oluştur
        user = new User();
        user.setEmail("commenter@test.com");
        user.setUsername("commenter");
        user.setFirstName("Commenter");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setIsActive(true);
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);
        
        // Admin oluştur
        admin = new User();
        admin.setEmail("admin@test.com");
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setIsActive(true);
        admin.setRoles(Set.of(adminRole));
        admin = userRepository.save(admin);
        
        // Writer oluştur
        User writer = new User();
        writer.setEmail("writer@test.com");
        writer.setUsername("writer");
        writer.setPassword(passwordEncoder.encode("password123"));
        writer.setIsActive(true);
        writer.setRoles(Set.of(writerRole));
        writer = userRepository.save(writer);
        
        // Story oluştur
        story = new Story();
        story.setTitle("Test Story");
        story.setContent("Content");
        story.setSummary("Summary");
        story.setSlug("test-story");
        story.setUser(writer);
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setIsActive(true);
        story = storyRepository.save(story);
        
        Set<String> userRoles = user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
        Set<String> adminRoles = admin.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
        userToken = "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getId(), userRoles);
        adminToken = "Bearer " + jwtUtil.generateToken(admin.getEmail(), admin.getId(), adminRoles);
    }

    @Test
    void testCreateComment() throws Exception {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("Test comment");

        mockMvc.perform(post("/api/yorumlar/haber/{haberId}", story.getId())
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Test comment"));
    }

    @Test
    void testFindById() throws Exception {
        Comment comment = createTestComment("Find By Id Comment", story.getId(), user.getId());

        mockMvc.perform(get("/api/yorumlar/{id}", comment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comment.getId()));
    }

    @Test
    void testFindByHaberId() throws Exception {
        createTestComment("Comment 1", story.getId(), user.getId());
        createTestComment("Comment 2", story.getId(), user.getId());

        mockMvc.perform(get("/api/yorumlar/haber/{haberId}", story.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testFindByHaberIdSayfali() throws Exception {
        createTestComment("Comment 1", story.getId(), user.getId());
        createTestComment("Comment 2", story.getId(), user.getId());

        mockMvc.perform(get("/api/yorumlar/haber/{haberId}/sayfali", story.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testFindByDurum() throws Exception {
        Comment comment = createTestComment("Pending Comment", story.getId(), user.getId());
        comment.setStatus(Comment.CommentStatus.BEKLIYOR);
        commentRepository.save(comment);

        mockMvc.perform(get("/api/yorumlar/durum/BEKLIYOR")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testUpdateComment() throws Exception {
        Comment comment = createTestComment("Update Comment", story.getId(), user.getId());

        mockMvc.perform(put("/api/yorumlar/{id}", comment.getId())
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("Updated content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated content"));
    }

    @Test
    void testDeleteComment() throws Exception {
        Comment comment = createTestComment("Delete Comment", story.getId(), user.getId());

        mockMvc.perform(delete("/api/yorumlar/{id}", comment.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isNoContent());

        Comment deletedComment = commentRepository.findById(comment.getId()).orElse(null);
        assertNotNull(deletedComment);
        assertFalse(deletedComment.getIsActive());
    }

    @Test
    void testApproveComment() throws Exception {
        Comment comment = createTestComment("Approve Comment", story.getId(), user.getId());
        comment.setStatus(Comment.CommentStatus.BEKLIYOR);
        commentRepository.save(comment);

        mockMvc.perform(post("/api/yorumlar/{id}/onayla", comment.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());

        Comment approvedComment = commentRepository.findById(comment.getId()).orElse(null);
        assertNotNull(approvedComment);
        assertEquals(Comment.CommentStatus.ONAYLANDI, approvedComment.getStatus());
    }

    @Test
    void testRejectComment() throws Exception {
        Comment comment = createTestComment("Reject Comment", story.getId(), user.getId());
        comment.setStatus(Comment.CommentStatus.BEKLIYOR);
        commentRepository.save(comment);

        mockMvc.perform(post("/api/yorumlar/{id}/reddet", comment.getId())
                        .header("Authorization", adminToken)
                        .param("sebep", "Spam"))
                .andExpect(status().isOk());

        Comment rejectedComment = commentRepository.findById(comment.getId()).orElse(null);
        assertNotNull(rejectedComment);
        assertEquals(Comment.CommentStatus.REDDEDILDI, rejectedComment.getStatus());
    }

    @Test
    void testFindByYazarId() throws Exception {
        createTestComment("Author Comment 1", story.getId(), user.getId());
        createTestComment("Author Comment 2", story.getId(), user.getId());

        mockMvc.perform(get("/api/yorumlar/yazar/{yazarId}", user.getId())
                        .header("Authorization", userToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testFindByYazarIdAndHaberId() throws Exception {
        createTestComment("Author Story Comment", story.getId(), user.getId());

        mockMvc.perform(get("/api/yorumlar/yazar/{yazarId}/haber/{haberId}", user.getId(), story.getId())
                        .header("Authorization", userToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testCreateCommentUnauthorized() throws Exception {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("Unauthorized comment");

        mockMvc.perform(post("/api/yorumlar/haber/{haberId}", story.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403, 
                        "Expected 401 or 403 but got " + status);
                });
    }

    private Comment createTestComment(String content, Long storyId, Long userId) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setStory(storyRepository.findById(storyId).orElse(story));
        comment.setUser(userRepository.findById(userId).orElse(user));
        comment.setStatus(Comment.CommentStatus.BEKLIYOR);
        comment.setIsActive(true);
        return commentRepository.save(comment);
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

