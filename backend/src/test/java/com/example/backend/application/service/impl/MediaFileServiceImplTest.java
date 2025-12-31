package com.example.backend.application.service.impl;

import com.example.backend.application.dto.response.MediaFileResponse;
import com.example.backend.application.exception.ForbiddenException;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.domain.entity.MediaFile;
import com.example.backend.domain.repository.MediaFileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaFileServiceImplTest {

    @Mock
    private MediaFileRepository mediaFileRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private MediaFileServiceImpl mediaFileService;

    @Test
    void findById_shouldReturnMediaFileResponse() {
        Long fileId = 1L;
        MediaFile mediaFile = new MediaFile();
        mediaFile.setId(fileId);
        mediaFile.setFileName("test.jpg");
        mediaFile.setFileType(MediaFile.FileType.RESIM);

        when(mediaFileRepository.findById(fileId)).thenReturn(Optional.of(mediaFile));

        ReflectionTestUtils.setField(mediaFileService, "serverPort", "8080");

        MediaFileResponse response = mediaFileService.findById(fileId);

        assertNotNull(response);
        assertEquals(fileId, response.getId());
        assertEquals("test.jpg", response.getFileName());
    }

    @Test
    void deleteFile_shouldDeleteFileFromDiskAndDatabase() throws IOException {
        Long fileId = 1L;
        Long userId = 2L;

        MediaFile mediaFile = new MediaFile();
        mediaFile.setId(fileId);
        mediaFile.setUploaderUserId(userId);
        mediaFile.setFilePath("2024/01/test.jpg");

        when(mediaFileRepository.findById(fileId)).thenReturn(Optional.of(mediaFile));

        ReflectionTestUtils.setField(mediaFileService, "uploadDir", "uploads");

        // Create temp directory for test
        Path tempDir = Files.createTempDirectory("test-uploads");
        ReflectionTestUtils.setField(mediaFileService, "uploadDir", tempDir.toString());

        mediaFileService.deleteFile(fileId, userId);

        verify(mediaFileRepository, times(1)).delete(mediaFile);
    }

    @Test
    void deleteFile_shouldThrowExceptionWhenUserNotOwner() {
        Long fileId = 1L;
        Long userId = 2L;
        Long otherUserId = 3L;

        MediaFile mediaFile = new MediaFile();
        mediaFile.setId(fileId);
        mediaFile.setUploaderUserId(otherUserId);

        when(mediaFileRepository.findById(fileId)).thenReturn(Optional.of(mediaFile));

        assertThrows(ForbiddenException.class, () -> mediaFileService.deleteFile(fileId, userId));
        verify(mediaFileRepository, never()).delete(any(MediaFile.class));
    }

    @Test
    void findById_shouldThrowExceptionWhenNotFound() {
        Long fileId = 999L;
        when(mediaFileRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> mediaFileService.findById(fileId));
    }

    @Test
    void deleteFile_shouldThrowExceptionWhenFileNotFound() {
        Long fileId = 999L;
        Long userId = 1L;

        when(mediaFileRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> mediaFileService.deleteFile(fileId, userId));
        verify(mediaFileRepository, never()).delete(any(MediaFile.class));
    }
}

