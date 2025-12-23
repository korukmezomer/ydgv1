package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.CategoryCreateRequest;
import com.example.backend.application.dto.response.CategoryResponse;
import com.example.backend.domain.entity.Category;
import com.example.backend.domain.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        assertThrows(RuntimeException.class, () -> categoryService.create(request));
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
}

