package com.example.backend.domain.repository;

import com.example.backend.domain.entity.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
    
    List<MediaFile> findByUploaderUserId(Long uploaderUserId);
}

