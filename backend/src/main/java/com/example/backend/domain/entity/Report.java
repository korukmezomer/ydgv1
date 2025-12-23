package com.example.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "raporlar")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Report extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raporlayan_kullanici_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "hedef_tip", nullable = false, length = 20)
    private ReportTargetType targetType;

    @Column(name = "ilgili_story_id")
    private Long relatedStoryId;

    @Column(name = "ilgili_yorum_id")
    private Long relatedCommentId;

    @Column(name = "ilgili_kullanici_id")
    private Long relatedUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sebep", nullable = false, length = 50)
    private ReportReason reason;

    @Column(name = "aciklama", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "durum", nullable = false, length = 20)
    private ReportStatus status = ReportStatus.BEKLIYOR;

    @Column(name = "inceleme_notu", columnDefinition = "TEXT")
    private String reviewNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inceleyici_kullanici_id")
    private User reviewer;

    public enum ReportTargetType {
        HABER,
        YORUM,
        KULLANICI
    }

    public enum ReportReason {
        SPAM,
        UYGUNSUZ_ICERIK,
        YANILTICI_BILGI,
        NEFRET_SOYLEMI,
        TELIF_IHLALI,
        DIGER
    }

    public enum ReportStatus {
        BEKLIYOR,
        INCELENIYOR,
        ONAYLANDI,
        REDDEDILDI
    }
}

