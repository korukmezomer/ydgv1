package com.example.backend.integration;

import com.example.backend.application.dto.request.ListCreateRequest;
import com.example.backend.application.dto.response.ListResponse;
import com.example.backend.application.service.ListService;
import com.example.backend.domain.entity.ListEntity;
import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.ListRepository;
import com.example.backend.domain.repository.RoleRepository;
import com.example.backend.domain.repository.StoryRepository;
import com.example.backend.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ListServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ListService listService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ListRepository listRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Story testStory;

    @BeforeEach
    void setUp() {
        Role userRole = createRoleIfNotExists("USER");
        
        testUser = new User();
        testUser.setEmail("listuser@test.com");
        testUser.setUsername("listuser");
        testUser.setFirstName("List");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setIsActive(true);
        testUser.setRoles(Set.of(userRole));
        testUser = userRepository.save(testUser);

        User writer = createTestWriter();
        testStory = createTestStory("Story for List", writer.getId());
    }

    @Test
    void testCreateList() {
        ListCreateRequest request = new ListCreateRequest();
        request.setName("My Reading List");
        request.setDescription("List description");

        ListResponse response = listService.olustur(testUser.getId(), request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("My Reading List", response.getName());
        assertEquals(testUser.getId(), response.getUserId());

        // Veritabanında kontrol et
        ListEntity savedList = listRepository.findById(response.getId()).orElse(null);
        assertNotNull(savedList);
        assertEquals(testUser.getId(), savedList.getUser().getId());
    }

    @Test
    void testAddStoryToList() {
        // Önce bir liste oluştur
        ListEntity testList = createTestList("Test List");

        // Story'yi listeye ekle
        listService.haberEkle(testList.getId(), testStory.getId(), testUser.getId());

        // Liste içeriğini kontrol et
        ListEntity updatedList = listRepository.findById(testList.getId()).orElse(null);
        assertNotNull(updatedList);
        assertTrue(updatedList.getStories().stream()
                .anyMatch(s -> s.getId().equals(testStory.getId())));
    }

    @Test
    void testRemoveStoryFromList() {
        // Önce bir liste oluştur ve story ekle
        ListEntity testList = createTestList("Test List");
        testList.getStories().add(testStory);
        testList = listRepository.save(testList);

        // Story'yi listeden çıkar
        listService.haberCikar(testList.getId(), testStory.getId(), testUser.getId());

        // Liste içeriğini kontrol et
        ListEntity updatedList = listRepository.findById(testList.getId()).orElse(null);
        assertNotNull(updatedList);
        assertFalse(updatedList.getStories().stream()
                .anyMatch(s -> s.getId().equals(testStory.getId())));
    }

    private ListEntity createTestList(String title) {
        ListEntity list = new ListEntity();
        list.setName(title);
        list.setSlug(title.toLowerCase().replace(" ", "-"));
        list.setDescription("Description");
        list.setUser(testUser);
        list.setIsPrivate(false);
        return listRepository.save(list);
    }

    private Story createTestStory(String title, Long userId) {
        Story story = new Story();
        story.setTitle(title);
        story.setContent("Content");
        story.setSummary("Summary");
        story.setSlug(title.toLowerCase().replace(" ", "-"));
        story.setUser(userRepository.findById(userId).orElse(testUser));
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setViewCount(0L);
        story.setLikeCount(0L);
        story.setCommentCount(0L);
        story.setIsActive(true);
        return storyRepository.save(story);
    }

    private User createTestWriter() {
        Role writerRole = createRoleIfNotExists("WRITER");
        User writer = new User();
        writer.setEmail("writer@test.com");
        writer.setUsername("writer");
        writer.setFirstName("Writer");
        writer.setPassword(passwordEncoder.encode("password123"));
        writer.setIsActive(true);
        writer.setRoles(Set.of(writerRole));
        return userRepository.save(writer);
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

