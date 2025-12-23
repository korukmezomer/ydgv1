package com.example.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "listeler")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListEntity extends BaseEntity {

    @Column(name = "ad", nullable = false, length = 60)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "aciklama", columnDefinition = "TEXT")
    private String description;

    @Column(name = "gizli", nullable = false)
    private Boolean isPrivate = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false)
    private User user;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "list_stories",
            joinColumns = @JoinColumn(name = "list_id"),
            inverseJoinColumns = @JoinColumn(name = "story_id")
    )
    private Set<Story> stories = new HashSet<>();
}

