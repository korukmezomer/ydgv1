package com.example.backend.presentation.controller;

import com.example.backend.application.dto.request.UserUpdateRequest;
import com.example.backend.application.dto.response.UserResponse;
import com.example.backend.application.dto.response.PageResponse;
import com.example.backend.application.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kullanicilar")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        UserResponse response = userService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> findByEmail(@PathVariable String email) {
        UserResponse response = userService.findByEmail(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> findAll(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Repository'de zaten ORDER BY u.id DESC var, bu yüzden Sort eklemiyoruz
        // @Query içinde ORDER BY varsa, Pageable'daki Sort göz ardı edilir
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> kullaniciPage;
        
        if (q != null && !q.trim().isEmpty()) {
            kullaniciPage = userService.search(q.trim(), pageable);
        } else {
            kullaniciPage = userService.findAll(pageable);
        }
        
        PageResponse<UserResponse> response = toPageResponse(kullaniciPage);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/arama")
    public ResponseEntity<PageResponse<UserResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Repository'de zaten ORDER BY u.id DESC var, bu yüzden Sort eklemiyoruz
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> kullaniciPage = userService.search(q.trim(), pageable);
        PageResponse<UserResponse> response = toPageResponse(kullaniciPage);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> guncelle(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> sil(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/aktif")
    public ResponseEntity<Void> aktifPasifYap(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestParam Boolean aktif) {
        userService.setActive(id, aktif);
        return ResponseEntity.ok().build();
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

