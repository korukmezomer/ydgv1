package com.example.backend.integration;

import com.example.backend.application.dto.request.CategoryCreateRequest;
import com.example.backend.application.dto.response.CategoryResponse;
import com.example.backend.application.service.CategoryService;
import com.example.backend.domain.entity.Category;
import com.example.backend.domain.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class CategoryServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        // Her test öncesi temizlik (create-drop sayesinde otomatik)
    }

    @Test
    void testCreateCategory() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Technology");
        request.setDescription("Tech category description");

        CategoryResponse response = categoryService.create(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Technology", response.getName());
        assertEquals("technology", response.getSlug()); // Slug otomatik oluşturulmalı

        // Veritabanında kontrol et
        Category savedCategory = categoryRepository.findById(response.getId()).orElse(null);
        assertNotNull(savedCategory);
        assertEquals("Technology", savedCategory.getName());
    }

    @Test
    void testFindById() {
        Category category = createTestCategory("Science", "science");

        CategoryResponse response = categoryService.findById(category.getId());

        assertNotNull(response);
        assertEquals(category.getId(), response.getId());
        assertEquals("Science", response.getName());
    }

    @Test
    void testFindBySlug() {
        Category category = createTestCategory("Health", "health");

        CategoryResponse response = categoryService.findBySlug("health");

        assertNotNull(response);
        assertEquals(category.getId(), response.getId());
        assertEquals("Health", response.getName());
    }

    @Test
    void testUpdateCategory() {
        Category category = createTestCategory("Old Name", "old-name");
        Long categoryId = category.getId();

        CategoryCreateRequest updateRequest = new CategoryCreateRequest();
        updateRequest.setName("New Name");
        updateRequest.setDescription("New description");

        CategoryResponse response = categoryService.update(categoryId, updateRequest);

        assertNotNull(response);
        assertEquals("New Name", response.getName());
        assertEquals("new-name", response.getSlug()); // Name değiştiği için slug güncellenmeli

        // Veritabanında kontrol et
        Category updatedCategory = categoryRepository.findById(categoryId).orElse(null);
        assertNotNull(updatedCategory);
        assertEquals("New Name", updatedCategory.getName());
    }

    @Test
    void testDeleteCategory() {
        Category category = createTestCategory("To Delete", "to-delete");
        Long categoryId = category.getId();
        assertTrue(category.getIsActive());

        categoryService.delete(categoryId);

        // Soft delete yapılıyor, kategori hala veritabanında ama aktif değil
        Category deletedCategory = categoryRepository.findById(categoryId).orElse(null);
        assertNotNull(deletedCategory);
        assertFalse(deletedCategory.getIsActive());
    }

    private Category createTestCategory(String name, String slug) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setDescription("Description for " + name);
        return categoryRepository.save(category);
    }
}

