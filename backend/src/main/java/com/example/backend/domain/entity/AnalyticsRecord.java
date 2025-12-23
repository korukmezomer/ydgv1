package com.example.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "analiz_kayitlari")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(name = "olay_tipi", nullable = false, length = 50)
    private String eventType;

    @Column(name = "kullanici_id")
    private Long userId;

    @Column(name = "ip_adresi", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "referer", length = 500)
    private String referer;

    @Column(name = "olay_tarihi", nullable = false)
    private LocalDateTime eventDate;
}

