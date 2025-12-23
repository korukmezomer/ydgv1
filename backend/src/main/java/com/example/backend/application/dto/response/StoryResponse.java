package com.example.backend.application.dto.response;

import com.example.backend.domain.entity.Story.StoryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoryResponse {
    private Long id;
    private String baslik;
    private String slug;
    private String ozet;
    private String content; // İçerik field'ı eklendi
    private String icerik; // Backward compatibility için
    private String kapakResmiUrl;
    private StoryStatus durum;
    private LocalDateTime yayinlanmaTarihi;
    private Long okunmaSayisi;
    private Long begeniSayisi;
    private Long yorumSayisi;
    private Boolean isEditorPick;
    private String metaDescription;
    private Long kullaniciId;
    private String kullaniciAdi;
    private java.util.Set<String> kullaniciRolleri; // Yazarın rolleri
    private Long kategoriId;
    private String kategoriAdi;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


