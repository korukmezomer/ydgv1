package com.example.backend.application.dto.response;

import com.example.backend.domain.entity.MediaFile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MediaFileResponse {
    private Long id;
    private String fileName;
    private String originalFileName;
    private String filePath;
    private String url;
    private String mimeType;
    private Long fileSize;
    private Long uploaderUserId;
    private MediaFile.FileType fileType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
