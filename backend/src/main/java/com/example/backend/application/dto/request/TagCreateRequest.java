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
public class TagCreateRequest {
    
    @NotBlank(message = "Etiket adı zorunludur")
    @Size(max = 50, message = "Etiket adı en fazla 50 karakter olabilir")
    private String name;
}

