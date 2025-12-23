package com.example.backend.application.service.impl;

import com.example.backend.application.dto.response.StoryResponse;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.application.service.SavedStoryService;
import com.example.backend.domain.entity.SavedStory;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.SavedStoryRepository;
import com.example.backend.domain.repository.StoryRepository;
import com.example.backend.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SavedStoryServiceImpl implements SavedStoryService {

    @Autowired
    private SavedStoryRepository savedStoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Override
    public void saveStory(Long userId, Long storyId) {
        if (savedStoryRepository.existsByUserIdAndStoryId(userId, storyId)) {
            return; // Zaten kayıtlı
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Story bulunamadı"));

        SavedStory savedStory = new SavedStory();
        savedStory.setUser(user);
        savedStory.setStory(story);
        savedStory.setIsActive(true);

        savedStoryRepository.save(savedStory);
    }

    @Override
    public void removeStory(Long userId, Long storyId) {
        SavedStory savedStory = savedStoryRepository.findActiveByUserIdAndStoryId(userId, storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Kayıtlı story bulunamadı"));

        savedStory.setIsActive(false);
        savedStoryRepository.save(savedStory);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSaved(Long userId, Long storyId) {
        return savedStoryRepository.existsByUserIdAndStoryId(userId, storyId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StoryResponse> findByUserId(Long userId, Pageable pageable) {
        return savedStoryRepository.findByUserId(userId, pageable)
                .map(savedStory -> toStoryResponse(savedStory.getStory()));
    }

    private StoryResponse toStoryResponse(Story story) {
        StoryResponse response = new StoryResponse();
        response.setId(story.getId());
        response.setBaslik(story.getTitle());
        response.setSlug(story.getSlug());
        response.setOzet(story.getSummary());
        response.setContent(story.getContent()); // İçerik eklendi
        response.setIcerik(story.getContent()); // Backward compatibility için
        response.setKapakResmiUrl(story.getCoverImageUrl());
        response.setDurum(story.getStatus());
        response.setYayinlanmaTarihi(story.getPublishedAt());
        response.setOkunmaSayisi(story.getViewCount());
        response.setBegeniSayisi(story.getLikeCount());
        response.setYorumSayisi(story.getCommentCount());
        response.setMetaDescription(story.getMetaDescription());
        response.setKullaniciId(story.getUser().getId());
        response.setKullaniciAdi(story.getUser().getUsername());
        // Yazarın rollerini ekle
        response.setKullaniciRolleri(story.getUser().getRoles().stream()
                .map(role -> role.getName())
                .collect(java.util.stream.Collectors.toSet()));
        if (story.getCategory() != null) {
            response.setKategoriId(story.getCategory().getId());
            response.setKategoriAdi(story.getCategory().getName());
        }
        response.setCreatedAt(story.getCreatedAt());
        response.setUpdatedAt(story.getUpdatedAt());
        return response;
    }
}

