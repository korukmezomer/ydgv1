package com.example.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "story_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoryVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(name = "surum_numarasi", nullable = false)
    private Integer versionNumber;

    @Column(name = "baslik", length = 200)
    private String title;

    @Column(name = "icerik", columnDefinition = "TEXT")
    private String content;

    @Column(name = "degisiklik_notu", length = 500)
    private String changeNote;
}

