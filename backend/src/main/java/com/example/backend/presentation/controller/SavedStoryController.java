package com.example.backend.presentation.controller;

import com.example.backend.application.dto.response.PageResponse;
import com.example.backend.application.dto.response.StoryResponse;
import com.example.backend.application.service.SavedStoryService;
import com.example.backend.application.exception.UnauthorizedException;
import com.example.backend.infrastructure.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kayitli-haberler")
@CrossOrigin(origins = "*")
public class SavedStoryController {

    @Autowired
    private SavedStoryService savedStoryService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER', 'USER')")
    public ResponseEntity<PageResponse<StoryResponse>> getAll(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserIdFromToken(token);
        // Repository'de zaten ORDER BY createdAt DESC var, bu yüzden Sort eklemiyoruz
        Pageable pageable = PageRequest.of(page, size);
        Page<StoryResponse> storyPage = savedStoryService.findByUserId(userId, pageable);
        PageResponse<StoryResponse> response = toPageResponse(storyPage);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/haber/{haberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER', 'USER')")
    public ResponseEntity<Void> kaydet(
            @RequestHeader("Authorization") String token,
            @PathVariable Long haberId) {
        Long userId = getUserIdFromToken(token);
        savedStoryService.saveStory(userId, haberId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/haber/{haberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER', 'USER')")
    public ResponseEntity<Void> kaldir(
            @RequestHeader("Authorization") String token,
            @PathVariable Long haberId) {
        Long userId = getUserIdFromToken(token);
        savedStoryService.removeStory(userId, haberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/haber/{haberId}/durum")
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITER', 'USER')")
    public ResponseEntity<Boolean> kayitliMi(
            @RequestHeader("Authorization") String token,
            @PathVariable Long haberId) {
        Long userId = getUserIdFromToken(token);
        boolean kayitli = savedStoryService.isSaved(userId, haberId);
        return ResponseEntity.ok(kayitli);
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

