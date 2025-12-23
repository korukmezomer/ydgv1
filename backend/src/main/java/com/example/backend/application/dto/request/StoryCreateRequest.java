package com.example.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoryCreateRequest {
    
    @NotBlank(message = "Başlık zorunludur")
    @Size(max = 200, message = "Başlık en fazla 200 karakter olabilir")
    private String baslik;
    
    @NotBlank(message = "İçerik zorunludur")
    private String icerik;
    
    @Size(max = 500, message = "Özet en fazla 500 karakter olabilir")
    private String ozet;
    
    private Long kategoriId;
    
    private String kapakResmiUrl;
    
    @Size(max = 160, message = "Meta description en fazla 160 karakter olabilir")
    private String metaDescription;
    
    private List<String> etiketler;
}

