package com.example.backend.domain.repository;

import com.example.backend.domain.entity.AuthorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorProfileRepository extends JpaRepository<AuthorProfile, Long> {
    
    @Query("SELECT a FROM AuthorProfile a WHERE a.user.id = :userId")
    Optional<AuthorProfile> findByUserId(@Param("userId") Long userId);
}

