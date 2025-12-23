package com.example.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ortam_dosyalari")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MediaFile extends BaseEntity {

    @Column(name = "dosya_adi", nullable = false, length = 255)
    private String fileName;

    @Column(name = "orijinal_dosya_adi", length = 255)
    private String originalFileName;

    @Column(name = "dosya_yolu", nullable = false, length = 500)
    private String filePath;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "dosya_boyutu")
    private Long fileSize;

    @Column(name = "yukleyen_kullanici_id")
    private Long uploaderUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "dosya_tipi", length = 20)
    private FileType fileType;

    public enum FileType {
        RESIM,
        VIDEO,
        DOKUMAN,
        DIGER
    }
}

