package com.example.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bildirimler")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false)
    private User user;

    @Column(name = "baslik", nullable = false, length = 200)
    private String title;

    @Column(name = "mesaj", nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "okundu", nullable = false)
    private Boolean isRead = false;

    @Column(name = "ilgili_story_id")
    private Long relatedStoryId;

    @Column(name = "ilgili_yorum_id")
    private Long relatedCommentId;

    public enum NotificationType {
        YENI_YORUM,
        YORUM_YANITI,
        YORUM_BEGENILDI,
        HABER_BEGENILDI,
        HABER_YAYINLANDI,
        HABER_REDDEDILDI,
        YENI_TAKIPCI,
        GENEL
    }
}

