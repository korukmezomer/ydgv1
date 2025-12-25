package com.example.backend.integration;

import com.example.backend.application.dto.request.ListCreateRequest;
import com.example.backend.domain.entity.ListEntity;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.ListRepository;
import com.example.backend.domain.repository.RoleRepository;
import com.example.backend.domain.repository.StoryRepository;
import com.example.backend.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ListControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ListRepository listRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User user;
    private Story story;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        Role userRole = createRoleIfNotExists("USER");
        Role writerRole = createRoleIfNotExists("WRITER");
        
        user = new User();
        user.setEmail("listuser@test.com");
        user.setUsername("listuser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setIsActive(true);
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);
        
        User writer = new User();
        writer.setEmail("writer@test.com");
        writer.setUsername("writer");
        writer.setPassword(passwordEncoder.encode("password123"));
        writer.setIsActive(true);
        writer.setRoles(Set.of(writerRole));
        writer = userRepository.save(writer);
        
        story = new Story();
        story.setTitle("List Test Story");
        story.setContent("Content");
        story.setSlug("list-test-story");
        story.setUser(writer);
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setIsActive(true);
        story = storyRepository.save(story);
    }

    @Test
    void testCreateList() throws Exception {
        ListCreateRequest request = new ListCreateRequest();
        request.setName("Test List");
        request.setDescription("Test description");
        request.setIsPrivate(false);

        mockMvc.perform(post("/api/listeler")
                        .principal(() -> user.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test List"));
    }

    @Test
    void testFindById() throws Exception {
        ListEntity list = createTestList("Find By Id List", user.getId());

        mockMvc.perform(get("/api/listeler/{id}", list.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(list.getId()));
    }

    @Test
    void testFindBySlug() throws Exception {
        ListEntity list = createTestList("Find By Slug List", user.getId());

        mockMvc.perform(get("/api/listeler/slug/{slug}", list.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value(list.getSlug()));
    }

    @Test
    void testFindByKullaniciId() throws Exception {
        createTestList("List 1", user.getId());
        createTestList("List 2", user.getId());

        mockMvc.perform(get("/api/listeler")
                        .principal(() -> user.getEmail())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testUpdateList() throws Exception {
        ListEntity list = createTestList("Update List", user.getId());

        ListCreateRequest request = new ListCreateRequest();
        request.setName("Updated List");
        request.setDescription("Updated description");

        mockMvc.perform(put("/api/listeler/{id}", list.getId())
                        .principal(() -> user.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated List"));
    }

    @Test
    void testDeleteList() throws Exception {
        ListEntity list = createTestList("Delete List", user.getId());

        mockMvc.perform(delete("/api/listeler/{id}", list.getId())
                        .principal(() -> user.getEmail()))
                .andExpect(status().isNoContent());

        ListEntity deletedList = listRepository.findById(list.getId()).orElse(null);
        assertNotNull(deletedList);
        assertFalse(deletedList.getIsActive());
    }

    @Test
    void testAddStoryToList() throws Exception {
        ListEntity list = createTestList("Add Story List", user.getId());

        mockMvc.perform(post("/api/listeler/{listeId}/haber/{haberId}", list.getId(), story.getId())
                        .principal(() -> user.getEmail()))
                .andExpect(status().isOk());

        ListEntity updatedList = listRepository.findById(list.getId()).orElse(null);
        assertNotNull(updatedList);
        assertTrue(updatedList.getStories().contains(story));
    }

    @Test
    void testRemoveStoryFromList() throws Exception {
        ListEntity list = createTestList("Remove Story List", user.getId());
        list.getStories().add(story);
        listRepository.save(list);

        mockMvc.perform(delete("/api/listeler/{listeId}/haber/{haberId}", list.getId(), story.getId())
                        .principal(() -> user.getEmail()))
                .andExpect(status().isOk());

        ListEntity updatedList = listRepository.findById(list.getId()).orElse(null);
        assertNotNull(updatedList);
        assertFalse(updatedList.getStories().contains(story));
    }

    @Test
    void testCreateListUnauthorized() throws Exception {
        ListCreateRequest request = new ListCreateRequest();
        request.setName("Unauthorized List");

        mockMvc.perform(post("/api/listeler")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    private ListEntity createTestList(String name, Long userId) {
        ListEntity list = new ListEntity();
        list.setName(name);
        list.setSlug(name.toLowerCase().replace(" ", "-"));
        list.setUser(userRepository.findById(userId).orElse(user));
        list.setIsPrivate(false);
        list.setIsActive(true);
        return listRepository.save(list);
    }

    private Role createRoleIfNotExists(String roleName) {
        return roleRepository.findByName(roleName).orElseGet(() -> {
            Role role = new Role();
            role.setName(roleName);
            role.setIsActive(true);
            return roleRepository.save(role);
        });
    }
}

