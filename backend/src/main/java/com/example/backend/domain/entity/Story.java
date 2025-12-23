package com.example.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "stories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Story extends BaseEntity {

    @Column(name = "baslik", nullable = false, length = 200)
    private String title;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "ozet", length = 500)
    private String summary;

    @Column(name = "icerik", columnDefinition = "TEXT")
    private String content;

    @Column(name = "kapak_resmi_url", length = 255)
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "durum", nullable = false, length = 20)
    private StoryStatus status = StoryStatus.TASLAK;

    @Column(name = "yayinlanma_tarihi")
    private LocalDateTime publishedAt;

    @Column(name = "okunma_sayisi")
    private Long viewCount = 0L;

    @Column(name = "begeni_sayisi")
    private Long likeCount = 0L;

    @Column(name = "yorum_sayisi")
    private Long commentCount = 0L;

    @Column(name = "editor_secimi", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isEditorPick = false;

    @Column(name = "meta_description", length = 160)
    private String metaDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kategori_id")
    private Category category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "story_tags",
            joinColumns = @JoinColumn(name = "story_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StoryVersion> versions = new HashSet<>();

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes = new HashSet<>();

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SavedStory> savedStories = new HashSet<>();

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AnalyticsRecord> analytics = new HashSet<>();

    public enum StoryStatus {
        TASLAK,
        YAYIN_BEKLIYOR,
        YAYINLANDI,
        REDDEDILDI,
        ARSIVLENDI
    }
}
