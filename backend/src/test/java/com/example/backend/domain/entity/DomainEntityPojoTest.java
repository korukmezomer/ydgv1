package com.example.backend.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * POJO tarzı getter/setter ve temel eşitlik kontrolleri.
 * Reflection kullanılmaz; her entity için alanlar elle set edilip doğrulanır.
 */
class DomainEntityPojoTest {

    @Test
    void user_and_role_fields_shouldBeSetAndReadable() {
        Role role = new Role();
        role.setId(10L);
        role.setName("ADMIN");
        role.setIsActive(true);

        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setUsername("user1");
        user.setPassword("pwd");
        user.setFirstName("Name");
        user.setLastName("Last");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        assertEquals("user@example.com", user.getEmail());
        assertEquals("user1", user.getUsername());
        assertTrue(user.getRoles().contains(role));
        assertNotNull(user.toString());
        assertEquals(user, user); // equals path
    }

    @Test
    void story_fields_shouldBeSetAndReadable() {
        Story story = new Story();
        story.setId(100L);
        story.setTitle("Title");
        story.setContent("Content");
        story.setSlug("slug");
        story.setSummary("sum");
        story.setCoverImageUrl("/cover.png");
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        User owner = new User();
        owner.setId(2L);
        story.setUser(owner);
        story.setLikeCount(5L);
        story.setCommentCount(3L);
        story.setViewCount(10L);
        story.setCreatedAt(LocalDateTime.now());
        story.setUpdatedAt(LocalDateTime.now());
        story.setPublishedAt(LocalDateTime.now());
        story.setIsEditorPick(true);
        story.setMetaDescription("meta");
        Tag tag = new Tag();
        tag.setId(99L);
        story.getTags().add(tag);

        assertEquals("Title", story.getTitle());
        assertEquals(Story.StoryStatus.YAYINLANDI, story.getStatus());
        assertEquals(owner, story.getUser());
        assertTrue(story.getTags().contains(tag));
        assertEquals("meta", story.getMetaDescription());
        assertNotNull(story.toString());
    }

    @Test
    void comment_like_follow_savedStory_shouldLinkUserAndStory() {
        User user = new User();
        user.setId(1L);
        Story story = new Story();
        story.setId(2L);

        Comment comment = new Comment();
        comment.setId(3L);
        comment.setContent("c");
        comment.setUser(user);
        comment.setStory(story);
        comment.setStatus(Comment.CommentStatus.ONAYLANDI);
        assertEquals(story, comment.getStory());
        assertEquals(Comment.CommentStatus.ONAYLANDI, comment.getStatus());

        Like like = new Like();
        like.setId(4L);
        like.setUser(user);
        like.setStory(story);
        assertEquals(user, like.getUser());
        assertEquals(story, like.getStory());

        Follow follow = new Follow();
        follow.setId(5L);
        follow.setFollower(user);
        follow.setFollowed(user);
        assertEquals(user, follow.getFollower());
        assertEquals(user, follow.getFollowed());

        SavedStory savedStory = new SavedStory();
        savedStory.setId(6L);
        savedStory.setUser(user);
        savedStory.setStory(story);
        assertEquals(user, savedStory.getUser());
        assertEquals(story, savedStory.getStory());
    }

    @Test
    void notification_shouldSetAllFields() {
        User user = new User();
        user.setId(1L);
        Notification notification = new Notification();
        notification.setId(7L);
        notification.setUser(user);
        notification.setTitle("title");
        notification.setMessage("msg");
        notification.setType(Notification.NotificationType.HABER_YAYINLANDI);
        notification.setIsRead(true);
        notification.setRelatedStoryId(123L);
        notification.setRelatedCommentId(456L);
        notification.setCreatedAt(LocalDateTime.now());

        assertEquals("msg", notification.getMessage());
        assertEquals(Notification.NotificationType.HABER_YAYINLANDI, notification.getType());
        assertTrue(notification.getIsRead());
        assertEquals(user, notification.getUser());
        assertEquals("title", notification.getTitle());
        assertEquals(123L, notification.getRelatedStoryId());
        assertEquals(456L, notification.getRelatedCommentId());
    }

    @Test
    void metadataEntities_shouldBeSettable() {
        Tag tag = new Tag();
        tag.setId(11L);
        tag.setName("tech");
        assertEquals("tech", tag.getName());

        Category category = new Category();
        category.setId(12L);
        category.setName("cat");
        assertEquals("cat", category.getName());

        MediaFile mediaFile = new MediaFile();
        mediaFile.setId(13L);
        mediaFile.setFileType(MediaFile.FileType.RESIM);
        mediaFile.setFilePath("/u.png");
        mediaFile.setFileSize(123L);
        assertEquals(MediaFile.FileType.RESIM, mediaFile.getFileType());
        assertEquals("/u.png", mediaFile.getFilePath());

        ListEntity list = new ListEntity();
        list.setId(14L);
        list.setName("list");
        list.setDescription("desc");
        assertEquals("list", list.getName());
        assertEquals("desc", list.getDescription());
    }

    @Test
    void analytics_storyVersion_subscription_report_newsletter_shouldWork() {
        Story story = new Story();
        story.setId(1L);

        AnalyticsRecord record = new AnalyticsRecord();
        record.setId(15L);
        record.setStory(story);
        record.setEventType("VIEW");
        record.setUserId(99L);
        record.setIpAddress("127.0.0.1");
        record.setUserAgent("ua");
        record.setReferer("ref");
        record.setEventDate(LocalDateTime.now());
        assertEquals("VIEW", record.getEventType());
        assertEquals("ua", record.getUserAgent());

        StoryVersion version = new StoryVersion();
        version.setId(16L);
        version.setStory(story);
        version.setVersionNumber(1);
        version.setTitle("t");
        version.setContent("c");
        version.setChangeNote("n");
        assertEquals(1, version.getVersionNumber());
        assertEquals("n", version.getChangeNote());

        Subscription subscription = new Subscription();
        subscription.setId(17L);
        subscription.setPlanType(Subscription.PlanType.PREMIUM);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusDays(30));
        subscription.setIsActive(true);
        assertEquals(Subscription.PlanType.PREMIUM, subscription.getPlanType());
        assertTrue(subscription.getIsActive());

        Report report = new Report();
        report.setId(18L);
        report.setTargetType(Report.ReportTargetType.HABER);
        report.setReason(Report.ReportReason.SPAM);
        report.setStatus(Report.ReportStatus.BEKLIYOR);
        report.setDescription("d");
        assertEquals(Report.ReportReason.SPAM, report.getReason());
        assertEquals("d", report.getDescription());

        Newsletter newsletter = new Newsletter();
        newsletter.setId(19L);
        newsletter.setEmail("n@example.com");
        newsletter.setFirstName("f");
        newsletter.setLastName("l");
        newsletter.setIsActive(true);
        assertEquals("n@example.com", newsletter.getEmail());
        assertTrue(newsletter.getIsActive());
    }

    @Test
    void authorProfile_shouldSetFields() {
        User user = new User();
        user.setId(20L);
        AuthorProfile profile = new AuthorProfile();
        profile.setId(21L);
        profile.setUser(user);
        profile.setBio("bio");
        profile.setAvatarUrl("a");
        profile.setWebsite("w");
        profile.setTwitterHandle("t");
        profile.setLinkedinUrl("l");
        profile.setTotalViewCount(1L);
        profile.setTotalLikeCount(2L);

        assertEquals("bio", profile.getBio());
        assertEquals(user, profile.getUser());
    }

    @Test
    void equals_hashCode_shouldHandleNullAndDifferentType() {
        User user = new User();
        user.setId(1L);
        assertNotEquals(user, null);
        assertNotEquals(user, "string");
        assertEquals(user, user);
        assertEquals(user.hashCode(), user.hashCode());
    }
}


