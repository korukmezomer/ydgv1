package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.CommentCreateRequest;
import com.example.backend.application.dto.response.CommentResponse;
import com.example.backend.application.service.NotificationService;
import com.example.backend.domain.entity.Comment;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.entity.Notification;
import com.example.backend.domain.repository.CommentRepository;
import com.example.backend.domain.repository.StoryRepository;
import com.example.backend.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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
}


