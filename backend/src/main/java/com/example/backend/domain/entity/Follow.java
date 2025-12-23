package com.example.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "takip", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"takipci_id", "takip_edilen_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Follow extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "takipci_id", nullable = false)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "takip_edilen_id", nullable = false)
    private User followed;
}

