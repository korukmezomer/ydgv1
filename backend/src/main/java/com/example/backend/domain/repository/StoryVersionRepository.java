package com.example.backend.domain.repository;

import com.example.backend.domain.entity.StoryVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryVersionRepository extends JpaRepository<StoryVersion, Long> {
    List<StoryVersion> findByStoryIdOrderByVersionNumberDesc(Long storyId);
}

