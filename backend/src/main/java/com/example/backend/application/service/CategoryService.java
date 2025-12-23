package com.example.backend.application.service;

import com.example.backend.application.dto.request.CategoryCreateRequest;
import com.example.backend.application.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    
    CategoryResponse create(CategoryCreateRequest request);
    
    CategoryResponse findById(Long id);
    
    CategoryResponse findBySlug(String slug);
    
    List<CategoryResponse> findAll();
    
    Page<CategoryResponse> findAll(Pageable pageable);
    
    CategoryResponse update(Long id, CategoryCreateRequest request);
    
    void delete(Long id);
}

