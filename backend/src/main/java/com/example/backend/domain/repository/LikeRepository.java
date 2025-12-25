package com.example.backend.domain.repository;

import com.example.backend.domain.entity.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    
    @Query("SELECT l FROM Like l WHERE l.user.id = :userId AND l.story.id = :storyId AND l.isActive = true")
    Optional<Like> findByUserIdAndStoryId(@Param("userId") Long userId, @Param("storyId") Long storyId);
    
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Like l WHERE l.user.id = :userId AND l.story.id = :storyId AND l.isActive = true")
    boolean existsByUserIdAndStoryId(@Param("userId") Long userId, @Param("storyId") Long storyId);
    
    @Query("SELECT COUNT(l) FROM Like l WHERE l.story.id = :storyId")
    Long countByStoryId(@Param("storyId") Long storyId);
    
    @Query("SELECT COUNT(l) FROM Like l WHERE l.story.id = :storyId AND l.isActive = true")
    Long countActiveByStoryId(@Param("storyId") Long storyId);
}

