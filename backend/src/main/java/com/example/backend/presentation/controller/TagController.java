package com.example.backend.presentation.controller;

import com.example.backend.application.dto.request.TagCreateRequest;
import com.example.backend.application.dto.response.PageResponse;
import com.example.backend.application.dto.response.TagResponse;
import com.example.backend.application.service.TagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etiketler")
@CrossOrigin(origins = "*")
public class TagController {

    @Autowired
    private TagService tagService;

    @PostMapping
    public ResponseEntity<TagResponse> olustur(@Valid @RequestBody TagCreateRequest request) {
        TagResponse response = tagService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> findById(@PathVariable Long id) {
        TagResponse response = tagService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<TagResponse> findBySlug(@PathVariable String slug) {
        TagResponse response = tagService.findBySlug(slug);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TagResponse>> findAll() {
        List<TagResponse> responses = tagService.findAll();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/sayfali")
    public ResponseEntity<PageResponse<TagResponse>> findAllSayfali(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TagResponse> tagPage = tagService.findAll(pageable);
        PageResponse<TagResponse> response = toPageResponse(tagPage);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> guncelle(
            @PathVariable Long id,
            @Valid @RequestBody TagCreateRequest request) {
        TagResponse response = tagService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> sil(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
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

