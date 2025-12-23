package com.example.backend.application.service.impl;

import com.example.backend.application.dto.response.MediaFileResponse;
import com.example.backend.application.exception.BadRequestException;
import com.example.backend.application.exception.ForbiddenException;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.application.service.MediaFileService;
import com.example.backend.domain.entity.MediaFile;
import com.example.backend.domain.repository.MediaFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${server.port:8080}")
    private String serverPort;

    @Override
    public MediaFileResponse uploadFile(Long userId, MultipartFile file) {
        try {
            // Dosya adını oluştur
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + extension;
            
            // Klasör yapısını oluştur (yıl/ay)
            LocalDateTime now = LocalDateTime.now();
            String yearMonth = now.getYear() + "/" + String.format("%02d", now.getMonthValue());
            Path uploadPath = Paths.get(uploadDir, yearMonth);
            
            // Klasör yoksa oluştur
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Dosyayı kaydet
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Dosya tipini belirle
            MediaFile.FileType fileType = MediaFile.FileType.DIGER;
            String contentType = file.getContentType();
            if (contentType != null) {
                if (contentType.startsWith("image/")) {
                    fileType = MediaFile.FileType.RESIM;
                } else if (contentType.startsWith("video/")) {
                    fileType = MediaFile.FileType.VIDEO;
                } else if (contentType.contains("pdf") || contentType.contains("word") || 
                          contentType.contains("excel") || contentType.contains("powerpoint")) {
                    fileType = MediaFile.FileType.DOKUMAN;
                }
            }
            
            // Veritabanına kaydet
            MediaFile mediaFile = new MediaFile();
            mediaFile.setFileName(uniqueFileName);
            mediaFile.setOriginalFileName(originalFilename);
            mediaFile.setFilePath(yearMonth + "/" + uniqueFileName);
            mediaFile.setMimeType(contentType);
            mediaFile.setFileSize(file.getSize());
            mediaFile.setUploaderUserId(userId);
            mediaFile.setFileType(fileType);
            
            mediaFile = mediaFileRepository.save(mediaFile);
            
            return toResponse(mediaFile);
        } catch (IOException e) {
            throw new BadRequestException("Dosya yüklenirken hata oluştu: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MediaFileResponse findById(Long id) {
        MediaFile mediaFile = mediaFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dosya bulunamadı"));
        return toResponse(mediaFile);
    }

    @Override
    public void deleteFile(Long id, Long userId) {
        MediaFile mediaFile = mediaFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dosya bulunamadı"));
        
        // Sadece yükleyen kullanıcı silebilir
        if (!mediaFile.getUploaderUserId().equals(userId)) {
            throw new ForbiddenException("Bu dosyayı silme yetkiniz yok");
        }
        
        // Dosyayı diskten sil
        try {
            Path filePath = Paths.get(uploadDir, mediaFile.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Dosya silinemediyse devam et, veritabanından sil
        }
        
        // Veritabanından sil
        mediaFileRepository.delete(mediaFile);
    }

    private MediaFileResponse toResponse(MediaFile mediaFile) {
        MediaFileResponse response = new MediaFileResponse();
        response.setId(mediaFile.getId());
        response.setFileName(mediaFile.getFileName());
        response.setOriginalFileName(mediaFile.getOriginalFileName());
        response.setFilePath(mediaFile.getFilePath());
        response.setUrl("http://localhost:" + serverPort + "/api/dosyalar/" + mediaFile.getFilePath());
        response.setMimeType(mediaFile.getMimeType());
        response.setFileSize(mediaFile.getFileSize());
        response.setUploaderUserId(mediaFile.getUploaderUserId());
        response.setFileType(mediaFile.getFileType());
        response.setCreatedAt(mediaFile.getCreatedAt());
        response.setUpdatedAt(mediaFile.getUpdatedAt());
        return response;
    }
}

