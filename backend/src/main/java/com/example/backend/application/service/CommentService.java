package com.example.backend.application.service;

import com.example.backend.application.dto.request.CommentCreateRequest;
import com.example.backend.application.dto.response.CommentResponse;
import com.example.backend.domain.entity.Comment.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {
    
    CommentResponse olustur(Long storyId, Long userId, CommentCreateRequest request);
    
    CommentResponse findById(Long id);
    
    List<CommentResponse> findByStoryId(Long storyId);
    
    Page<CommentResponse> findByStoryId(Long storyId, Pageable pageable);
    
    Page<CommentResponse> findByDurum(CommentStatus durum, Pageable pageable);
    
    CommentResponse guncelle(Long id, Long userId, String content);
    
    void sil(Long id, Long userId);
    
    void onayla(Long id, Long adminId);
    
    void reddet(Long id, Long adminId, String reason);
    
    Page<CommentResponse> findByAuthorId(Long authorId, Pageable pageable);
    
    Page<CommentResponse> findByAuthorIdAndStoryId(Long authorId, Long storyId, Pageable pageable);
}

