package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.ListCreateRequest;
import com.example.backend.application.dto.response.ListResponse;
import com.example.backend.application.exception.BadRequestException;
import com.example.backend.application.exception.ResourceNotFoundException;
import com.example.backend.domain.entity.ListEntity;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.ListRepository;
import com.example.backend.domain.repository.StoryRepository;
import com.example.backend.domain.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListServiceImplTest {

    @Mock
    private ListRepository listRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoryRepository storyRepository;

    @InjectMocks
    private ListServiceImpl listService;

    @Test
    void olustur_shouldCreateListForUser() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        ListCreateRequest request = new ListCreateRequest();
        request.setName("Okuma Listem");
        request.setDescription("Açıklama");
        request.setIsPrivate(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ListEntity saved = new ListEntity();
        saved.setId(10L);
        saved.setName(request.getName());
        saved.setUser(user);
        saved.setStories(new HashSet<>());

        when(listRepository.save(any(ListEntity.class))).thenReturn(saved);

        ListResponse response = listService.olustur(userId, request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Okuma Listem", response.getName());
        verify(listRepository, times(1)).save(any(ListEntity.class));
    }

    @Test
    void haberEkle_shouldAddStoryToList() {
        Long listId = 1L;
        Long storyId = 2L;
        Long userId = 3L;

        User owner = new User();
        owner.setId(userId);

        ListEntity list = new ListEntity();
        list.setId(listId);
        list.setUser(owner);
        list.setStories(new HashSet<>());

        Story story = new Story();
        story.setId(storyId);

        when(listRepository.findById(listId)).thenReturn(Optional.of(list));
        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

        listService.haberEkle(listId, storyId, userId);

        assertTrue(list.getStories().contains(story));
        verify(listRepository, times(1)).save(list);
    }

    @Test
    void haberCikar_shouldRemoveStoryFromList() {
        Long listId = 1L;
        Long storyId = 2L;
        Long userId = 3L;

        User owner = new User();
        owner.setId(userId);

        Story story = new Story();
        story.setId(storyId);

        ListEntity list = new ListEntity();
        list.setId(listId);
        list.setUser(owner);
        list.setStories(new HashSet<>());
        list.getStories().add(story);

        when(listRepository.findById(listId)).thenReturn(Optional.of(list));
        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

        listService.haberCikar(listId, storyId, userId);

        assertFalse(list.getStories().contains(story));
        verify(listRepository, times(1)).save(list);
    }

    @Test
    void olustur_shouldThrowExceptionWhenUserNotFound() {
        Long userId = 999L;
        ListCreateRequest request = new ListCreateRequest();
        request.setName("Test List");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> listService.olustur(userId, request));
        verify(listRepository, never()).save(any(ListEntity.class));
    }

    @Test
    void findById_shouldReturnListResponse() {
        Long listId = 1L;
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        ListEntity list = new ListEntity();
        list.setId(listId);
        list.setName("Test List");
        list.setUser(user);
        list.setStories(new HashSet<>());

        when(listRepository.findById(listId)).thenReturn(Optional.of(list));

        ListResponse response = listService.findById(listId);

        assertNotNull(response);
        assertEquals(listId, response.getId());
        assertEquals("Test List", response.getName());
    }

    @Test
    void findById_shouldThrowExceptionWhenNotFound() {
        Long listId = 999L;
        when(listRepository.findById(listId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> listService.findById(listId));
    }

    @Test
    void findBySlug_shouldReturnListResponse() {
        String slug = "test-list";
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        ListEntity list = new ListEntity();
        list.setId(1L);
        list.setName("Test List");
        list.setSlug(slug);
        list.setUser(user);
        list.setStories(new HashSet<>());

        when(listRepository.findBySlug(slug)).thenReturn(Optional.of(list));

        ListResponse response = listService.findBySlug(slug);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(slug, response.getSlug());
    }

    @Test
    void findBySlug_shouldThrowExceptionWhenNotFound() {
        String slug = "non-existent";
        when(listRepository.findBySlug(slug)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> listService.findBySlug(slug));
    }

    @Test
    void findByKullaniciId_shouldReturnPageOfLists() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        ListEntity list = new ListEntity();
        list.setId(1L);
        list.setName("Test List");
        list.setUser(user);
        list.setStories(new HashSet<>());

        Page<ListEntity> listPage = new PageImpl<>(List.of(list));
        when(listRepository.findByUserIdAndIsActiveTrue(userId, pageable)).thenReturn(listPage);

        Page<ListResponse> response = listService.findByKullaniciId(userId, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(listRepository, times(1)).findByUserIdAndIsActiveTrue(userId, pageable);
    }

    @Test
    void guncelle_shouldUpdateListFields() {
        Long listId = 1L;
        Long userId = 1L;

        User owner = new User();
        owner.setId(userId);

        ListEntity list = new ListEntity();
        list.setId(listId);
        list.setName("Old Name");
        list.setSlug("old-name");
        list.setUser(owner);

        ListCreateRequest request = new ListCreateRequest();
        request.setName("New Name");
        request.setDescription("New Description");
        request.setIsPrivate(false);

        when(listRepository.findById(listId)).thenReturn(Optional.of(list));
        when(listRepository.existsBySlug(anyString())).thenReturn(false);
        when(listRepository.save(any(ListEntity.class))).thenAnswer(invocation -> {
            ListEntity saved = invocation.getArgument(0);
            saved.setName("New Name");
            saved.setDescription("New Description");
            saved.setSlug("new-name");
            return saved;
        });

        ListResponse response = listService.guncelle(listId, userId, request);

        assertNotNull(response);
        assertEquals("New Name", list.getName());
        assertEquals("New Description", list.getDescription());
        verify(listRepository, times(1)).save(list);
    }

    @Test
    void guncelle_shouldThrowExceptionWhenUserNotOwner() {
        Long listId = 1L;
        Long ownerId = 1L;
        Long otherUserId = 2L;

        User owner = new User();
        owner.setId(ownerId);

        ListEntity list = new ListEntity();
        list.setId(listId);
        list.setUser(owner);

        ListCreateRequest request = new ListCreateRequest();
        request.setName("New Name");

        when(listRepository.findById(listId)).thenReturn(Optional.of(list));

        assertThrows(BadRequestException.class, () -> listService.guncelle(listId, otherUserId, request));
        verify(listRepository, never()).save(any(ListEntity.class));
    }

    @Test
    void sil_shouldSetListInactive() {
        Long listId = 1L;
        Long userId = 1L;

        User owner = new User();
        owner.setId(userId);

        ListEntity list = new ListEntity();
        list.setId(listId);
        list.setUser(owner);
        list.setIsActive(true);

        when(listRepository.findById(listId)).thenReturn(Optional.of(list));

        listService.sil(listId, userId);

        assertFalse(list.getIsActive());
        verify(listRepository, times(1)).save(list);
    }

    @Test
    void sil_shouldThrowExceptionWhenUserNotOwner() {
        Long listId = 1L;
        Long ownerId = 1L;
        Long otherUserId = 2L;

        User owner = new User();
        owner.setId(ownerId);

        ListEntity list = new ListEntity();
        list.setId(listId);
        list.setUser(owner);

        when(listRepository.findById(listId)).thenReturn(Optional.of(list));

        assertThrows(BadRequestException.class, () -> listService.sil(listId, otherUserId));
        verify(listRepository, never()).save(any(ListEntity.class));
    }

    @Test
    void haberEkle_shouldThrowExceptionWhenListNotFound() {
        Long listId = 999L;
        Long storyId = 1L;
        Long userId = 1L;

        when(listRepository.findById(listId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> listService.haberEkle(listId, storyId, userId));
    }

    @Test
    void haberEkle_shouldThrowExceptionWhenStoryNotFound() {
        Long listId = 1L;
        Long storyId = 999L;
        Long userId = 1L;

        User owner = new User();
        owner.setId(userId);

        ListEntity list = new ListEntity();
        list.setId(listId);
        list.setUser(owner);

        when(listRepository.findById(listId)).thenReturn(Optional.of(list));
        when(storyRepository.findById(storyId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> listService.haberEkle(listId, storyId, userId));
    }

    @Test
    void haberCikar_shouldThrowExceptionWhenListNotFound() {
        Long listId = 999L;
        Long storyId = 1L;
        Long userId = 1L;

        when(listRepository.findById(listId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> listService.haberCikar(listId, storyId, userId));
    }

    @Test
    void haberCikar_shouldThrowExceptionWhenStoryNotFound() {
        Long listId = 1L;
        Long storyId = 999L;
        Long userId = 1L;

        User owner = new User();
        owner.setId(userId);

        ListEntity list = new ListEntity();
        list.setId(listId);
        list.setUser(owner);

        when(listRepository.findById(listId)).thenReturn(Optional.of(list));
        when(storyRepository.findById(storyId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> listService.haberCikar(listId, storyId, userId));
    }
}

