package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.TagCreateRequest;
import com.example.backend.application.dto.response.TagResponse;
import com.example.backend.application.exception.BadRequestException;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.domain.entity.Tag;
import com.example.backend.domain.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    @Test
    void create_shouldCreateTagWithSlug() {
        TagCreateRequest request = new TagCreateRequest();
        request.setName("Java Programming");

        when(tagRepository.existsByName("Java Programming")).thenReturn(false);
        when(tagRepository.existsBySlug(anyString())).thenReturn(false);

        Tag saved = new Tag();
        saved.setId(1L);
        saved.setName(request.getName());
        saved.setSlug("java-programming");

        when(tagRepository.save(any(Tag.class))).thenReturn(saved);

        TagResponse response = tagService.create(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Java Programming", response.getName());
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    void create_shouldThrowExceptionWhenNameExists() {
        TagCreateRequest request = new TagCreateRequest();
        request.setName("Existing Tag");

        when(tagRepository.existsByName("Existing Tag")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> tagService.create(request));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void findById_shouldReturnTagResponse() {
        Long tagId = 1L;
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setName("Test Tag");
        tag.setSlug("test-tag");

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        TagResponse response = tagService.findById(tagId);

        assertNotNull(response);
        assertEquals(tagId, response.getId());
        assertEquals("Test Tag", response.getName());
        assertEquals("test-tag", response.getSlug());
    }

    @Test
    void findById_shouldThrowExceptionWhenNotFound() {
        Long tagId = 999L;
        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tagService.findById(tagId));
    }

    @Test
    void findBySlug_shouldReturnTagResponse() {
        String slug = "test-tag";
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Test Tag");
        tag.setSlug(slug);

        when(tagRepository.findBySlug(slug)).thenReturn(Optional.of(tag));

        TagResponse response = tagService.findBySlug(slug);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Tag", response.getName());
        assertEquals(slug, response.getSlug());
    }

    @Test
    void findBySlug_shouldThrowExceptionWhenNotFound() {
        String slug = "non-existent";
        when(tagRepository.findBySlug(slug)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tagService.findBySlug(slug));
    }

    @Test
    void findAll_shouldReturnListOfTagResponses() {
        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("Tag 1");
        tag1.setSlug("tag-1");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("Tag 2");
        tag2.setSlug("tag-2");

        when(tagRepository.findAll()).thenReturn(List.of(tag1, tag2));

        List<TagResponse> responses = tagService.findAll();

        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(2L, responses.get(1).getId());
    }

    @Test
    void findAll_withPageable_shouldReturnPageOfTagResponses() {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Test Tag");
        tag.setSlug("test-tag");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Tag> tagPage = new PageImpl<>(List.of(tag), pageable, 1);

        when(tagRepository.findAll(pageable)).thenReturn(tagPage);

        Page<TagResponse> responsePage = tagService.findAll(pageable);

        assertEquals(1, responsePage.getTotalElements());
        assertEquals(1L, responsePage.getContent().get(0).getId());
    }

    @Test
    void update_shouldUpdateTagFields() {
        Long tagId = 1L;
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setName("Old Name");
        tag.setSlug("old-name");

        TagCreateRequest request = new TagCreateRequest();
        request.setName("New Name");

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(tagRepository.existsByName("New Name")).thenReturn(false);
        when(tagRepository.existsBySlug(anyString())).thenReturn(false);

        Tag saved = new Tag();
        saved.setId(tagId);
        saved.setName("New Name");
        saved.setSlug("new-name");
        when(tagRepository.save(any(Tag.class))).thenReturn(saved);

        TagResponse response = tagService.update(tagId, request);

        assertNotNull(response);
        assertEquals("New Name", response.getName());
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    void update_shouldThrowExceptionWhenNameExists() {
        Long tagId = 1L;
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setName("Old Name");

        TagCreateRequest request = new TagCreateRequest();
        request.setName("Existing Name");

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(tagRepository.existsByName("Existing Name")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> tagService.update(tagId, request));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void update_shouldThrowExceptionWhenTagNotFound() {
        Long tagId = 999L;
        TagCreateRequest request = new TagCreateRequest();
        request.setName("New Name");

        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tagService.update(tagId, request));
    }

    @Test
    void delete_shouldSetTagInactive() {
        Long tagId = 1L;
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setIsActive(true);

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        tagService.delete(tagId);

        assertFalse(tag.getIsActive());
        verify(tagRepository, times(1)).save(tag);
    }

    @Test
    void delete_shouldThrowExceptionWhenTagNotFound() {
        Long tagId = 999L;
        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tagService.delete(tagId));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void create_shouldGenerateUniqueSlugWhenSlugExists() {
        TagCreateRequest request = new TagCreateRequest();
        request.setName("Test Tag");

        when(tagRepository.existsByName("Test Tag")).thenReturn(false);
        when(tagRepository.existsBySlug("test-tag")).thenReturn(true);
        when(tagRepository.existsBySlug("test-tag-1")).thenReturn(false);

        Tag saved = new Tag();
        saved.setId(1L);
        saved.setName(request.getName());
        saved.setSlug("test-tag-1");

        when(tagRepository.save(any(Tag.class))).thenReturn(saved);

        TagResponse response = tagService.create(request);

        assertNotNull(response);
        assertEquals("test-tag-1", response.getSlug());
    }
}

