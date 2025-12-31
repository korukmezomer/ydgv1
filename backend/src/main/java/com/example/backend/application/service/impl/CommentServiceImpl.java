package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.CommentCreateRequest;
import com.example.backend.application.dto.response.CommentResponse;
import com.example.backend.application.service.CommentService;
import com.example.backend.application.exception.BadRequestException;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.application.service.NotificationService;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.Comment;
import com.example.backend.domain.entity.Notification;
import com.example.backend.domain.repository.StoryRepository;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.domain.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public CommentResponse olustur(Long storyId, Long userId, CommentCreateRequest request) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Story bulunamadı"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setStory(story);
        comment.setUser(user);
        // Yorumları otomatik onayla - kullanıcılar yorumlarını hemen görebilmeli
        comment.setStatus(Comment.CommentStatus.ONAYLANDI);
        comment.setLikeCount(0L);
        comment.setIsActive(true); // Yorum aktif olarak işaretle

        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Üst yorum bulunamadı"));
            comment.setParentComment(parentComment);
        }

        comment = commentRepository.save(comment);

        // Haber yorum sayısını artır
        story.setCommentCount(story.getCommentCount() + 1);
        storyRepository.save(story);

        // Bildirim gönder
        if (request.getParentCommentId() != null) {
            // Yorum cevabı - parent comment sahibine bildirim gönder
            Comment parentComment = comment.getParentComment();
            Long parentCommentOwnerId = parentComment.getUser().getId();
            
            // Kendi yorumuna cevap verildiyse bildirim gönderme
            if (!parentCommentOwnerId.equals(userId)) {
                String commenterName = user.getUsername() != null 
                    ? user.getUsername() 
                    : (user.getFirstName() != null ? user.getFirstName() : "Bir kullanıcı");
                
                notificationService.createNotification(
                    parentCommentOwnerId,
                    "Yorumunuza Yanıt",
                    commenterName + " yorumunuza yanıt verdi: " + 
                    (comment.getContent().length() > 100 
                        ? comment.getContent().substring(0, 100) + "..." 
                        : comment.getContent()),
                    Notification.NotificationType.YORUM_YANITI,
                    story.getId(),
                    comment.getId()
                );
            }
        } else {
            // Yeni yorum - story sahibine bildirim gönder
            Long storyOwnerId = story.getUser().getId();
            
            // Kendi yazısına yorum yapıldıysa bildirim gönderme
            if (!storyOwnerId.equals(userId)) {
                String commenterName = user.getUsername() != null 
                    ? user.getUsername() 
                    : (user.getFirstName() != null ? user.getFirstName() : "Bir kullanıcı");
                
                notificationService.createNotification(
                    storyOwnerId,
                    "Yeni Yorum",
                    commenterName + " yazınıza yorum yaptı: " + 
                    (comment.getContent().length() > 100 
                        ? comment.getContent().substring(0, 100) + "..." 
                        : comment.getContent()),
                    Notification.NotificationType.YENI_YORUM,
                    story.getId(),
                    comment.getId()
                );
            }
        }

        return toResponse(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse findById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yorum bulunamadı"));
        return toResponse(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> findByStoryId(Long storyId) {
        List<Comment> comments = commentRepository.findByStoryIdAndParentCommentIsNullAndStatus(
                storyId, Comment.CommentStatus.ONAYLANDI);
        return comments.stream()
                .map(this::toResponseWithReplies)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> findByStoryId(Long storyId, Pageable pageable) {
        return commentRepository.findByStoryIdAndStatus(storyId, Comment.CommentStatus.ONAYLANDI, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> findByDurum(Comment.CommentStatus durum, Pageable pageable) {
        return commentRepository.findByStatus(durum, pageable)
                .map(this::toResponse);
    }

    @Override
    public CommentResponse guncelle(Long id, Long userId, String content) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yorum bulunamadı"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bu yorumu güncelleme yetkiniz yok");
        }

        comment.setContent(content);
        comment.setStatus(Comment.CommentStatus.BEKLIYOR); // Güncelleme sonrası tekrar onay bekler
        comment = commentRepository.save(comment);
        return toResponse(comment);
    }

    @Override
    public void sil(Long id, Long userId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yorum bulunamadı"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        // Admin veya yorum sahibi silebilir
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
        boolean isOwner = comment.getUser().getId().equals(userId);

        if (!isAdmin && !isOwner) {
            throw new BadRequestException("Bu yorumu silme yetkiniz yok");
        }

        comment.setIsActive(false);
        commentRepository.save(comment);

        // Haber yorum sayısını azalt
        Story story = comment.getStory();
        story.setCommentCount(Math.max(0, story.getCommentCount() - 1));
        storyRepository.save(story);
    }

    @Override
    public void onayla(Long id, Long adminId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yorum bulunamadı"));
        comment.setStatus(Comment.CommentStatus.ONAYLANDI);
        commentRepository.save(comment);
    }

    @Override
    public void reddet(Long id, Long adminId, String sebep) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yorum bulunamadı"));
        comment.setStatus(Comment.CommentStatus.REDDEDILDI);
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> findByAuthorId(Long authorId, Pageable pageable) {
        return commentRepository.findByAuthorId(authorId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> findByAuthorIdAndStoryId(Long authorId, Long storyId, Pageable pageable) {
        return commentRepository.findByAuthorIdAndStoryId(authorId, storyId, pageable)
                .map(this::toResponse);
    }

    private CommentResponse toResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setStatus(comment.getStatus());
        response.setLikeCount(comment.getLikeCount());
        response.setUserId(comment.getUser().getId());
        response.setUsername(comment.getUser().getUsername());
        if (comment.getStory() != null) {
            response.setStoryId(comment.getStory().getId());
            response.setStoryTitle(comment.getStory().getTitle());
            response.setStorySlug(comment.getStory().getSlug());
        }
        if (comment.getParentComment() != null) {
            response.setParentCommentId(comment.getParentComment().getId());
        }
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }

    private CommentResponse toResponseWithReplies(Comment comment) {
        CommentResponse response = toResponse(comment);
        List<Comment> replies = commentRepository.findByParentCommentId(comment.getId(), Comment.CommentStatus.ONAYLANDI);
        response.setReplies(replies.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
        return response;
    }
}

