package com.example.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {
    
    @NotBlank(message = "Kategori adı zorunludur")
    @Size(max = 100, message = "Kategori adı en fazla 100 karakter olabilir")
    private String name;
    
    @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir")
    private String description;
}
