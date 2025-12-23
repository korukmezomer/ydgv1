package com.example.backend.domain.repository;

import com.example.backend.domain.entity.ListEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ListRepository extends JpaRepository<ListEntity, Long> {
    
    Optional<ListEntity> findBySlug(String slug);
    
    boolean existsBySlug(String slug);
    
    @Query("SELECT l FROM ListEntity l WHERE l.user.id = :userId AND l.isActive = true")
    Page<ListEntity> findByUserIdAndIsActiveTrue(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT l FROM ListEntity l WHERE l.user.id = :userId AND l.isPrivate = false AND l.isActive = true")
    Page<ListEntity> findByUserIdAndIsPrivateFalseAndIsActiveTrue(@Param("userId") Long userId, Pageable pageable);
}

