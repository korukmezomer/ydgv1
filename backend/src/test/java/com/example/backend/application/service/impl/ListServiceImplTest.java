package com.example.backend.application.service.impl;

import com.example.backend.application.dto.request.ListCreateRequest;
import com.example.backend.application.dto.response.ListResponse;
import com.example.backend.domain.entity.ListEntity;
import com.example.backend.domain.entity.Story;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.ListRepository;
import com.example.backend.domain.repository.StoryRepository;
import com.example.backend.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
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
}

