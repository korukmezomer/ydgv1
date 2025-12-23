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
@Table(name = "bultenler")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Newsletter extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "ad", length = 50)
    private String firstName;

    @Column(name = "soyad", length = 50)
    private String lastName;

    @Column(name = "aktif", nullable = false)
    private Boolean isActive = true;

    @Column(name = "abone_olma_tarihi", nullable = false)
    private LocalDateTime subscriptionDate;

    @Column(name = "abonelik_iptal_tarihi")
    private LocalDateTime unsubscriptionDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "bulten_kategoriler",
            joinColumns = @JoinColumn(name = "bulten_id"),
            inverseJoinColumns = @JoinColumn(name = "kategori_id")
    )
    private Set<Category> interests = new HashSet<>();
}

