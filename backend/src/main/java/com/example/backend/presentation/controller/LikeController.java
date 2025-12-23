package com.example.backend.presentation.controller;

import com.example.backend.application.service.LikeService;
import com.example.backend.application.exception.UnauthorizedException;
import com.example.backend.infrastructure.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/begeniler")
@CrossOrigin(origins = "*")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/haber/{haberId}")
    public ResponseEntity<Void> begen(
            @RequestHeader("Authorization") String token,
            @PathVariable Long haberId) {
        Long kullaniciId = getKullaniciIdFromToken(token);
        likeService.like(haberId, kullaniciId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/haber/{haberId}")
    public ResponseEntity<Void> begeniyiKaldir(
            @RequestHeader("Authorization") String token,
            @PathVariable Long haberId) {
        Long kullaniciId = getKullaniciIdFromToken(token);
        likeService.unlike(haberId, kullaniciId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/haber/{haberId}/durum")
    public ResponseEntity<Boolean> begenildiMi(
            @RequestHeader("Authorization") String token,
            @PathVariable Long haberId) {
        Long kullaniciId = getKullaniciIdFromToken(token);
        boolean begenildi = likeService.isLiked(haberId, kullaniciId);
        return ResponseEntity.ok(begenildi);
    }

    @GetMapping("/haber/{haberId}/sayi")
    public ResponseEntity<Long> begeniSayisi(@PathVariable Long haberId) {
        Long sayi = likeService.getLikeCount(haberId);
        return ResponseEntity.ok(sayi);
    }

    private Long getKullaniciIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            return jwtUtil.extractUserId(jwtToken);
        }
        throw new UnauthorizedException("Geçersiz token");
    }
}

