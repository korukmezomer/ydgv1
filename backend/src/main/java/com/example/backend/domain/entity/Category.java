package com.example.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "kategoriler")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseEntity {

    @Column(name = "kategori_adi", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "aciklama", length = 500)
    private String description;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private Set<Story> stories = new HashSet<>();
}

