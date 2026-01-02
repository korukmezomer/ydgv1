package com.example.backend.presentation.controller;

import com.example.backend.application.dto.request.CommentCreateRequest;
import com.example.backend.application.dto.response.PageResponse;
import com.example.backend.application.dto.response.CommentResponse;
import com.example.backend.application.service.CommentService;
import com.example.backend.domain.entity.Comment.CommentStatus;
import com.example.backend.application.exception.UnauthorizedException;
import com.example.backend.infrastructure.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/yorumlar")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/haber/{haberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER', 'USER')")
    public ResponseEntity<CommentResponse> olustur(
            @RequestHeader("Authorization") String token,
            @PathVariable Long haberId,
            @Valid @RequestBody CommentCreateRequest request) {
        Long kullaniciId = getKullaniciIdFromToken(token);
        CommentResponse response = commentService.olustur(haberId, kullaniciId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> findById(@PathVariable Long id) {
        CommentResponse response = commentService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/haber/{haberId}")
    public ResponseEntity<List<CommentResponse>> findByHaberId(@PathVariable Long haberId) {
        List<CommentResponse> responses = commentService.findByStoryId(haberId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/haber/{haberId}/sayfali")
    public ResponseEntity<PageResponse<CommentResponse>> findByHaberIdSayfali(
            @PathVariable Long haberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
        Page<CommentResponse> yorumPage = commentService.findByStoryId(haberId, pageable);
        PageResponse<CommentResponse> response = toPageResponse(yorumPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/durum/{durum}")
    public ResponseEntity<PageResponse<CommentResponse>> findByDurum(
            @PathVariable CommentStatus durum,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
        Page<CommentResponse> yorumPage = commentService.findByDurum(durum, pageable);
        PageResponse<CommentResponse> response = toPageResponse(yorumPage);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> guncelle(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody String icerik) {
        Long kullaniciId = getKullaniciIdFromToken(token);
        CommentResponse response = commentService.guncelle(id, kullaniciId, icerik);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> sil(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Long kullaniciId = getKullaniciIdFromToken(token);
        commentService.sil(id, kullaniciId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/onayla")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> onayla(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Long adminId = getKullaniciIdFromToken(token);
        commentService.onayla(id, adminId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reddet")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reddet(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestParam(required = false) String sebep) {
        Long adminId = getKullaniciIdFromToken(token);
        commentService.reddet(id, adminId, sebep);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/yazar/{yazarId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER')")
    public ResponseEntity<PageResponse<CommentResponse>> findByYazarId(
            @RequestHeader("Authorization") String token,
            @PathVariable Long yazarId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
        Page<CommentResponse> yorumPage = commentService.findByAuthorId(yazarId, pageable);
        PageResponse<CommentResponse> response = toPageResponse(yorumPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/yazar/{yazarId}/haber/{haberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER')")
    public ResponseEntity<PageResponse<CommentResponse>> findByYazarIdAndHaberId(
            @RequestHeader("Authorization") String token,
            @PathVariable Long yazarId,
            @PathVariable Long haberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
        Page<CommentResponse> yorumPage = commentService.findByAuthorIdAndStoryId(yazarId, haberId, pageable);
        PageResponse<CommentResponse> response = toPageResponse(yorumPage);
        return ResponseEntity.ok(response);
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

