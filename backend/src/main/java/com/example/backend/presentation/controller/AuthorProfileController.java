package com.example.backend.presentation.controller;

import com.example.backend.application.dto.request.AuthorProfileUpdateRequest;
import com.example.backend.application.dto.response.AuthorProfileResponse;
import com.example.backend.application.service.AuthorProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/yazar-profilleri")
@CrossOrigin(origins = "*")
public class AuthorProfileController {

    @Autowired
    private AuthorProfileService authorProfileService;

    @GetMapping("/kullanici/{kullaniciId}")
    public ResponseEntity<AuthorProfileResponse> findByKullaniciId(@PathVariable Long kullaniciId) {
        AuthorProfileResponse response = authorProfileService.findByUserId(kullaniciId);
        // Profil yoksa 200 OK ile null döndür (frontend'de handle edilecek)
        // Bu şekilde 404 hatası console'da görünmez
        return ResponseEntity.ok(response);
    }

    @PostMapping("/kullanici/{kullaniciId}")
    public ResponseEntity<AuthorProfileResponse> olusturVeyaGuncelle(
            @RequestHeader("Authorization") String token,
            @PathVariable Long kullaniciId,
            @Valid @RequestBody AuthorProfileUpdateRequest request) {
        AuthorProfileResponse response = authorProfileService.createOrUpdate(kullaniciId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/kullanici/{kullaniciId}")
    public ResponseEntity<AuthorProfileResponse> guncelle(
            @RequestHeader("Authorization") String token,
            @PathVariable Long kullaniciId,
            @Valid @RequestBody AuthorProfileUpdateRequest request) {
        AuthorProfileResponse response = authorProfileService.update(kullaniciId, request);
        return ResponseEntity.ok(response);
    }
}

