package com.example.backend.domain.repository;

import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.Story.StoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    Optional<Story> findBySlug(String slug);

    Page<Story> findByStatus(StoryStatus status, Pageable pageable);

    Page<Story> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT s FROM Story s WHERE s.user.id = :userId AND s.isActive = true ORDER BY s.createdAt DESC")
    Page<Story> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT s FROM Story s WHERE s.status = :status AND s.isActive = true ORDER BY s.publishedAt DESC")
    Page<Story> findPublishedStories(@Param("status") StoryStatus status, Pageable pageable);

    @Query("SELECT s FROM Story s WHERE s.status = :status AND s.isActive = true ORDER BY s.viewCount DESC")
    Page<Story> findPopularStories(@Param("status") StoryStatus status, Pageable pageable);

    @Query("SELECT s FROM Story s JOIN s.tags t WHERE t.id = :tagId AND s.status = :status AND s.isActive = true")
    Page<Story> findByTagId(@Param("tagId") Long tagId, @Param("status") StoryStatus status, Pageable pageable);

    @Query("SELECT s FROM Story s WHERE " +
           "(LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(s.title, 'ö', 'o'), 'Ö', 'O'), 'ş', 's'), 'Ş', 'S'), 'ı', 'i'), 'İ', 'I'), 'ü', 'u'), 'Ü', 'U')) LIKE LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CONCAT('%', :search, '%'), 'ö', 'o'), 'Ö', 'O'), 'ş', 's'), 'Ş', 'S'), 'ı', 'i'), 'İ', 'I'), 'ü', 'u'), 'Ü', 'U')) OR " +
           "LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(s.content, 'ö', 'o'), 'Ö', 'O'), 'ş', 's'), 'Ş', 'S'), 'ı', 'i'), 'İ', 'I'), 'ü', 'u'), 'Ü', 'U')) LIKE LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CONCAT('%', :search, '%'), 'ö', 'o'), 'Ö', 'O'), 'ş', 's'), 'Ş', 'S'), 'ı', 'i'), 'İ', 'I'), 'ü', 'u'), 'Ü', 'U')) OR " +
           "LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(s.summary, 'ö', 'o'), 'Ö', 'O'), 'ş', 's'), 'Ş', 'S'), 'ı', 'i'), 'İ', 'I'), 'ü', 'u'), 'Ü', 'U')) LIKE LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CONCAT('%', :search, '%'), 'ö', 'o'), 'Ö', 'O'), 'ş', 's'), 'Ş', 'S'), 'ı', 'i'), 'İ', 'I'), 'ü', 'u'), 'Ü', 'U')))" +
           "AND s.status = :status AND s.isActive = true")
    Page<Story> searchStories(@Param("search") String search, @Param("status") StoryStatus status, Pageable pageable);

    @Query("SELECT s FROM Story s WHERE s.isEditorPick = true AND s.status = :status AND s.isActive = true ORDER BY s.publishedAt DESC")
    Page<Story> findEditorPicks(@Param("status") StoryStatus status, Pageable pageable);

    boolean existsBySlug(String slug);
}


