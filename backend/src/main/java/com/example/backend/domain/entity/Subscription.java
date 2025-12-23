package com.example.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "abonelikler")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_tipi", nullable = false, length = 20)
    private PlanType planType;

    @Column(name = "baslangic_tarihi", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "bitis_tarihi")
    private LocalDateTime endDate;

    @Column(name = "aktif", nullable = false)
    private Boolean isActive = true;

    @Column(name = "odeme_id", length = 100)
    private String paymentId;

    public enum PlanType {
        UCRETSIZ,
        PREMIUM,
        YAZAR_PREMIUM
    }
}

