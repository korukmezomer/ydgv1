package com.example.backend.presentation.controller;

import com.example.backend.application.service.FollowService;
import com.example.backend.application.exception.UnauthorizedException;
import com.example.backend.infrastructure.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/takip")
@CrossOrigin(origins = "*")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/{takipEdilenId}")
    public ResponseEntity<Void> takipEt(
            @RequestHeader("Authorization") String token,
            @PathVariable Long takipEdilenId) {
        Long takipciId = getKullaniciIdFromToken(token);
        followService.follow(takipciId, takipEdilenId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{takipEdilenId}")
    public ResponseEntity<Void> takibiBirak(
            @RequestHeader("Authorization") String token,
            @PathVariable Long takipEdilenId) {
        Long takipciId = getKullaniciIdFromToken(token);
        followService.unfollow(takipciId, takipEdilenId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{takipEdilenId}/durum")
    public ResponseEntity<Boolean> takipEdiliyorMu(
            @RequestHeader("Authorization") String token,
            @PathVariable Long takipEdilenId) {
        Long takipciId = getKullaniciIdFromToken(token);
        boolean takipEdiliyor = followService.isFollowing(takipciId, takipEdilenId);
        return ResponseEntity.ok(takipEdiliyor);
    }

    @GetMapping("/{kullaniciId}/takipci-sayisi")
    public ResponseEntity<Long> takipciSayisi(@PathVariable Long kullaniciId) {
        Long sayi = followService.getFollowerCount(kullaniciId);
        return ResponseEntity.ok(sayi);
    }

    @GetMapping("/{kullaniciId}/takip-edilen-sayisi")
    public ResponseEntity<Long> takipEdilenSayisi(@PathVariable Long kullaniciId) {
        Long sayi = followService.getFollowingCount(kullaniciId);
        return ResponseEntity.ok(sayi);
    }

    @GetMapping("/{kullaniciId}/takipciler")
    public ResponseEntity<java.util.List<com.example.backend.application.dto.response.UserResponse>> getTakipciler(
            @PathVariable Long kullaniciId) {
        java.util.List<com.example.backend.application.dto.response.UserResponse> takipciler = followService.getFollowers(kullaniciId);
        return ResponseEntity.ok(takipciler);
    }

    @GetMapping("/{kullaniciId}/takip-edilenler")
    public ResponseEntity<java.util.List<com.example.backend.application.dto.response.UserResponse>> getTakipEdilenler(
            @PathVariable Long kullaniciId) {
        java.util.List<com.example.backend.application.dto.response.UserResponse> takipEdilenler = followService.getFollowing(kullaniciId);
        return ResponseEntity.ok(takipEdilenler);
    }

    private Long getKullaniciIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            return jwtUtil.extractUserId(jwtToken);
        }
        throw new UnauthorizedException("Geçersiz token");
    }
}

