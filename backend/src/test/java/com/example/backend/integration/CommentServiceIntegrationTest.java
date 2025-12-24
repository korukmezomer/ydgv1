package com.example.backend.integration;

import com.example.backend.application.dto.request.CommentCreateRequest;
import com.example.backend.application.dto.response.CommentResponse;
import com.example.backend.application.service.CommentService;
import com.example.backend.domain.entity.*;
import com.example.backend.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CommentServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Story testStory;

    @BeforeEach
    void setUp() {
        // Test kullanıcısı oluştur
        Role userRole = createRoleIfNotExists("USER");
        
        testUser = new User();
        testUser.setEmail("commenter@test.com");
        testUser.setUsername("commenter");
        testUser.setFirstName("Test");
        testUser.setLastName("Commenter");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setIsActive(true);
        testUser.setRoles(Set.of(userRole));
        testUser = userRepository.save(testUser);

        // Test story oluştur
        User writer = createTestWriter();
        testStory = createTestStory("Test Story for Comments", writer.getId());
    }

    @Test
    void testCreateComment() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("This is a test comment");

        CommentResponse response = commentService.olustur(testStory.getId(), testUser.getId(), request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("This is a test comment", response.getContent());
        assertEquals(Comment.CommentStatus.BEKLIYOR, response.getStatus());
        assertEquals(testStory.getId(), response.getStoryId());

        // Veritabanında kontrol et
        Comment savedComment = commentRepository.findById(response.getId()).orElse(null);
        assertNotNull(savedComment);
        assertEquals(testUser.getId(), savedComment.getUser().getId());
        assertEquals(testStory.getId(), savedComment.getStory().getId());
    }

    @Test
    void testCreateReplyComment() {
        // Önce ana yorum oluştur
        Comment parentComment = new Comment();
        parentComment.setContent("Parent comment");
        parentComment.setUser(testUser);
        parentComment.setStory(testStory);
        parentComment.setStatus(Comment.CommentStatus.ONAYLANDI);
        parentComment = commentRepository.save(parentComment);

        // Yanıt yorumu oluştur
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("This is a reply");
        request.setParentCommentId(parentComment.getId());

        CommentResponse response = commentService.olustur(testStory.getId(), testUser.getId(), request);

        assertNotNull(response);
        assertNotNull(response.getParentCommentId());
        assertEquals(parentComment.getId(), response.getParentCommentId());
    }

    @Test
    void testApproveComment() {
        Comment comment = new Comment();
        comment.setContent("Pending comment");
        comment.setUser(testUser);
        comment.setStory(testStory);
        comment.setStatus(Comment.CommentStatus.BEKLIYOR);
        comment = commentRepository.save(comment);

        Long adminId = createTestAdmin().getId();
        commentService.onayla(comment.getId(), adminId);

        Comment approvedComment = commentRepository.findById(comment.getId()).orElse(null);
        assertNotNull(approvedComment);
        assertEquals(Comment.CommentStatus.ONAYLANDI, approvedComment.getStatus());
    }

    @Test
    void testRejectComment() {
        Comment comment = new Comment();
        comment.setContent("Comment to reject");
        comment.setUser(testUser);
        comment.setStory(testStory);
        comment.setStatus(Comment.CommentStatus.BEKLIYOR);
        comment = commentRepository.save(comment);

        Long adminId = createTestAdmin().getId();
        commentService.reddet(comment.getId(), adminId, "Inappropriate content");

        Comment rejectedComment = commentRepository.findById(comment.getId()).orElse(null);
        assertNotNull(rejectedComment);
        assertEquals(Comment.CommentStatus.REDDEDILDI, rejectedComment.getStatus());
    }

    @Test
    void testDeleteComment() {
        Comment comment = new Comment();
        comment.setContent("Comment to delete");
        comment.setUser(testUser);
        comment.setStory(testStory);
        comment.setStatus(Comment.CommentStatus.ONAYLANDI);
        comment = commentRepository.save(comment);

        Long commentId = comment.getId();
        commentService.sil(commentId, testUser.getId());

        Comment deletedComment = commentRepository.findById(commentId).orElse(null);
        assertNull(deletedComment);
    }

    @Test
    void testFindByStoryId() {
        // Birkaç yorum oluştur
        createTestComment("Comment 1", testStory.getId());
        createTestComment("Comment 2", testStory.getId());

        Pageable pageable = PageRequest.of(0, 10);
        Page<CommentResponse> comments = commentService.findByStoryId(testStory.getId(), pageable);

        assertTrue(comments.getTotalElements() >= 2);
    }

    private Comment createTestComment(String content, Long storyId) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(testUser);
        comment.setStory(storyRepository.findById(storyId).orElse(testStory));
        comment.setStatus(Comment.CommentStatus.ONAYLANDI);
        return commentRepository.save(comment);
    }

    private Story createTestStory(String title, Long userId) {
        Story story = new Story();
        story.setTitle(title);
        story.setContent("Content for " + title);
        story.setSummary("Summary");
        story.setSlug(title.toLowerCase().replace(" ", "-"));
        story.setUser(userRepository.findById(userId).orElse(testUser));
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setViewCount(0L);
        story.setLikeCount(0L);
        story.setCommentCount(0L);
        story.setIsActive(true);
        return storyRepository.save(story);
    }

    private User createTestWriter() {
        Role writerRole = createRoleIfNotExists("WRITER");
        User writer = new User();
        writer.setEmail("writer@test.com");
        writer.setUsername("writer");
        writer.setFirstName("Writer");
        writer.setPassword(passwordEncoder.encode("password123"));
        writer.setIsActive(true);
        writer.setRoles(Set.of(writerRole));
        return userRepository.save(writer);
    }

    private User createTestAdmin() {
        Role adminRole = createRoleIfNotExists("ADMIN");
        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setUsername("admin");
        admin.setFirstName("Admin");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setIsActive(true);
        admin.setRoles(Set.of(adminRole));
        return userRepository.save(admin);
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

