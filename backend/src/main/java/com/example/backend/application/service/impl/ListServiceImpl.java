package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.ListCreateRequest;
import com.example.backend.application.dto.response.ListResponse;
import com.example.backend.application.exception.BadRequestException;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.application.dto.response.StoryResponse;
import com.example.backend.application.service.ListService;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.entity.ListEntity;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.repository.StoryRepository;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.domain.repository.ListRepository;
import com.example.backend.infrastructure.util.SlugUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional
public class ListServiceImpl implements ListService {

    @Autowired
    private ListRepository listRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Override
    public ListResponse olustur(Long kullaniciId, ListCreateRequest request) {
        User user = userRepository.findById(kullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        ListEntity list = new ListEntity();
        list.setName(request.getName());
        list.setSlug(generateUniqueSlug(request.getName()));
        list.setDescription(request.getDescription());
        list.setIsPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false);
        list.setUser(user);

        list = listRepository.save(list);
        return toResponse(list);
    }

    @Override
    @Transactional(readOnly = true)
    public ListResponse findById(Long id) {
        ListEntity list = listRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Liste bulunamadı"));
        // Stories'leri initialize et (lazy loading için)
        if (list.getStories() != null) {
            list.getStories().size(); // Initialize lazy collection
        }
        return toResponse(list);
    }

    @Override
    @Transactional(readOnly = true)
    public ListResponse findBySlug(String slug) {
        ListEntity list = listRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Liste bulunamadı"));
        // Stories'leri initialize et (lazy loading için)
        if (list.getStories() != null) {
            list.getStories().size(); // Initialize lazy collection
        }
        return toResponse(list);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListResponse> findByKullaniciId(Long kullaniciId, Pageable pageable) {
        Page<ListEntity> lists = listRepository.findByUserIdAndIsActiveTrue(kullaniciId, pageable);
        // Stories'leri initialize et (lazy loading için)
        lists.getContent().forEach(list -> {
            if (list.getStories() != null) {
                list.getStories().size(); // Initialize lazy collection
            }
        });
        return lists.map(this::toResponse);
    }

    @Override
    public ListResponse guncelle(Long id, Long kullaniciId, ListCreateRequest request) {
        ListEntity list = listRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Liste bulunamadı"));

        if (!list.getUser().getId().equals(kullaniciId)) {
            throw new BadRequestException("Bu listeyi düzenleme yetkiniz yok");
        }

        list.setName(request.getName());
        if (!list.getName().equals(request.getName())) {
            list.setSlug(generateUniqueSlug(request.getName()));
        }
        list.setDescription(request.getDescription());
        list.setIsPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false);

        list = listRepository.save(list);
        return toResponse(list);
    }

    @Override
    public void sil(Long id, Long kullaniciId) {
        ListEntity list = listRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Liste bulunamadı"));

        if (!list.getUser().getId().equals(kullaniciId)) {
            throw new BadRequestException("Bu listeyi silme yetkiniz yok");
        }

        list.setIsActive(false);
        listRepository.save(list);
    }

    @Override
    public void haberEkle(Long listeId, Long haberId, Long kullaniciId) {
        ListEntity list = listRepository.findById(listeId)
                .orElseThrow(() -> new ResourceNotFoundException("Liste bulunamadı"));

        if (!list.getUser().getId().equals(kullaniciId)) {
            throw new BadRequestException("Bu listeye haber ekleme yetkiniz yok");
        }

        Story story = storyRepository.findById(haberId)
                .orElseThrow(() -> new ResourceNotFoundException("Story bulunamadı"));

        list.getStories().add(story);
        listRepository.save(list);
    }

    @Override
    public void haberCikar(Long listeId, Long haberId, Long kullaniciId) {
        ListEntity list = listRepository.findById(listeId)
                .orElseThrow(() -> new ResourceNotFoundException("Liste bulunamadı"));

        if (!list.getUser().getId().equals(kullaniciId)) {
            throw new BadRequestException("Bu listeden haber çıkarma yetkiniz yok");
        }

        Story story = storyRepository.findById(haberId)
                .orElseThrow(() -> new ResourceNotFoundException("Story bulunamadı"));

        list.getStories().remove(story);
        listRepository.save(list);
    }

    private String generateUniqueSlug(String ad) {
        String baseSlug = SlugUtil.toSlug(ad);
        String slug = baseSlug;
        int counter = 1;

        while (listRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    private ListResponse toResponse(ListEntity list) {
        ListResponse response = new ListResponse();
        response.setId(list.getId());
        response.setName(list.getName());
        response.setSlug(list.getSlug());
        response.setDescription(list.getDescription());
        response.setIsPrivate(list.getIsPrivate());
        response.setUserId(list.getUser().getId());
        response.setUsername(list.getUser().getUsername() != null
                ? list.getUser().getUsername()
                : list.getUser().getEmail());
        response.setCreatedAt(list.getCreatedAt());
        response.setUpdatedAt(list.getUpdatedAt());
        
        if (list.getStories() != null) {
            response.setStories(list.getStories().stream()
                    .map(this::storyToResponse)
                    .collect(Collectors.toList()));
        }
        
        return response;
    }

    private StoryResponse storyToResponse(Story story) {
        StoryResponse response = new StoryResponse();
        response.setId(story.getId());
        response.setBaslik(story.getTitle());
        response.setSlug(story.getSlug());
        response.setOzet(story.getSummary());
        response.setContent(story.getContent()); // İçerik eklendi
        response.setIcerik(story.getContent()); // Backward compatibility için
        String coverImageUrl = story.getCoverImageUrl();
        response.setKapakResmiUrl(coverImageUrl);
        // Debug: coverImageUrl null kontrolü
        if (coverImageUrl == null) {
            System.out.println("⚠️ WARNING: Story ID " + story.getId() + " coverImageUrl is NULL");
        }
        response.setOkunmaSayisi(story.getViewCount());
        response.setBegeniSayisi(story.getLikeCount());
        response.setYorumSayisi(story.getCommentCount());
        response.setKullaniciId(story.getUser().getId());
        response.setKullaniciAdi(story.getUser().getUsername() != null
                ? story.getUser().getUsername()
                : story.getUser().getEmail());
        // Yazarın rollerini ekle
        response.setKullaniciRolleri(story.getUser().getRoles().stream()
                .map(role -> role.getName())
                .collect(java.util.stream.Collectors.toSet()));
        response.setCreatedAt(story.getCreatedAt());
        response.setUpdatedAt(story.getUpdatedAt());
        return response;
    }
}

