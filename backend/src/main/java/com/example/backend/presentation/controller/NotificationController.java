package com.example.backend.presentation.controller;

import com.example.backend.application.dto.response.NotificationResponse;
import com.example.backend.application.dto.response.PageResponse;
import com.example.backend.application.service.NotificationService;
import com.example.backend.application.exception.UnauthorizedException;
import com.example.backend.infrastructure.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bildirimler")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> findAll(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long kullaniciId = getKullaniciIdFromToken(token);
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> bildirimPage = notificationService.findByUserId(kullaniciId, pageable);
        PageResponse<NotificationResponse> response = toPageResponse(bildirimPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/okunmamis")
    public ResponseEntity<PageResponse<NotificationResponse>> findOkunmamis(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long kullaniciId = getKullaniciIdFromToken(token);
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> bildirimPage = notificationService.findUnreadByUserId(kullaniciId, pageable);
        PageResponse<NotificationResponse> response = toPageResponse(bildirimPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/okunmamis-sayi")
    public ResponseEntity<Long> okunmamisSayisi(@RequestHeader("Authorization") String token) {
        Long kullaniciId = getKullaniciIdFromToken(token);
        Long sayi = notificationService.countUnread(kullaniciId);
        return ResponseEntity.ok(sayi);
    }

    @PatchMapping("/{id}/okundu")
    public ResponseEntity<Void> okunduIsaretle(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Long kullaniciId = getKullaniciIdFromToken(token);
        notificationService.markAsRead(id, kullaniciId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/tumunu-okundu")
    public ResponseEntity<Void> tumunuOkunduIsaretle(@RequestHeader("Authorization") String token) {
        Long kullaniciId = getKullaniciIdFromToken(token);
        notificationService.markAllAsRead(kullaniciId);
        return ResponseEntity.ok().build();
    }

    private Long getKullaniciIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            return jwtUtil.extractUserId(jwtToken);
        }
        throw new UnauthorizedException("Geçersiz token");
    }

    private <T> PageResponse<T> toPageResponse(Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(page.getContent());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }
}

