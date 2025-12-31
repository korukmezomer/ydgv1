package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.CommentCreateRequest;
import com.example.backend.application.dto.response.CommentResponse;
import com.example.backend.application.exception.BadRequestException;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.application.service.NotificationService;
import com.example.backend.domain.entity.Comment;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.entity.Notification;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.repository.CommentRepository;
import com.example.backend.domain.repository.StoryRepository;
import com.example.backend.domain.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Story story;
    private User user;

    @BeforeEach
    void setUp() {
        story = new Story();
        story.setId(10L);
        story.setCommentCount(0L);

        User storyOwner = new User();
        storyOwner.setId(1L);
        story.setUser(storyOwner);

        user = new User();
        user.setId(2L);
        user.setUsername("yorumcu");
    }

    @Test
    void olustur_shouldCreateTopLevelCommentAndIncreaseStoryCommentCount() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("Merhaba dünya");
        request.setParentCommentId(null);

        when(storyRepository.findById(10L)).thenReturn(Optional.of(story));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        Comment savedComment = new Comment();
        savedComment.setId(100L);
        savedComment.setContent(request.getContent());
        savedComment.setStory(story);
        savedComment.setUser(user);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentResponse response = commentService.olustur(10L, 2L, request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Merhaba dünya", response.getContent());
        assertEquals(1L, story.getCommentCount());
        verify(storyRepository).save(story);

        // Yeni yorum olduğu için YENI_YORUM bildirimi beklenir
        verify(notificationService, times(1)).createNotification(
                eq(1L),
                eq("Yeni Yorum"),
                contains("yorumcu"),
                eq(Notification.NotificationType.YENI_YORUM),
                eq(10L),
                eq(100L)
        );
    }

    @Test
    void olustur_shouldCreateReplyAndSendReplyNotification() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("Cevap metni");
        request.setParentCommentId(50L);

        when(storyRepository.findById(10L)).thenReturn(Optional.of(story));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        Comment parent = new Comment();
        parent.setId(50L);
        User parentOwner = new User();
        parentOwner.setId(3L);
        parent.setUser(parentOwner);
        when(commentRepository.findById(50L)).thenReturn(Optional.of(parent));

        Comment savedReply = new Comment();
        savedReply.setId(200L);
        savedReply.setContent(request.getContent());
        savedReply.setStory(story);
        savedReply.setUser(user);
        savedReply.setParentComment(parent);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedReply);

        CommentResponse response = commentService.olustur(10L, 2L, request);

        assertNotNull(response);
        assertEquals(200L, response.getId());
        assertEquals(1L, story.getCommentCount());

        // YORUM_YANITI bildirimi beklenir
        verify(notificationService, times(1)).createNotification(
                eq(3L),
                eq("Yorumunuza Yanıt"),
                contains("yorumcu"),
                eq(Notification.NotificationType.YORUM_YANITI),
                eq(10L),
                eq(200L)
        );
    }

    @Test
    void sil_shouldMarkCommentInactiveAndDecreaseStoryCommentCount() {
        Comment comment = new Comment();
        comment.setId(300L);
        comment.setUser(user);
        comment.setStory(story);

        story.setCommentCount(5L);

        when(commentRepository.findById(300L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        commentService.sil(300L, 2L);

        assertFalse(comment.getIsActive()); // should be marked inactive
        assertEquals(4L, story.getCommentCount());
        verify(commentRepository).save(comment);
        verify(storyRepository).save(story);
    }

    @Test
    void onayla_shouldChangeStatusToOnaylandi() {
        Comment comment = new Comment();
        comment.setId(400L);
        comment.setStatus(Comment.CommentStatus.BEKLIYOR);

        when(commentRepository.findById(400L)).thenReturn(Optional.of(comment));

        commentService.onayla(400L, 99L);

        assertEquals(Comment.CommentStatus.ONAYLANDI, comment.getStatus());
        verify(commentRepository).save(comment);
    }

    @Test
    void reddet_shouldChangeStatusToReddedildi() {
        Comment comment = new Comment();
        comment.setId(500L);
        comment.setStatus(Comment.CommentStatus.BEKLIYOR);

        when(commentRepository.findById(500L)).thenReturn(Optional.of(comment));

        commentService.reddet(500L, 99L, "spam");

        assertEquals(Comment.CommentStatus.REDDEDILDI, comment.getStatus());
        verify(commentRepository).save(comment);
    }

    @Test
    void findById_shouldReturnCommentResponse() {
        Long commentId = 1L;
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setContent("Test comment");
        comment.setUser(user);
        comment.setStory(story);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        CommentResponse response = commentService.findById(commentId);

        assertNotNull(response);
        assertEquals(commentId, response.getId());
        assertEquals("Test comment", response.getContent());
    }

    @Test
    void findById_shouldThrowExceptionWhenNotFound() {
        Long commentId = 999L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.findById(commentId));
    }

    @Test
    void findByStoryId_shouldReturnListOfComments() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContent("Test comment");
        comment.setUser(user);
        comment.setStory(story);
        comment.setStatus(Comment.CommentStatus.ONAYLANDI);

        when(commentRepository.findByStoryIdAndParentCommentIsNullAndStatus(
                10L, Comment.CommentStatus.ONAYLANDI)).thenReturn(List.of(comment));
        when(commentRepository.findByParentCommentId(1L, Comment.CommentStatus.ONAYLANDI))
                .thenReturn(List.of());

        List<CommentResponse> responses = commentService.findByStoryId(10L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(commentRepository, times(1)).findByStoryIdAndParentCommentIsNullAndStatus(
                10L, Comment.CommentStatus.ONAYLANDI);
    }

    @Test
    void findByStoryId_shouldReturnPageOfComments() {
        Pageable pageable = PageRequest.of(0, 10);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContent("Test comment");
        comment.setUser(user);
        comment.setStory(story);
        comment.setStatus(Comment.CommentStatus.ONAYLANDI);

        Page<Comment> commentPage = new PageImpl<>(List.of(comment));
        when(commentRepository.findByStoryIdAndStatus(10L, Comment.CommentStatus.ONAYLANDI, pageable))
                .thenReturn(commentPage);

        Page<CommentResponse> response = commentService.findByStoryId(10L, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(commentRepository, times(1)).findByStoryIdAndStatus(
                10L, Comment.CommentStatus.ONAYLANDI, pageable);
    }

    @Test
    void findByDurum_shouldReturnPageOfComments() {
        Comment.CommentStatus durum = Comment.CommentStatus.BEKLIYOR;
        Pageable pageable = PageRequest.of(0, 10);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContent("Test comment");
        comment.setStatus(durum);

        Page<Comment> commentPage = new PageImpl<>(List.of(comment));
        when(commentRepository.findByStatus(durum, pageable)).thenReturn(commentPage);

        Page<CommentResponse> response = commentService.findByDurum(durum, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(commentRepository, times(1)).findByStatus(durum, pageable);
    }

    @Test
    void findByAuthorId_shouldReturnPageOfComments() {
        Long authorId = 2L;
        Pageable pageable = PageRequest.of(0, 10);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContent("Test comment");
        comment.setUser(user);

        Page<Comment> commentPage = new PageImpl<>(List.of(comment));
        when(commentRepository.findByAuthorId(authorId, pageable)).thenReturn(commentPage);

        Page<CommentResponse> response = commentService.findByAuthorId(authorId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(commentRepository, times(1)).findByAuthorId(authorId, pageable);
    }

    @Test
    void findByAuthorIdAndStoryId_shouldReturnPageOfComments() {
        Long authorId = 2L;
        Long storyId = 10L;
        Pageable pageable = PageRequest.of(0, 10);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContent("Test comment");
        comment.setUser(user);
        comment.setStory(story);

        Page<Comment> commentPage = new PageImpl<>(List.of(comment));
        when(commentRepository.findByAuthorIdAndStoryId(authorId, storyId, pageable))
                .thenReturn(commentPage);

        Page<CommentResponse> response = commentService.findByAuthorIdAndStoryId(authorId, storyId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(commentRepository, times(1)).findByAuthorIdAndStoryId(authorId, storyId, pageable);
    }

    @Test
    void guncelle_shouldUpdateCommentContent() {
        Long commentId = 1L;
        Long userId = 2L;
        String newContent = "Updated content";

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setContent("Old content");
        comment.setUser(user);
        comment.setStatus(Comment.CommentStatus.ONAYLANDI);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        Comment saved = new Comment();
        saved.setId(commentId);
        saved.setContent(newContent);
        saved.setStatus(Comment.CommentStatus.BEKLIYOR);
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        CommentResponse response = commentService.guncelle(commentId, userId, newContent);

        assertNotNull(response);
        assertEquals(Comment.CommentStatus.BEKLIYOR, comment.getStatus());
        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    void guncelle_shouldThrowExceptionWhenUserNotOwner() {
        Long commentId = 1L;
        Long otherUserId = 3L;
        String newContent = "Updated content";

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(user); // user.getId() = 2L

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(BadRequestException.class, () -> commentService.guncelle(commentId, otherUserId, newContent));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void guncelle_shouldThrowExceptionWhenCommentNotFound() {
        Long commentId = 999L;
        Long userId = 2L;
        String newContent = "Updated content";

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.guncelle(commentId, userId, newContent));
    }

    @Test
    void sil_shouldAllowAdminToDelete() {
        Long commentId = 1L;
        Long adminId = 99L;

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(user);
        comment.setStory(story);
        comment.setIsActive(true);

        User admin = new User();
        admin.setId(adminId);
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        admin.setRoles(Set.of(adminRole));

        story.setCommentCount(5L);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        commentService.sil(commentId, adminId);

        assertFalse(comment.getIsActive());
        assertEquals(4L, story.getCommentCount());
        verify(commentRepository, times(1)).save(comment);
        verify(storyRepository, times(1)).save(story);
    }

    @Test
    void sil_shouldThrowExceptionWhenUserNotOwnerOrAdmin() {
        Long commentId = 1L;
        Long otherUserId = 3L;

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(user); // user.getId() = 2L

        User otherUser = new User();
        otherUser.setId(otherUserId);
        Role userRole = new Role();
        userRole.setName("USER");
        otherUser.setRoles(Set.of(userRole));

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));

        assertThrows(BadRequestException.class, () -> commentService.sil(commentId, otherUserId));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void sil_shouldThrowExceptionWhenUserNotFound() {
        Long commentId = 1L;
        Long userId = 999L;

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(user);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.sil(commentId, userId));
    }

    @Test
    void sil_shouldThrowExceptionWhenCommentNotFound() {
        Long commentId = 999L;
        Long userId = 2L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.sil(commentId, userId));
    }

    @Test
    void olustur_shouldThrowExceptionWhenStoryNotFound() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("Test comment");

        when(storyRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.olustur(10L, 2L, request));
    }

    @Test
    void olustur_shouldThrowExceptionWhenUserNotFound() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("Test comment");

        when(storyRepository.findById(10L)).thenReturn(Optional.of(story));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.olustur(10L, 2L, request));
    }

    @Test
    void olustur_shouldThrowExceptionWhenParentCommentNotFound() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("Test comment");
        request.setParentCommentId(999L);

        when(storyRepository.findById(10L)).thenReturn(Optional.of(story));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.olustur(10L, 2L, request));
    }

    @Test
    void olustur_shouldNotSendNotificationWhenCommentingOnOwnStory() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("Test comment");
        request.setParentCommentId(null);

        User storyOwner = new User();
        storyOwner.setId(2L); // Same as user.getId()
        story.setUser(storyOwner);

        when(storyRepository.findById(10L)).thenReturn(Optional.of(story));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        Comment savedComment = new Comment();
        savedComment.setId(100L);
        savedComment.setContent(request.getContent());
        savedComment.setStory(story);
        savedComment.setUser(user);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        commentService.olustur(10L, 2L, request);

        verify(notificationService, never()).createNotification(anyLong(), anyString(), anyString(), any(), anyLong(), anyLong());
    }

    @Test
    void olustur_shouldNotSendNotificationWhenReplyingToOwnComment() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("Test reply");
        request.setParentCommentId(50L);

        Comment parent = new Comment();
        parent.setId(50L);
        parent.setUser(user); // Same user
        parent.setStory(story);

        when(storyRepository.findById(10L)).thenReturn(Optional.of(story));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(commentRepository.findById(50L)).thenReturn(Optional.of(parent));

        Comment savedReply = new Comment();
        savedReply.setId(200L);
        savedReply.setContent(request.getContent());
        savedReply.setStory(story);
        savedReply.setUser(user);
        savedReply.setParentComment(parent);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedReply);

        commentService.olustur(10L, 2L, request);

        verify(notificationService, never()).createNotification(anyLong(), anyString(), anyString(), any(), anyLong(), anyLong());
    }

    @Test
    void olustur_shouldHandleNullUsername() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("Test comment");
        request.setParentCommentId(null);

        user.setUsername(null);
        user.setFirstName("John");

        when(storyRepository.findById(10L)).thenReturn(Optional.of(story));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        Comment savedComment = new Comment();
        savedComment.setId(100L);
        savedComment.setContent(request.getContent());
        savedComment.setStory(story);
        savedComment.setUser(user);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        commentService.olustur(10L, 2L, request);

        verify(notificationService, times(1)).createNotification(
                eq(1L),
                eq("Yeni Yorum"),
                contains("John"),
                eq(Notification.NotificationType.YENI_YORUM),
                eq(10L),
                eq(100L)
        );
    }

    @Test
    void olustur_shouldHandleNullUsernameAndFirstName() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("Test comment");
        request.setParentCommentId(null);

        user.setUsername(null);
        user.setFirstName(null);

        when(storyRepository.findById(10L)).thenReturn(Optional.of(story));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        Comment savedComment = new Comment();
        savedComment.setId(100L);
        savedComment.setContent(request.getContent());
        savedComment.setStory(story);
        savedComment.setUser(user);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        commentService.olustur(10L, 2L, request);

        verify(notificationService, times(1)).createNotification(
                eq(1L),
                eq("Yeni Yorum"),
                contains("Bir kullanıcı"),
                eq(Notification.NotificationType.YENI_YORUM),
                eq(10L),
                eq(100L)
        );
    }

    @Test
    void olustur_shouldTruncateLongContent() {
        CommentCreateRequest request = new CommentCreateRequest();
        String longContent = "a".repeat(150);
        request.setContent(longContent);
        request.setParentCommentId(null);

        when(storyRepository.findById(10L)).thenReturn(Optional.of(story));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        Comment savedComment = new Comment();
        savedComment.setId(100L);
        savedComment.setContent(longContent);
        savedComment.setStory(story);
        savedComment.setUser(user);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        commentService.olustur(10L, 2L, request);

        verify(notificationService, times(1)).createNotification(
                eq(1L),
                eq("Yeni Yorum"),
                argThat(message -> message.contains("...") && message.length() < longContent.length() + 50),
                eq(Notification.NotificationType.YENI_YORUM),
                eq(10L),
                eq(100L)
        );
    }
}


