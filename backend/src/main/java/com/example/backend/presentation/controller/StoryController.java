package com.example.backend.presentation.controller;

import com.example.backend.application.dto.request.StoryCreateRequest;
import com.example.backend.application.dto.request.StoryUpdateRequest;
import com.example.backend.application.dto.response.PageResponse;
import com.example.backend.application.dto.response.StoryResponse;
import com.example.backend.application.service.StoryService;
import com.example.backend.domain.entity.Story.StoryStatus;
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

@RestController
@RequestMapping("/api/haberler")
@CrossOrigin(origins = "*")
public class StoryController {

    @Autowired
    private StoryService storyService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER')")
    public ResponseEntity<StoryResponse> create(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody StoryCreateRequest request) {
        Long userId = getUserIdFromToken(token);
        StoryResponse response = storyService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoryResponse> findById(@PathVariable Long id) {
        StoryResponse response = storyService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<StoryResponse> findBySlug(@PathVariable String slug) {
        StoryResponse response = storyService.findBySlug(slug);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<StoryResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StoryResponse> storyPage = storyService.findPublishedStories(pageable);
        PageResponse<StoryResponse> response = toPageResponse(storyPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/kullanici/{kullaniciId}")
    public ResponseEntity<PageResponse<StoryResponse>> findByKullanici(
            @PathVariable Long kullaniciId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StoryResponse> storyPage = storyService.findByUserId(kullaniciId, pageable);
        PageResponse<StoryResponse> response = toPageResponse(storyPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/kategori/{kategoriId}")
    public ResponseEntity<PageResponse<StoryResponse>> findByKategori(
            @PathVariable Long kategoriId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StoryResponse> storyPage = storyService.findByCategoryId(kategoriId, pageable);
        PageResponse<StoryResponse> response = toPageResponse(storyPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/populer")
    public ResponseEntity<PageResponse<StoryResponse>> getPopular(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StoryResponse> storyPage = storyService.findPopularStories(pageable);
        PageResponse<StoryResponse> response = toPageResponse(storyPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/editor-secimleri")
    public ResponseEntity<PageResponse<StoryResponse>> getEditorPicks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StoryResponse> storyPage = storyService.findEditorPicks(pageable);
        PageResponse<StoryResponse> response = toPageResponse(storyPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bekleyen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<StoryResponse>> getBekleyen(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StoryResponse> storyPage = storyService.findByStatus(StoryStatus.YAYIN_BEKLIYOR, pageable);
        PageResponse<StoryResponse> response = toPageResponse(storyPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/arama")
    public ResponseEntity<PageResponse<StoryResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StoryResponse> storyPage = storyService.search(q, pageable);
        PageResponse<StoryResponse> response = toPageResponse(storyPage);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER')")
    public ResponseEntity<StoryResponse> update(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @Valid @RequestBody StoryUpdateRequest request) {
        Long userId = getUserIdFromToken(token);
        StoryResponse response = storyService.update(id, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER')")
    public ResponseEntity<Void> delete(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Long userId = getUserIdFromToken(token);
        storyService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/yayinla")
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER')")
    public ResponseEntity<Void> yayinla(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Long userId = getUserIdFromToken(token);
        storyService.publish(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/onayla")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> onayla(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Long adminId = getUserIdFromToken(token);
        storyService.approve(id, adminId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reddet")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reddet(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestParam(required = false) String sebep) {
        Long adminId = getUserIdFromToken(token);
        storyService.reject(id, adminId, sebep);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/editor-secimi")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleEditorPick(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Long adminId = getUserIdFromToken(token);
        storyService.toggleEditorPick(id, adminId);
        return ResponseEntity.ok().build();
    }

    private Long getUserIdFromToken(String token) {
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

