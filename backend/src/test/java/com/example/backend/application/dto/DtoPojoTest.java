package com.example.backend.application.dto;

import com.example.backend.application.dto.request.*;
import com.example.backend.application.dto.response.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DTO'lar için elle set/get doğrulamaları (reflection yok).
 */
class DtoPojoTest {

    @Test
    void requestDtos_shouldSetAndGetFields() {
        AuthorProfileUpdateRequest authorReq = new AuthorProfileUpdateRequest();
        authorReq.setBio("bio");
        authorReq.setAvatarUrl("a");
        authorReq.setWebsite("w");
        authorReq.setTwitterHandle("t");
        authorReq.setLinkedinUrl("l");
        assertEquals("bio", authorReq.getBio());

        CategoryCreateRequest categoryReq = new CategoryCreateRequest();
        categoryReq.setName("cat");
        assertEquals("cat", categoryReq.getName());

        CommentCreateRequest commentReq = new CommentCreateRequest();
        commentReq.setContent("c");
        commentReq.setParentCommentId(1L);
        assertEquals("c", commentReq.getContent());

        ListCreateRequest listReq = new ListCreateRequest();
        listReq.setName("list");
        listReq.setDescription("desc");
        listReq.setIsPrivate(true);
        assertTrue(listReq.getIsPrivate());

        StoryCreateRequest storyReq = new StoryCreateRequest();
        storyReq.setBaslik("t");
        storyReq.setIcerik("c");
        storyReq.setKategoriId(1L);
        storyReq.setEtiketler(List.of("tag1"));
        assertEquals("t", storyReq.getBaslik());

        StoryUpdateRequest storyUpdateReq = new StoryUpdateRequest();
        storyUpdateReq.setBaslik("t2");
        storyUpdateReq.setIcerik("c2");
        storyUpdateReq.setOzet("o2");
        storyUpdateReq.setKapakResmiUrl("/cover2");
        storyUpdateReq.setMetaDescription("meta2");
        assertEquals("t2", storyUpdateReq.getBaslik());
        assertEquals("o2", storyUpdateReq.getOzet());
        assertEquals("/cover2", storyUpdateReq.getKapakResmiUrl());
        assertEquals("meta2", storyUpdateReq.getMetaDescription());

        TagCreateRequest tagReq = new TagCreateRequest();
        tagReq.setName("tag");
        assertEquals("tag", tagReq.getName());

        UserLoginRequest loginReq = new UserLoginRequest();
        loginReq.setEmail("e");
        loginReq.setPassword("p");
        assertEquals("e", loginReq.getEmail());

        UserRegistrationRequest regReq = new UserRegistrationRequest();
        regReq.setEmail("r");
        regReq.setPassword("p");
        regReq.setFirstName("f");
        regReq.setLastName("l");
        regReq.setUsername("u");
        regReq.setRoleName("USER");
        assertEquals("u", regReq.getUsername());

        UserUpdateRequest userUpdateReq = new UserUpdateRequest();
        userUpdateReq.setFirstName("f2");
        userUpdateReq.setLastName("l2");
        userUpdateReq.setUsername("u2");
        assertEquals("u2", userUpdateReq.getUsername());
    }

    @Test
    void responseDtos_shouldSetAndGetFields() {
        AuthorProfileResponse authorRes = new AuthorProfileResponse();
        authorRes.setId(1L);
        authorRes.setUsername("u");
        assertEquals("u", authorRes.getUsername());

        CategoryResponse categoryRes = new CategoryResponse();
        categoryRes.setId(2L);
        categoryRes.setName("cat");
        assertEquals("cat", categoryRes.getName());

        CommentResponse commentRes = new CommentResponse();
        commentRes.setId(3L);
        commentRes.setContent("c");
        commentRes.setStoryId(33L);
        commentRes.setUserId(44L);
        commentRes.setParentCommentId(55L);
        assertEquals("c", commentRes.getContent());
        assertEquals(33L, commentRes.getStoryId());
        assertEquals(44L, commentRes.getUserId());

        JwtResponse jwtRes = new JwtResponse("token", "Bearer", 1L, "email", "user", java.util.Set.of("USER"));
        assertEquals("token", jwtRes.getToken());

        ListResponse listRes = new ListResponse();
        listRes.setId(4L);
        listRes.setName("list");
        assertEquals("list", listRes.getName());

        MediaFileResponse mediaRes = new MediaFileResponse();
        mediaRes.setId(5L);
        mediaRes.setUrl("url");
        assertEquals("url", mediaRes.getUrl());

        NotificationResponse notifRes = new NotificationResponse();
        notifRes.setId(6L);
        notifRes.setTitle("title");
        notifRes.setMessage("m");
        notifRes.setType(com.example.backend.domain.entity.Notification.NotificationType.HABER_YAYINLANDI);
        notifRes.setIsRead(false);
        notifRes.setRelatedStoryId(10L);
        notifRes.setRelatedStorySlug("slug");
        notifRes.setRelatedCommentId(11L);
        notifRes.setCreatedAt(LocalDateTime.now());
        assertEquals("m", notifRes.getMessage());
        assertEquals("title", notifRes.getTitle());
        assertEquals("slug", notifRes.getRelatedStorySlug());

        PageResponse<String> pageRes = new PageResponse<>();
        pageRes.setContent(List.of("a", "b"));
        pageRes.setPage(0);
        pageRes.setSize(2);
        pageRes.setTotalPages(1);
        pageRes.setTotalElements(2L);
        pageRes.setFirst(true);
        pageRes.setLast(false);
        assertEquals(2, pageRes.getContent().size());
        assertTrue(pageRes.isFirst());
        assertFalse(pageRes.isLast());

        StoryResponse storyRes = new StoryResponse();
        storyRes.setId(7L);
        storyRes.setBaslik("t");
        storyRes.setSlug("s");
        storyRes.setOzet("o");
        storyRes.setIcerik("c");
        storyRes.setKapakResmiUrl("/cover");
        storyRes.setDurum(com.example.backend.domain.entity.Story.StoryStatus.YAYINLANDI);
        storyRes.setMetaDescription("meta");
        storyRes.setKullaniciAdi("author");
        storyRes.setKullaniciRolleri(java.util.Set.of("WRITER"));
        storyRes.setYorumSayisi(3L);
        storyRes.setBegeniSayisi(5L);
        storyRes.setOkunmaSayisi(8L);
        storyRes.setCreatedAt(LocalDateTime.now());
        storyRes.setUpdatedAt(LocalDateTime.now());
        assertEquals("t", storyRes.getBaslik());
        assertEquals("o", storyRes.getOzet());
        assertEquals("/cover", storyRes.getKapakResmiUrl());
        assertEquals(com.example.backend.domain.entity.Story.StoryStatus.YAYINLANDI, storyRes.getDurum());
        assertEquals("author", storyRes.getKullaniciAdi());

        TagResponse tagRes = new TagResponse();
        tagRes.setId(8L);
        tagRes.setName("tag");
        assertEquals("tag", tagRes.getName());

        UserResponse userRes = new UserResponse();
        userRes.setId(9L);
        userRes.setEmail("e");
        userRes.setUsername("u");
        assertEquals("u", userRes.getUsername());
    }
}


