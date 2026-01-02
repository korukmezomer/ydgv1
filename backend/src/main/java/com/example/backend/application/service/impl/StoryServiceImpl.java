package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.StoryCreateRequest;
import com.example.backend.application.dto.request.StoryUpdateRequest;
import com.example.backend.application.dto.response.StoryResponse;
import com.example.backend.application.exception.BadRequestException;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.application.service.NotificationService;
import com.example.backend.application.service.StoryService;
import com.example.backend.domain.entity.*;
import com.example.backend.domain.repository.*;
import com.example.backend.infrastructure.util.SlugUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class StoryServiceImpl implements StoryService {

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public StoryResponse create(Long userId, StoryCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        Story story = new Story();
        story.setTitle(request.getBaslik());
        story.setContent(request.getIcerik());
        story.setSummary(request.getOzet());
        story.setCoverImageUrl(request.getKapakResmiUrl());
        story.setMetaDescription(request.getMetaDescription());
        story.setUser(user);
        story.setStatus(Story.StoryStatus.TASLAK);
        story.setViewCount(0L);
        story.setLikeCount(0L);
        story.setCommentCount(0L);
        story.setIsEditorPick(false);
        story.setIsActive(true);

        if (request.getKategoriId() != null) {
            Category category = categoryRepository.findById(request.getKategoriId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kategori bulunamadı"));
            story.setCategory(category);
        }

        // Slug oluştur
        String slug = generateUniqueSlug(request.getBaslik());
        story.setSlug(slug);

        // Etiketleri ekle
        if (request.getEtiketler() != null && !request.getEtiketler().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : request.getEtiketler()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            newTag.setSlug(generateUniqueTagSlug(tagName));
                            return tagRepository.save(newTag);
                        });
                tags.add(tag);
            }
            story.setTags(tags);
        }

        story = storyRepository.save(story);
        return toResponse(story);
    }

    @Override
    @Transactional(readOnly = true)
    public StoryResponse findById(Long id) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Story bulunamadı"));
        return toResponse(story);
    }

    @Override
    @Transactional(readOnly = true)
    public StoryResponse findBySlug(String slug) {
        Story story = storyRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Story bulunamadı"));
        return toResponse(story);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StoryResponse> findAll(Pageable pageable) {
        return storyRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StoryResponse> findByUserId(Long userId, Pageable pageable) {
        return storyRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StoryResponse> findByCategoryId(Long categoryId, Pageable pageable) {
        return storyRepository.findByCategoryId(categoryId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StoryResponse> findByStatus(Story.StoryStatus status, Pageable pageable) {
        // Repository'den veriyi al
        Page<Story> storyPage = storyRepository.findByStatus(status, pageable);
        
        // İçeriği ID'ye göre DESC sırala (ekstra güvenlik için)
        java.util.List<Story> sortedStories = storyPage.getContent().stream()
            .sorted((s1, s2) -> Long.compare(s2.getId(), s1.getId()))
            .collect(java.util.stream.Collectors.toList());
        
        // Yeni Page oluştur
        Page<Story> sortedStoryPage = new org.springframework.data.domain.PageImpl<>(
            sortedStories, 
            pageable, 
            storyPage.getTotalElements()
        );
        
        return sortedStoryPage.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StoryResponse> findPublishedStories(Pageable pageable) {
        return storyRepository.findPublishedStories(Story.StoryStatus.YAYINLANDI, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StoryResponse> findPopularStories(Pageable pageable) {
        return storyRepository.findPopularStories(Story.StoryStatus.YAYINLANDI, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StoryResponse> findEditorPicks(Pageable pageable) {
        return storyRepository.findEditorPicks(Story.StoryStatus.YAYINLANDI, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public void toggleEditorPick(Long storyId, Long adminId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Story bulunamadı"));
        
        // Sadece yayınlanmış story'ler editor pick olabilir
        if (!story.getStatus().equals(Story.StoryStatus.YAYINLANDI)) {
            throw new BadRequestException("Sadece yayınlanmış story'ler editör seçimi olabilir");
        }
        
        story.setIsEditorPick(!story.getIsEditorPick());
        storyRepository.save(story);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StoryResponse> search(String query, Pageable pageable) {
        return storyRepository.searchStories(query, Story.StoryStatus.YAYINLANDI, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StoryResponse> findByTagId(Long tagId, Pageable pageable) {
        return storyRepository.findByTagId(tagId, Story.StoryStatus.YAYINLANDI, pageable)
                .map(this::toResponse);
    }

    @Override
    public StoryResponse update(Long id, Long userId, StoryUpdateRequest request) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Story bulunamadı"));

        if (!story.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bu story'yi güncelleme yetkiniz yok");
        }

        if (request.getBaslik() != null) {
            story.setTitle(request.getBaslik());
            // Başlık değiştiyse slug'ı güncelle
            String newSlug = generateUniqueSlug(request.getBaslik());
            story.setSlug(newSlug);
        }

        if (request.getIcerik() != null) {
            story.setContent(request.getIcerik());
        }

        if (request.getOzet() != null) {
            story.setSummary(request.getOzet());
        }

        if (request.getKapakResmiUrl() != null) {
            story.setCoverImageUrl(request.getKapakResmiUrl());
        }

        if (request.getMetaDescription() != null) {
            story.setMetaDescription(request.getMetaDescription());
        }

        if (request.getKategoriId() != null) {
            Category category = categoryRepository.findById(request.getKategoriId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kategori bulunamadı"));
            story.setCategory(category);
        }

        // Etiketleri güncelle
        if (request.getEtiketler() != null) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : request.getEtiketler()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            newTag.setSlug(generateUniqueTagSlug(tagName));
                            return tagRepository.save(newTag);
                        });
                tags.add(tag);
            }
            story.setTags(tags);
        }

        story = storyRepository.save(story);
        return toResponse(story);
    }

    @Override
    public void delete(Long id, Long userId) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Story bulunamadı"));

        if (!story.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bu story'yi silme yetkiniz yok");
        }

        story.setIsActive(false);
        storyRepository.save(story);
    }

    @Override
    public void publish(Long id, Long userId) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Story bulunamadı"));

        if (!story.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bu story'yi yayınlama yetkiniz yok");
        }

        story.setStatus(Story.StoryStatus.YAYIN_BEKLIYOR);
        storyRepository.save(story);
    }

    @Override
    public void approve(Long id, Long adminId) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Story bulunamadı"));

        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setPublishedAt(LocalDateTime.now());
        storyRepository.save(story);

        // Takip eden kullanıcılara bildirim gönder
        Long authorId = story.getUser().getId();
        List<User> followers = followRepository.findFollowersByFollowedId(authorId);
        
        String authorName = story.getUser().getUsername() != null 
            ? story.getUser().getUsername() 
            : (story.getUser().getFirstName() != null ? story.getUser().getFirstName() : "Bir yazar");
        
        for (User follower : followers) {
            notificationService.createNotification(
                follower.getId(),
                "Yeni İçerik",
                authorName + " yeni bir içerik yayınladı: " + story.getTitle(),
                Notification.NotificationType.HABER_YAYINLANDI,
                story.getId(),
                null
            );
        }
    }

    @Override
    public void reject(Long id, Long adminId, String reason) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Story bulunamadı"));

        story.setStatus(Story.StoryStatus.REDDEDILDI);
        storyRepository.save(story);
    }

    private String generateUniqueSlug(String baslik) {
        String baseSlug = SlugUtil.toSlug(baslik);
        String slug = baseSlug;
        int counter = 1;

        while (storyRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    private String generateUniqueTagSlug(String tagName) {
        String baseSlug = SlugUtil.toSlug(tagName);
        String slug = baseSlug;
        int counter = 1;

        while (tagRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    private StoryResponse toResponse(Story story) {
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
        response.setIsEditorPick(story.getIsEditorPick());
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

