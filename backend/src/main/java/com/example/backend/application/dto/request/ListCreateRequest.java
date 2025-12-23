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
public class ListCreateRequest {
    
    @NotBlank(message = "Liste adı boş olamaz")
    @Size(max = 60, message = "Liste adı en fazla 60 karakter olabilir")
    private String name;
    
    @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir")
    private String description;
    
    private Boolean isPrivate = false;
}
