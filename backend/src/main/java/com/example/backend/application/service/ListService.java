package com.example.backend.application.service;

import com.example.backend.application.dto.request.ListCreateRequest;
import com.example.backend.application.dto.response.ListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListService {
    
    ListResponse olustur(Long kullaniciId, ListCreateRequest request);
    
    ListResponse findById(Long id);
    
    ListResponse findBySlug(String slug);
    
    Page<ListResponse> findByKullaniciId(Long kullaniciId, Pageable pageable);
    
    ListResponse guncelle(Long id, Long kullaniciId, ListCreateRequest request);
    
    void sil(Long id, Long kullaniciId);
    
    void haberEkle(Long listeId, Long haberId, Long kullaniciId);
    
    void haberCikar(Long listeId, Long haberId, Long kullaniciId);
}

