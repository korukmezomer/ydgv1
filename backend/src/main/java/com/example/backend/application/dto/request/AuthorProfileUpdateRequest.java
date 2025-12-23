package com.example.backend.application.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorProfileUpdateRequest {
    
    @Size(max = 500, message = "Bio en fazla 500 karakter olabilir")
    private String bio;
    
    @Size(max = 255, message = "Avatar URL en fazla 255 karakter olabilir")
    private String avatarUrl;
    
    @Size(max = 255, message = "Web sitesi URL en fazla 255 karakter olabilir")
    private String website;
    
    @Size(max = 50, message = "Twitter handle en fazla 50 karakter olabilir")
    private String twitterHandle;
    
    @Size(max = 255, message = "LinkedIn URL en fazla 255 karakter olabilir")
    private String linkedinUrl;
}
