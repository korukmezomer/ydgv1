package com.example.backend.application.service;

import com.example.backend.application.dto.response.MediaFileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MediaFileService {
    MediaFileResponse uploadFile(Long userId, MultipartFile file);
    MediaFileResponse findById(Long id);
    void deleteFile(Long id, Long userId);
}

