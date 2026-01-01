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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(MediaFileServiceImpl.class);

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${server.port:8080}")
    private String serverPort;

    @Override
    public MediaFileResponse uploadFile(Long userId, MultipartFile file) {
        logger.info("📤 MediaFileService.uploadFile çağrıldı - UserId: {}, File: {}", 
                   userId, file != null ? file.getOriginalFilename() : "NULL");
        
        // Dosya null veya boş kontrolü
        if (file == null || file.isEmpty()) {
            logger.error("❌ Dosya null veya boş");
            throw new BadRequestException("Dosya boş veya bulunamadı");
        }
        
        logger.info("📄 Dosya bilgileri: Name={}, Size={}, ContentType={}", 
                   file.getOriginalFilename(), file.getSize(), file.getContentType());
        
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
            
            // Klasör yoksa oluştur - hata durumunda detaylı log
            if (!Files.exists(uploadPath)) {
                try {
                    Files.createDirectories(uploadPath);
                    logger.info("✅ Upload klasörü oluşturuldu: {}", uploadPath.toAbsolutePath());
                } catch (IOException e) {
                    logger.error("❌ Upload klasörü oluşturulamadı: {}", uploadPath.toAbsolutePath(), e);
                    // Alternatif olarak temp dizin kullan
                    Path tempUploadPath = Paths.get(System.getProperty("java.io.tmpdir"), "uploads", yearMonth);
                    try {
                        Files.createDirectories(tempUploadPath);
                        logger.info("✅ Temp upload klasörü kullanılıyor: {}", tempUploadPath.toAbsolutePath());
                        uploadPath = tempUploadPath;
                    } catch (IOException e2) {
                        logger.error("❌ Temp upload klasörü de oluşturulamadı: {}", tempUploadPath.toAbsolutePath(), e2);
                        throw new BadRequestException("Upload klasörü oluşturulamadı: " + e.getMessage());
                    }
                }
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
            
            logger.info("✅ Dosya başarıyla kaydedildi: ID={}, Path={}", mediaFile.getId(), mediaFile.getFilePath());
            
            MediaFileResponse response = toResponse(mediaFile);
            logger.info("✅ Response URL: {}", response.getUrl());
            
            return response;
        } catch (IOException e) {
            logger.error("❌ IO hatası: {}", e.getMessage(), e);
            throw new BadRequestException("Dosya yüklenirken hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Beklenmeyen hata: {}", e.getMessage(), e);
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

