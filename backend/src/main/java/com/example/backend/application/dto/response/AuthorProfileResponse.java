package com.example.backend.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorProfileResponse {
    
    private Long id;
    private Long userId;
    private String username;
    private String bio;
    private String avatarUrl;
    private String website;
    private String twitterHandle;
    private String linkedinUrl;
    private Long totalViewCount;
    private Long totalLikeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
