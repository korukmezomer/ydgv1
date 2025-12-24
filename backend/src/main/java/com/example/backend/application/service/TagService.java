package com.example.backend.application.service;

import com.example.backend.application.dto.request.TagCreateRequest;
import com.example.backend.application.dto.response.TagResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TagService {
    
    TagResponse create(TagCreateRequest request);
    
    TagResponse findById(Long id);
    
    TagResponse findBySlug(String slug);
    
    List<TagResponse> findAll();
    
    Page<TagResponse> findAll(Pageable pageable);
    
    TagResponse update(Long id, TagCreateRequest request);
    
    void delete(Long id);
}

