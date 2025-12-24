package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.TagCreateRequest;
import com.example.backend.application.dto.response.TagResponse;
import com.example.backend.application.exception.BadRequestException;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.application.service.TagService;
import com.example.backend.domain.entity.Tag;
import com.example.backend.domain.repository.TagRepository;
import com.example.backend.infrastructure.util.SlugUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository tagRepository;

    @Override
    public TagResponse create(TagCreateRequest request) {
        if (tagRepository.existsByName(request.getName())) {
            throw new BadRequestException("Bu etiket adı zaten kullanılıyor");
        }

        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setSlug(generateUniqueSlug(request.getName()));
        tag.setIsActive(true);

        tag = tagRepository.save(tag);
        return toResponse(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public TagResponse findById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etiket bulunamadı"));
        return toResponse(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public TagResponse findBySlug(String slug) {
        Tag tag = tagRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Etiket bulunamadı"));
        return toResponse(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> findAll() {
        return tagRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagResponse> findAll(Pageable pageable) {
        return tagRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    public TagResponse update(Long id, TagCreateRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etiket bulunamadı"));

        if (!tag.getName().equals(request.getName()) &&
            tagRepository.existsByName(request.getName())) {
            throw new BadRequestException("Bu etiket adı zaten kullanılıyor");
        }

        tag.setName(request.getName());
        if (!tag.getName().equals(request.getName())) {
            tag.setSlug(generateUniqueSlug(request.getName()));
        }

        tag = tagRepository.save(tag);
        return toResponse(tag);
    }

    @Override
    public void delete(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etiket bulunamadı"));
        tag.setIsActive(false);
        tagRepository.save(tag);
    }

    private String generateUniqueSlug(String tagName) {
        String baseSlug = SlugUtil.toSlug(tagName);
        String slug = baseSlug;
        int counter = 1;

        while (tagRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    private TagResponse toResponse(Tag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setSlug(tag.getSlug());
        response.setCreatedAt(tag.getCreatedAt());
        response.setUpdatedAt(tag.getUpdatedAt());
        return response;
    }
}

