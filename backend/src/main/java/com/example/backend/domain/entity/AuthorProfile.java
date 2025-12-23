package com.example.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "yazar_profilleri")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false, unique = true)
    private User user;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "web_sitesi", length = 255)
    private String website;

    @Column(name = "twitter_handle", length = 50)
    private String twitterHandle;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "toplam_okunma_sayisi")
    private Long totalViewCount = 0L;

    @Column(name = "toplam_begeni_sayisi")
    private Long totalLikeCount = 0L;
}

