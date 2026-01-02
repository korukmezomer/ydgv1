package com.example.backend.domain.repository;

import com.example.backend.domain.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findBySlug(String slug);
    
    Optional<Category> findByName(String name);
    
    boolean existsBySlug(String slug);
    
    boolean existsByName(String name);
    
    java.util.List<Category> findByIsActiveTrue();
    
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.id DESC")
    Page<Category> findByIsActiveTrue(Pageable pageable);
}

