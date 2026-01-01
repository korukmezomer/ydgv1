package com.example.backend.presentation.controller;

import com.example.backend.application.dto.response.MediaFileResponse;
import com.example.backend.application.service.MediaFileService;
import com.example.backend.application.exception.UnauthorizedException;
import com.example.backend.infrastructure.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/dosyalar")
@CrossOrigin(origins = "*")
public class MediaFileController {

    private static final Logger logger = LoggerFactory.getLogger(MediaFileController.class);

    @Autowired
    private MediaFileService mediaFileService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/yukle")
    public ResponseEntity<MediaFileResponse> dosyaYukle(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        
        logger.info("📤 Dosya yükleme isteği alındı");
        logger.info("Token: {}", token != null ? (token.length() > 20 ? token.substring(0, 20) + "..." : token) : "NULL");
        logger.info("File: {}", file != null ? (file.isEmpty() ? "EMPTY" : "Size: " + file.getSize() + ", Name: " + file.getOriginalFilename()) : "NULL");
        
        // Token kontrolü
        if (token == null || token.trim().isEmpty()) {
            logger.error("❌ Token eksik");
            throw new com.example.backend.application.exception.BadRequestException("Authorization token eksik");
        }
        
        // Dosya null veya boş kontrolü
        if (file == null) {
            logger.error("❌ Dosya null");
            throw new com.example.backend.application.exception.BadRequestException("Dosya null");
        }
        
        if (file.isEmpty()) {
            logger.error("❌ Dosya boş");
            throw new com.example.backend.application.exception.BadRequestException("Dosya boş");
        }
        
        logger.info("✅ Dosya validasyonu başarılı: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
        
        try {
            Long kullaniciId = getKullaniciIdFromToken(token);
            logger.info("✅ Kullanıcı ID: {}", kullaniciId);
            
            MediaFileResponse response = mediaFileService.uploadFile(kullaniciId, file);
            logger.info("✅ Dosya başarıyla yüklendi: {}", response.getUrl());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (com.example.backend.application.exception.UnauthorizedException e) {
            logger.error("❌ Token geçersiz: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("❌ Dosya yükleme hatası: {}", e.getMessage(), e);
            throw new com.example.backend.application.exception.BadRequestException("Dosya yüklenirken hata: " + e.getMessage());
        }
    }

    @GetMapping("/{year}/{month}/{filename:.+}")
    public ResponseEntity<Resource> dosyaGetir(
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir, year, month, filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = "application/octet-stream";
                try {
                    contentType = resource.getURL().openConnection().getContentType();
                } catch (Exception e) {
                    // Default content type
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaFileResponse> dosyaBilgisi(@PathVariable Long id) {
        MediaFileResponse response = mediaFileService.findById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> dosyaSil(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Long kullaniciId = getKullaniciIdFromToken(token);
        mediaFileService.deleteFile(id, kullaniciId);
        return ResponseEntity.noContent().build();
    }

    private Long getKullaniciIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            return jwtUtil.extractUserId(jwtToken);
        }
        throw new UnauthorizedException("Geçersiz token");
    }
}

