package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.CategoryCreateRequest;
import com.example.backend.application.dto.response.CategoryResponse;
import com.example.backend.application.exception.BadRequestException;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.domain.entity.Category;
import com.example.backend.domain.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void create_shouldCreateCategoryWithSlug() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Technology");
        request.setDescription("Tech category");

        when(categoryRepository.existsByName("Technology")).thenReturn(false);
        when(categoryRepository.existsBySlug(anyString())).thenReturn(false);

        Category saved = new Category();
        saved.setId(1L);
        saved.setName(request.getName());
        saved.setSlug("technology");

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse response = categoryService.create(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Technology", response.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void create_shouldThrowExceptionWhenNameExists() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Existing");

        when(categoryRepository.existsByName("Existing")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.create(request));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void update_shouldUpdateCategoryFields() {
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        category.setName("Old Name");

        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("New Name");
        request.setDescription("New Description");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("New Name")).thenReturn(false);

        Category saved = new Category();
        saved.setId(categoryId);
        saved.setName("New Name");
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse response = categoryService.update(categoryId, request);

        assertNotNull(response);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void delete_shouldSetCategoryInactive() {
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        category.setIsActive(true);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        categoryService.delete(categoryId);

        assertFalse(category.getIsActive());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void findById_shouldReturnCategoryResponse() {
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        category.setName("Technology");
        category.setSlug("technology");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.findById(categoryId);

        assertNotNull(response);
        assertEquals(categoryId, response.getId());
        assertEquals("Technology", response.getName());
    }

    @Test
    void findById_shouldThrowExceptionWhenNotFound() {
        Long categoryId = 999L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(categoryId));
    }

    @Test
    void findBySlug_shouldReturnCategoryResponse() {
        String slug = "technology";
        Category category = new Category();
        category.setId(1L);
        category.setName("Technology");
        category.setSlug(slug);

        when(categoryRepository.findBySlug(slug)).thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.findBySlug(slug);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Technology", response.getName());
        assertEquals(slug, response.getSlug());
    }

    @Test
    void findBySlug_shouldThrowExceptionWhenNotFound() {
        String slug = "non-existent";
        when(categoryRepository.findBySlug(slug)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.findBySlug(slug));
    }

    @Test
    void findAll_shouldReturnListOfCategories() {
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Tech");
        category1.setIsActive(true);

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Science");
        category2.setIsActive(true);

        when(categoryRepository.findByIsActiveTrue()).thenReturn(List.of(category1, category2));

        List<CategoryResponse> responses = categoryService.findAll();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(categoryRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void findAll_withPageable_shouldReturnPageOfCategories() {
        Pageable pageable = PageRequest.of(0, 10);
        Category category = new Category();
        category.setId(1L);
        category.setName("Technology");
        category.setIsActive(true);

        Page<Category> categoryPage = new PageImpl<>(List.of(category));
        when(categoryRepository.findByIsActiveTrue(pageable)).thenReturn(categoryPage);

        Page<CategoryResponse> response = categoryService.findAll(pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(categoryRepository, times(1)).findByIsActiveTrue(pageable);
    }

    @Test
    void update_shouldNotUpdateSlugWhenNameUnchanged() {
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        category.setName("Technology");
        category.setSlug("technology");

        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Technology"); // Same name
        request.setDescription("New Description");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        Category saved = new Category();
        saved.setId(categoryId);
        saved.setName("Technology");
        saved.setSlug("technology"); // Slug should remain same
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse response = categoryService.update(categoryId, request);

        assertNotNull(response);
        assertEquals("technology", response.getSlug());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void update_shouldThrowExceptionWhenNameExists() {
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        category.setName("Old Name");

        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Existing Name");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Existing Name")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.update(categoryId, request));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void update_shouldThrowExceptionWhenCategoryNotFound() {
        Long categoryId = 999L;
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("New Name");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.update(categoryId, request));
    }

    @Test
    void delete_shouldThrowExceptionWhenCategoryNotFound() {
        Long categoryId = 999L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.delete(categoryId));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void create_shouldGenerateUniqueSlugWhenSlugExists() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Test Category");

        when(categoryRepository.existsByName("Test Category")).thenReturn(false);
        when(categoryRepository.existsBySlug("test-category")).thenReturn(true);
        when(categoryRepository.existsBySlug("test-category-1")).thenReturn(false);

        Category saved = new Category();
        saved.setId(1L);
        saved.setName(request.getName());
        saved.setSlug("test-category-1");

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse response = categoryService.create(request);

        assertNotNull(response);
        assertEquals("test-category-1", response.getSlug());
    }
}

