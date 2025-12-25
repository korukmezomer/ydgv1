package com.example.backend.integration;

import com.example.backend.application.dto.request.CategoryCreateRequest;
import com.example.backend.domain.entity.Category;
import com.example.backend.domain.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CategoryControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CategoryRepository categoryRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testCreateCategory() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Test Category");
        request.setDescription("Test description");

        mockMvc.perform(post("/api/kategoriler")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Category"));
    }

    @Test
    void testFindById() throws Exception {
        Category category = createTestCategory("Find By Id Category");

        mockMvc.perform(get("/api/kategoriler/{id}", category.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()));
    }

    @Test
    void testFindBySlug() throws Exception {
        Category category = createTestCategory("Find By Slug Category");

        mockMvc.perform(get("/api/kategoriler/slug/{slug}", category.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value(category.getSlug()));
    }

    @Test
    void testFindAll() throws Exception {
        createTestCategory("Category 1");
        createTestCategory("Category 2");

        mockMvc.perform(get("/api/kategoriler"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testFindAllSayfali() throws Exception {
        createTestCategory("Category 1");
        createTestCategory("Category 2");

        mockMvc.perform(get("/api/kategoriler/sayfali")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testUpdateCategory() throws Exception {
        Category category = createTestCategory("Update Category");

        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Updated Category");
        request.setDescription("Updated description");

        mockMvc.perform(put("/api/kategoriler/{id}", category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Category"));
    }

    @Test
    void testDeleteCategory() throws Exception {
        Category category = createTestCategory("Delete Category");

        mockMvc.perform(delete("/api/kategoriler/{id}", category.getId()))
                .andExpect(status().isNoContent());

        Category deleted = categoryRepository.findById(category.getId()).orElse(null);
        assertNotNull(deleted);
        assertFalse(deleted.getIsActive());
    }

    @Test
    void testCreateCategoryDuplicateName() throws Exception {
        createTestCategory("Duplicate Category");

        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Duplicate Category");

        mockMvc.perform(post("/api/kategoriler")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFindByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/kategoriler/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    private Category createTestCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(name.toLowerCase().replace(" ", "-"));
        category.setDescription("Description");
        category.setIsActive(true);
        return categoryRepository.save(category);
    }
}

