package com.example.backend.domain.repository;

import com.example.backend.domain.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    Optional<Tag> findBySlug(String slug);
    
    Optional<Tag> findByName(String name);
    
    boolean existsBySlug(String slug);
    
    boolean existsByName(String name);
    
    Set<Tag> findByNameIn(Set<String> names);
    
    java.util.List<Tag> findByIsActiveTrue();
    
    org.springframework.data.domain.Page<Tag> findByIsActiveTrue(org.springframework.data.domain.Pageable pageable);
}

