package com.example.backend.presentation.controller;

import com.example.backend.application.dto.request.ListCreateRequest;
import com.example.backend.application.dto.response.ListResponse;
import com.example.backend.application.service.ListService;
import com.example.backend.infrastructure.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listeler")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ListController {

    @Autowired
    private ListService listService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER', 'USER')")
    public ResponseEntity<ListResponse> olustur(
            @Valid @RequestBody ListCreateRequest request,
            Authentication authentication) {
        Long kullaniciId = jwtTokenProvider.getUserIdFromAuthentication(authentication);
        ListResponse response = listService.olustur(kullaniciId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListResponse> findById(@PathVariable Long id) {
        ListResponse response = listService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ListResponse> findBySlug(@PathVariable String slug) {
        ListResponse response = listService.findBySlug(slug);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ListResponse>> findByKullaniciId(
            @RequestParam(required = false) Long kullaniciId,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        Long userId = kullaniciId != null ? kullaniciId 
                : jwtTokenProvider.getUserIdFromAuthentication(authentication);
        Page<ListResponse> response = listService.findByKullaniciId(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER', 'USER')")
    public ResponseEntity<ListResponse> guncelle(
            @PathVariable Long id,
            @Valid @RequestBody ListCreateRequest request,
            Authentication authentication) {
        Long kullaniciId = jwtTokenProvider.getUserIdFromAuthentication(authentication);
        ListResponse response = listService.guncelle(id, kullaniciId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER', 'USER')")
    public ResponseEntity<Void> sil(
            @PathVariable Long id,
            Authentication authentication) {
        Long kullaniciId = jwtTokenProvider.getUserIdFromAuthentication(authentication);
        listService.sil(id, kullaniciId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{listeId}/haber/{haberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER', 'USER')")
    public ResponseEntity<Void> haberEkle(
            @PathVariable Long listeId,
            @PathVariable Long haberId,
            Authentication authentication) {
        Long kullaniciId = jwtTokenProvider.getUserIdFromAuthentication(authentication);
        listService.haberEkle(listeId, haberId, kullaniciId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{listeId}/haber/{haberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER', 'USER')")
    public ResponseEntity<Void> haberCikar(
            @PathVariable Long listeId,
            @PathVariable Long haberId,
            Authentication authentication) {
        Long kullaniciId = jwtTokenProvider.getUserIdFromAuthentication(authentication);
        listService.haberCikar(listeId, haberId, kullaniciId);
        return ResponseEntity.ok().build();
    }
}

