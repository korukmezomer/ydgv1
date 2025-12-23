package com.example.backend.domain.repository;

import com.example.backend.domain.entity.Comment;
import com.example.backend.domain.entity.Comment.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    @Query("SELECT c FROM Comment c WHERE c.story.id = :storyId AND c.status = :status AND c.isActive = true")
    Page<Comment> findByStoryIdAndStatus(@Param("storyId") Long storyId, @Param("status") CommentStatus status, Pageable pageable);
    
    @Query("SELECT c FROM Comment c WHERE c.story.id = :storyId AND c.parentComment IS NULL AND c.status = :status AND c.isActive = true")
    List<Comment> findByStoryIdAndParentCommentIsNullAndStatus(@Param("storyId") Long storyId, @Param("status") CommentStatus status);
    
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentCommentId AND c.isActive = true AND c.status = :status ORDER BY c.createdAt ASC")
    List<Comment> findByParentCommentId(@Param("parentCommentId") Long parentCommentId, @Param("status") CommentStatus status);
    
    @Query("SELECT c FROM Comment c WHERE c.status = :status AND c.isActive = true")
    Page<Comment> findByStatus(@Param("status") CommentStatus status, Pageable pageable);
    
    @Query("SELECT c FROM Comment c JOIN c.story s WHERE s.user.id = :authorId AND c.isActive = true")
    Page<Comment> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);
    
    @Query("SELECT c FROM Comment c JOIN c.story s WHERE s.user.id = :authorId AND c.story.id = :storyId AND c.isActive = true")
    Page<Comment> findByAuthorIdAndStoryId(@Param("authorId") Long authorId, @Param("storyId") Long storyId, Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.story.id = :storyId")
    Long countByStoryId(@Param("storyId") Long storyId);
}

