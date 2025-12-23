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

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/dosyalar")
@CrossOrigin(origins = "*")
public class MediaFileController {

    @Autowired
    private MediaFileService mediaFileService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/yukle")
    public ResponseEntity<MediaFileResponse> dosyaYukle(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) {
        Long kullaniciId = getKullaniciIdFromToken(token);
        MediaFileResponse response = mediaFileService.uploadFile(kullaniciId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

