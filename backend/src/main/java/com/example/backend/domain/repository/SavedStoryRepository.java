package com.example.backend.domain.repository;

import com.example.backend.domain.entity.SavedStory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedStoryRepository extends JpaRepository<SavedStory, Long> {
    Optional<SavedStory> findByUserIdAndStoryId(Long userId, Long storyId);
    
    @Query("SELECT s FROM SavedStory s WHERE s.user.id = :userId AND s.story.id = :storyId AND s.isActive = true")
    Optional<SavedStory> findActiveByUserIdAndStoryId(@Param("userId") Long userId, @Param("storyId") Long storyId);
    
    @Query("SELECT COUNT(s) > 0 FROM SavedStory s WHERE s.user.id = :userId AND s.story.id = :storyId AND s.isActive = true")
    boolean existsByUserIdAndStoryId(@Param("userId") Long userId, @Param("storyId") Long storyId);
    
    @Query("SELECT s FROM SavedStory s WHERE s.user.id = :userId AND s.isActive = true ORDER BY s.createdAt DESC")
    Page<SavedStory> findByUserId(@Param("userId") Long userId, Pageable pageable);
}

