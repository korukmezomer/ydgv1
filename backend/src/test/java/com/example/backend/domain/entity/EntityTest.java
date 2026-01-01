package com.example.backend.domain.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    // ========== ENUM TESTS ==========
    
    @Test
    void commentStatus_shouldHaveAllValues() {
        Comment.CommentStatus[] statuses = Comment.CommentStatus.values();
        assertEquals(4, statuses.length);
        assertTrue(contains(statuses, Comment.CommentStatus.BEKLIYOR));
        assertTrue(contains(statuses, Comment.CommentStatus.ONAYLANDI));
        assertTrue(contains(statuses, Comment.CommentStatus.REDDEDILDI));
        assertTrue(contains(statuses, Comment.CommentStatus.SPAM));
    }

    @Test
    void commentStatus_valueOf_shouldWork() {
        assertEquals(Comment.CommentStatus.BEKLIYOR, Comment.CommentStatus.valueOf("BEKLIYOR"));
        assertEquals(Comment.CommentStatus.ONAYLANDI, Comment.CommentStatus.valueOf("ONAYLANDI"));
        assertEquals(Comment.CommentStatus.REDDEDILDI, Comment.CommentStatus.valueOf("REDDEDILDI"));
        assertEquals(Comment.CommentStatus.SPAM, Comment.CommentStatus.valueOf("SPAM"));
    }

    @Test
    void storyStatus_shouldHaveAllValues() {
        Story.StoryStatus[] statuses = Story.StoryStatus.values();
        assertEquals(5, statuses.length);
        assertTrue(contains(statuses, Story.StoryStatus.TASLAK));
        assertTrue(contains(statuses, Story.StoryStatus.YAYIN_BEKLIYOR));
        assertTrue(contains(statuses, Story.StoryStatus.YAYINLANDI));
        assertTrue(contains(statuses, Story.StoryStatus.REDDEDILDI));
        assertTrue(contains(statuses, Story.StoryStatus.ARSIVLENDI));
    }

    @Test
    void storyStatus_valueOf_shouldWork() {
        assertEquals(Story.StoryStatus.TASLAK, Story.StoryStatus.valueOf("TASLAK"));
        assertEquals(Story.StoryStatus.YAYIN_BEKLIYOR, Story.StoryStatus.valueOf("YAYIN_BEKLIYOR"));
        assertEquals(Story.StoryStatus.YAYINLANDI, Story.StoryStatus.valueOf("YAYINLANDI"));
        assertEquals(Story.StoryStatus.REDDEDILDI, Story.StoryStatus.valueOf("REDDEDILDI"));
        assertEquals(Story.StoryStatus.ARSIVLENDI, Story.StoryStatus.valueOf("ARSIVLENDI"));
    }

    @Test
    void reportTargetType_shouldHaveAllValues() {
        Report.ReportTargetType[] types = Report.ReportTargetType.values();
        assertEquals(3, types.length);
        assertTrue(contains(types, Report.ReportTargetType.HABER));
        assertTrue(contains(types, Report.ReportTargetType.YORUM));
        assertTrue(contains(types, Report.ReportTargetType.KULLANICI));
    }

    @Test
    void reportTargetType_valueOf_shouldWork() {
        assertEquals(Report.ReportTargetType.HABER, Report.ReportTargetType.valueOf("HABER"));
        assertEquals(Report.ReportTargetType.YORUM, Report.ReportTargetType.valueOf("YORUM"));
        assertEquals(Report.ReportTargetType.KULLANICI, Report.ReportTargetType.valueOf("KULLANICI"));
    }

    @Test
    void reportReason_shouldHaveAllValues() {
        Report.ReportReason[] reasons = Report.ReportReason.values();
        assertEquals(6, reasons.length);
        assertTrue(contains(reasons, Report.ReportReason.SPAM));
        assertTrue(contains(reasons, Report.ReportReason.UYGUNSUZ_ICERIK));
        assertTrue(contains(reasons, Report.ReportReason.YANILTICI_BILGI));
        assertTrue(contains(reasons, Report.ReportReason.NEFRET_SOYLEMI));
        assertTrue(contains(reasons, Report.ReportReason.TELIF_IHLALI));
        assertTrue(contains(reasons, Report.ReportReason.DIGER));
    }

    @Test
    void reportReason_valueOf_shouldWork() {
        assertEquals(Report.ReportReason.SPAM, Report.ReportReason.valueOf("SPAM"));
        assertEquals(Report.ReportReason.UYGUNSUZ_ICERIK, Report.ReportReason.valueOf("UYGUNSUZ_ICERIK"));
        assertEquals(Report.ReportReason.YANILTICI_BILGI, Report.ReportReason.valueOf("YANILTICI_BILGI"));
        assertEquals(Report.ReportReason.NEFRET_SOYLEMI, Report.ReportReason.valueOf("NEFRET_SOYLEMI"));
        assertEquals(Report.ReportReason.TELIF_IHLALI, Report.ReportReason.valueOf("TELIF_IHLALI"));
        assertEquals(Report.ReportReason.DIGER, Report.ReportReason.valueOf("DIGER"));
    }

    @Test
    void reportStatus_shouldHaveAllValues() {
        Report.ReportStatus[] statuses = Report.ReportStatus.values();
        assertEquals(4, statuses.length);
        assertTrue(contains(statuses, Report.ReportStatus.BEKLIYOR));
        assertTrue(contains(statuses, Report.ReportStatus.INCELENIYOR));
        assertTrue(contains(statuses, Report.ReportStatus.ONAYLANDI));
        assertTrue(contains(statuses, Report.ReportStatus.REDDEDILDI));
    }

    @Test
    void reportStatus_valueOf_shouldWork() {
        assertEquals(Report.ReportStatus.BEKLIYOR, Report.ReportStatus.valueOf("BEKLIYOR"));
        assertEquals(Report.ReportStatus.INCELENIYOR, Report.ReportStatus.valueOf("INCELENIYOR"));
        assertEquals(Report.ReportStatus.ONAYLANDI, Report.ReportStatus.valueOf("ONAYLANDI"));
        assertEquals(Report.ReportStatus.REDDEDILDI, Report.ReportStatus.valueOf("REDDEDILDI"));
    }

    @Test
    void subscriptionPlanType_shouldHaveAllValues() {
        Subscription.PlanType[] types = Subscription.PlanType.values();
        assertEquals(3, types.length);
        assertTrue(contains(types, Subscription.PlanType.UCRETSIZ));
        assertTrue(contains(types, Subscription.PlanType.PREMIUM));
        assertTrue(contains(types, Subscription.PlanType.YAZAR_PREMIUM));
    }

    @Test
    void subscriptionPlanType_valueOf_shouldWork() {
        assertEquals(Subscription.PlanType.UCRETSIZ, Subscription.PlanType.valueOf("UCRETSIZ"));
        assertEquals(Subscription.PlanType.PREMIUM, Subscription.PlanType.valueOf("PREMIUM"));
        assertEquals(Subscription.PlanType.YAZAR_PREMIUM, Subscription.PlanType.valueOf("YAZAR_PREMIUM"));
    }

    @Test
    void notificationType_shouldHaveAllValues() {
        Notification.NotificationType[] types = Notification.NotificationType.values();
        assertEquals(8, types.length);
        assertTrue(contains(types, Notification.NotificationType.YENI_YORUM));
        assertTrue(contains(types, Notification.NotificationType.YORUM_YANITI));
        assertTrue(contains(types, Notification.NotificationType.YORUM_BEGENILDI));
        assertTrue(contains(types, Notification.NotificationType.HABER_BEGENILDI));
        assertTrue(contains(types, Notification.NotificationType.HABER_YAYINLANDI));
        assertTrue(contains(types, Notification.NotificationType.HABER_REDDEDILDI));
        assertTrue(contains(types, Notification.NotificationType.YENI_TAKIPCI));
        assertTrue(contains(types, Notification.NotificationType.GENEL));
    }

    @Test
    void notificationType_valueOf_shouldWork() {
        assertEquals(Notification.NotificationType.YENI_YORUM, Notification.NotificationType.valueOf("YENI_YORUM"));
        assertEquals(Notification.NotificationType.YORUM_YANITI, Notification.NotificationType.valueOf("YORUM_YANITI"));
        assertEquals(Notification.NotificationType.YORUM_BEGENILDI, Notification.NotificationType.valueOf("YORUM_BEGENILDI"));
        assertEquals(Notification.NotificationType.HABER_BEGENILDI, Notification.NotificationType.valueOf("HABER_BEGENILDI"));
        assertEquals(Notification.NotificationType.HABER_YAYINLANDI, Notification.NotificationType.valueOf("HABER_YAYINLANDI"));
        assertEquals(Notification.NotificationType.HABER_REDDEDILDI, Notification.NotificationType.valueOf("HABER_REDDEDILDI"));
        assertEquals(Notification.NotificationType.YENI_TAKIPCI, Notification.NotificationType.valueOf("YENI_TAKIPCI"));
        assertEquals(Notification.NotificationType.GENEL, Notification.NotificationType.valueOf("GENEL"));
    }

    @Test
    void mediaFileFileType_shouldHaveAllValues() {
        MediaFile.FileType[] types = MediaFile.FileType.values();
        assertEquals(4, types.length);
        assertTrue(contains(types, MediaFile.FileType.RESIM));
        assertTrue(contains(types, MediaFile.FileType.VIDEO));
        assertTrue(contains(types, MediaFile.FileType.DOKUMAN));
        assertTrue(contains(types, MediaFile.FileType.DIGER));
    }

    @Test
    void mediaFileFileType_valueOf_shouldWork() {
        assertEquals(MediaFile.FileType.RESIM, MediaFile.FileType.valueOf("RESIM"));
        assertEquals(MediaFile.FileType.VIDEO, MediaFile.FileType.valueOf("VIDEO"));
        assertEquals(MediaFile.FileType.DOKUMAN, MediaFile.FileType.valueOf("DOKUMAN"));
        assertEquals(MediaFile.FileType.DIGER, MediaFile.FileType.valueOf("DIGER"));
    }

    // ========== BASE ENTITY TESTS ==========

    @Test
    void baseEntity_shouldHaveDefaultValues() {
        User user = new User();
        assertNotNull(user);
        assertTrue(user.getIsActive());
        assertNull(user.getId());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
    }

    @Test
    void baseEntity_shouldSetAndGetFields() {
        User user = new User();
        user.setId(1L);
        user.setIsActive(false);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        assertEquals(1L, user.getId());
        assertFalse(user.getIsActive());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }

    // ========== REPORT ENTITY TESTS ==========

    @Test
    void report_shouldCreateWithDefaultValues() {
        Report report = new Report();
        assertNotNull(report);
        assertEquals(Report.ReportStatus.BEKLIYOR, report.getStatus());
        assertNull(report.getReporter());
        assertNull(report.getTargetType());
        assertNull(report.getRelatedStoryId());
        assertNull(report.getRelatedCommentId());
        assertNull(report.getRelatedUserId());
        assertNull(report.getReason());
        assertNull(report.getDescription());
        assertNull(report.getReviewNote());
        assertNull(report.getReviewer());
    }

    @Test
    void report_shouldSetAndGetAllFields() {
        User reporter = new User();
        User reviewer = new User();
        Report report = new Report();
        
        report.setReporter(reporter);
        report.setTargetType(Report.ReportTargetType.HABER);
        report.setRelatedStoryId(1L);
        report.setRelatedCommentId(2L);
        report.setRelatedUserId(3L);
        report.setReason(Report.ReportReason.SPAM);
        report.setDescription("Test description");
        report.setStatus(Report.ReportStatus.ONAYLANDI);
        report.setReviewNote("Review note");
        report.setReviewer(reviewer);
        
        assertEquals(reporter, report.getReporter());
        assertEquals(Report.ReportTargetType.HABER, report.getTargetType());
        assertEquals(1L, report.getRelatedStoryId());
        assertEquals(2L, report.getRelatedCommentId());
        assertEquals(3L, report.getRelatedUserId());
        assertEquals(Report.ReportReason.SPAM, report.getReason());
        assertEquals("Test description", report.getDescription());
        assertEquals(Report.ReportStatus.ONAYLANDI, report.getStatus());
        assertEquals("Review note", report.getReviewNote());
        assertEquals(reviewer, report.getReviewer());
    }

    @Test
    void report_shouldUseAllArgsConstructor() {
        User reporter = new User();
        User reviewer = new User();
        Report report = new Report(reporter, Report.ReportTargetType.YORUM, 1L, 2L, 3L, 
                Report.ReportReason.UYGUNSUZ_ICERIK, "Description", Report.ReportStatus.INCELENIYOR, 
                "Review note", reviewer);
        
        assertEquals(reporter, report.getReporter());
        assertEquals(Report.ReportTargetType.YORUM, report.getTargetType());
        assertEquals(1L, report.getRelatedStoryId());
        assertEquals(2L, report.getRelatedCommentId());
        assertEquals(3L, report.getRelatedUserId());
        assertEquals(Report.ReportReason.UYGUNSUZ_ICERIK, report.getReason());
        assertEquals("Description", report.getDescription());
        assertEquals(Report.ReportStatus.INCELENIYOR, report.getStatus());
        assertEquals("Review note", report.getReviewNote());
        assertEquals(reviewer, report.getReviewer());
    }

    // ========== ANALYTICS RECORD ENTITY TESTS ==========

    @Test
    void analyticsRecord_shouldCreateWithDefaultValues() {
        AnalyticsRecord record = new AnalyticsRecord();
        assertNotNull(record);
        assertNull(record.getStory());
        assertNull(record.getEventType());
        assertNull(record.getUserId());
        assertNull(record.getIpAddress());
        assertNull(record.getUserAgent());
        assertNull(record.getReferer());
        assertNull(record.getEventDate());
    }

    @Test
    void analyticsRecord_shouldSetAndGetAllFields() {
        Story story = new Story();
        LocalDateTime eventDate = LocalDateTime.now();
        AnalyticsRecord record = new AnalyticsRecord();
        
        record.setStory(story);
        record.setEventType("VIEW");
        record.setUserId(1L);
        record.setIpAddress("192.168.1.1");
        record.setUserAgent("Mozilla/5.0");
        record.setReferer("https://example.com");
        record.setEventDate(eventDate);
        
        assertEquals(story, record.getStory());
        assertEquals("VIEW", record.getEventType());
        assertEquals(1L, record.getUserId());
        assertEquals("192.168.1.1", record.getIpAddress());
        assertEquals("Mozilla/5.0", record.getUserAgent());
        assertEquals("https://example.com", record.getReferer());
        assertEquals(eventDate, record.getEventDate());
    }

    @Test
    void analyticsRecord_shouldUseAllArgsConstructor() {
        Story story = new Story();
        LocalDateTime eventDate = LocalDateTime.now();
        AnalyticsRecord record = new AnalyticsRecord(story, "CLICK", 1L, "192.168.1.1", 
                "Mozilla/5.0", "https://example.com", eventDate);
        
        assertEquals(story, record.getStory());
        assertEquals("CLICK", record.getEventType());
        assertEquals(1L, record.getUserId());
        assertEquals("192.168.1.1", record.getIpAddress());
        assertEquals("Mozilla/5.0", record.getUserAgent());
        assertEquals("https://example.com", record.getReferer());
        assertEquals(eventDate, record.getEventDate());
    }

    // ========== STORY VERSION ENTITY TESTS ==========

    @Test
    void storyVersion_shouldCreateWithDefaultValues() {
        StoryVersion version = new StoryVersion();
        assertNotNull(version);
        assertNull(version.getStory());
        assertNull(version.getVersionNumber());
        assertNull(version.getTitle());
        assertNull(version.getContent());
        assertNull(version.getChangeNote());
    }

    @Test
    void storyVersion_shouldSetAndGetAllFields() {
        Story story = new Story();
        StoryVersion version = new StoryVersion();
        
        version.setStory(story);
        version.setVersionNumber(1);
        version.setTitle("Title");
        version.setContent("Content");
        version.setChangeNote("Change note");
        
        assertEquals(story, version.getStory());
        assertEquals(1, version.getVersionNumber());
        assertEquals("Title", version.getTitle());
        assertEquals("Content", version.getContent());
        assertEquals("Change note", version.getChangeNote());
    }

    @Test
    void storyVersion_shouldUseAllArgsConstructor() {
        Story story = new Story();
        StoryVersion version = new StoryVersion(story, 2, "Title", "Content", "Change note");
        
        assertEquals(story, version.getStory());
        assertEquals(2, version.getVersionNumber());
        assertEquals("Title", version.getTitle());
        assertEquals("Content", version.getContent());
        assertEquals("Change note", version.getChangeNote());
    }

    // ========== NEWSLETTER ENTITY TESTS ==========

    @Test
    void newsletter_shouldCreateWithDefaultValues() {
        Newsletter newsletter = new Newsletter();
        assertNotNull(newsletter);
        assertTrue(newsletter.getIsActive());
        assertNotNull(newsletter.getInterests());
        assertTrue(newsletter.getInterests().isEmpty());
        assertNull(newsletter.getEmail());
        assertNull(newsletter.getFirstName());
        assertNull(newsletter.getLastName());
        assertNull(newsletter.getSubscriptionDate());
        assertNull(newsletter.getUnsubscriptionDate());
    }

    @Test
    void newsletter_shouldSetAndGetAllFields() {
        LocalDateTime subDate = LocalDateTime.now();
        LocalDateTime unsubDate = LocalDateTime.now().plusDays(30);
        Set<Category> interests = new HashSet<>();
        Newsletter newsletter = new Newsletter();
        
        newsletter.setEmail("test@example.com");
        newsletter.setFirstName("John");
        newsletter.setLastName("Doe");
        newsletter.setIsActive(true);
        newsletter.setSubscriptionDate(subDate);
        newsletter.setUnsubscriptionDate(unsubDate);
        newsletter.setInterests(interests);
        
        assertEquals("test@example.com", newsletter.getEmail());
        assertEquals("John", newsletter.getFirstName());
        assertEquals("Doe", newsletter.getLastName());
        assertTrue(newsletter.getIsActive());
        assertEquals(subDate, newsletter.getSubscriptionDate());
        assertEquals(unsubDate, newsletter.getUnsubscriptionDate());
        assertEquals(interests, newsletter.getInterests());
    }

    @Test
    void newsletter_shouldUseAllArgsConstructor() {
        LocalDateTime subDate = LocalDateTime.now();
        LocalDateTime unsubDate = LocalDateTime.now().plusDays(30);
        Set<Category> interests = new HashSet<>();
        Newsletter newsletter = new Newsletter("test@example.com", "John", "Doe", true, 
                subDate, unsubDate, interests);
        
        assertEquals("test@example.com", newsletter.getEmail());
        assertEquals("John", newsletter.getFirstName());
        assertEquals("Doe", newsletter.getLastName());
        assertTrue(newsletter.getIsActive());
        assertEquals(subDate, newsletter.getSubscriptionDate());
        assertEquals(unsubDate, newsletter.getUnsubscriptionDate());
        assertEquals(interests, newsletter.getInterests());
    }

    // ========== SUBSCRIPTION ENTITY TESTS ==========

    @Test
    void subscription_shouldCreateWithDefaultValues() {
        Subscription subscription = new Subscription();
        assertNotNull(subscription);
        assertTrue(subscription.getIsActive());
        assertNull(subscription.getUser());
        assertNull(subscription.getPlanType());
        assertNull(subscription.getStartDate());
        assertNull(subscription.getEndDate());
        assertNull(subscription.getPaymentId());
    }

    @Test
    void subscription_shouldSetAndGetAllFields() {
        User user = new User();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusMonths(1);
        Subscription subscription = new Subscription();
        
        subscription.setUser(user);
        subscription.setPlanType(Subscription.PlanType.PREMIUM);
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        subscription.setIsActive(true);
        subscription.setPaymentId("payment123");
        
        assertEquals(user, subscription.getUser());
        assertEquals(Subscription.PlanType.PREMIUM, subscription.getPlanType());
        assertEquals(startDate, subscription.getStartDate());
        assertEquals(endDate, subscription.getEndDate());
        assertTrue(subscription.getIsActive());
        assertEquals("payment123", subscription.getPaymentId());
    }

    @Test
    void subscription_shouldUseAllArgsConstructor() {
        User user = new User();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusMonths(1);
        Subscription subscription = new Subscription(user, Subscription.PlanType.YAZAR_PREMIUM, 
                startDate, endDate, true, "payment456");
        
        assertEquals(user, subscription.getUser());
        assertEquals(Subscription.PlanType.YAZAR_PREMIUM, subscription.getPlanType());
        assertEquals(startDate, subscription.getStartDate());
        assertEquals(endDate, subscription.getEndDate());
        assertTrue(subscription.getIsActive());
        assertEquals("payment456", subscription.getPaymentId());
    }

    // ========== USER ENTITY TESTS ==========

    @Test
    void user_shouldCreateWithDefaultValues() {
        User user = new User();
        assertNotNull(user);
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().isEmpty());
        assertNotNull(user.getStories());
        assertTrue(user.getStories().isEmpty());
        assertNotNull(user.getComments());
        assertTrue(user.getComments().isEmpty());
        assertNotNull(user.getLikes());
        assertTrue(user.getLikes().isEmpty());
        assertNotNull(user.getFollowing());
        assertTrue(user.getFollowing().isEmpty());
        assertNotNull(user.getFollowers());
        assertTrue(user.getFollowers().isEmpty());
        assertNotNull(user.getSavedStories());
        assertTrue(user.getSavedStories().isEmpty());
        assertNotNull(user.getNotifications());
        assertTrue(user.getNotifications().isEmpty());
        assertNotNull(user.getSubscriptions());
        assertTrue(user.getSubscriptions().isEmpty());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getUsername());
        assertNull(user.getAuthorProfile());
    }

    @Test
    void user_shouldSetAndGetAllFields() {
        User user = new User();
        AuthorProfile profile = new AuthorProfile();
        Set<Role> roles = new HashSet<>();
        Set<Story> stories = new HashSet<>();
        
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("johndoe");
        user.setAuthorProfile(profile);
        user.setRoles(roles);
        user.setStories(stories);
        
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("johndoe", user.getUsername());
        assertEquals(profile, user.getAuthorProfile());
        assertEquals(roles, user.getRoles());
        assertEquals(stories, user.getStories());
    }

    @Test
    void user_shouldUseAllArgsConstructor() {
        AuthorProfile profile = new AuthorProfile();
        Set<Role> roles = new HashSet<>();
        Set<Story> stories = new HashSet<>();
        Set<Comment> comments = new HashSet<>();
        Set<Like> likes = new HashSet<>();
        Set<Follow> following = new HashSet<>();
        Set<Follow> followers = new HashSet<>();
        Set<SavedStory> savedStories = new HashSet<>();
        Set<Notification> notifications = new HashSet<>();
        Set<Subscription> subscriptions = new HashSet<>();
        
        User user = new User("test@example.com", "password123", "John", "Doe", "johndoe", 
                profile, roles, stories, comments, likes, following, followers, savedStories, 
                notifications, subscriptions);
        
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("johndoe", user.getUsername());
        assertEquals(profile, user.getAuthorProfile());
        assertEquals(roles, user.getRoles());
        assertEquals(stories, user.getStories());
        assertEquals(comments, user.getComments());
        assertEquals(likes, user.getLikes());
        assertEquals(following, user.getFollowing());
        assertEquals(followers, user.getFollowers());
        assertEquals(savedStories, user.getSavedStories());
        assertEquals(notifications, user.getNotifications());
        assertEquals(subscriptions, user.getSubscriptions());
    }

    // ========== STORY ENTITY TESTS ==========

    @Test
    void story_shouldCreateWithDefaultValues() {
        Story story = new Story();
        assertNotNull(story);
        assertEquals(Story.StoryStatus.TASLAK, story.getStatus());
        assertEquals(0L, story.getViewCount());
        assertEquals(0L, story.getLikeCount());
        assertEquals(0L, story.getCommentCount());
        assertFalse(story.getIsEditorPick());
        assertNotNull(story.getTags());
        assertTrue(story.getTags().isEmpty());
        assertNotNull(story.getVersions());
        assertTrue(story.getVersions().isEmpty());
        assertNotNull(story.getComments());
        assertTrue(story.getComments().isEmpty());
        assertNotNull(story.getLikes());
        assertTrue(story.getLikes().isEmpty());
        assertNotNull(story.getSavedStories());
        assertTrue(story.getSavedStories().isEmpty());
        assertNotNull(story.getAnalytics());
        assertTrue(story.getAnalytics().isEmpty());
        assertNull(story.getTitle());
        assertNull(story.getSlug());
        assertNull(story.getSummary());
        assertNull(story.getContent());
        assertNull(story.getCoverImageUrl());
        assertNull(story.getPublishedAt());
        assertNull(story.getMetaDescription());
        assertNull(story.getUser());
        assertNull(story.getCategory());
    }

    @Test
    void story_shouldSetAndGetAllFields() {
        User user = new User();
        Category category = new Category();
        Set<Tag> tags = new HashSet<>();
        LocalDateTime publishedAt = LocalDateTime.now();
        Story story = new Story();
        
        story.setTitle("Test Title");
        story.setSlug("test-slug");
        story.setSummary("Test Summary");
        story.setContent("Test Content");
        story.setCoverImageUrl("https://example.com/image.jpg");
        story.setStatus(Story.StoryStatus.YAYINLANDI);
        story.setPublishedAt(publishedAt);
        story.setViewCount(100L);
        story.setLikeCount(50L);
        story.setCommentCount(25L);
        story.setIsEditorPick(true);
        story.setMetaDescription("Meta description");
        story.setUser(user);
        story.setCategory(category);
        story.setTags(tags);
        
        assertEquals("Test Title", story.getTitle());
        assertEquals("test-slug", story.getSlug());
        assertEquals("Test Summary", story.getSummary());
        assertEquals("Test Content", story.getContent());
        assertEquals("https://example.com/image.jpg", story.getCoverImageUrl());
        assertEquals(Story.StoryStatus.YAYINLANDI, story.getStatus());
        assertEquals(publishedAt, story.getPublishedAt());
        assertEquals(100L, story.getViewCount());
        assertEquals(50L, story.getLikeCount());
        assertEquals(25L, story.getCommentCount());
        assertTrue(story.getIsEditorPick());
        assertEquals("Meta description", story.getMetaDescription());
        assertEquals(user, story.getUser());
        assertEquals(category, story.getCategory());
        assertEquals(tags, story.getTags());
    }

    @Test
    void story_shouldUseAllArgsConstructor() {
        User user = new User();
        Category category = new Category();
        Set<Tag> tags = new HashSet<>();
        Set<StoryVersion> versions = new HashSet<>();
        Set<Comment> comments = new HashSet<>();
        Set<Like> likes = new HashSet<>();
        Set<SavedStory> savedStories = new HashSet<>();
        Set<AnalyticsRecord> analytics = new HashSet<>();
        LocalDateTime publishedAt = LocalDateTime.now();
        
        Story story = new Story("Title", "slug", "Summary", "Content", "image.jpg", 
                Story.StoryStatus.YAYINLANDI, publishedAt, 100L, 50L, 25L, true, 
                "Meta", user, category, tags, versions, comments, likes, savedStories, analytics);
        
        assertEquals("Title", story.getTitle());
        assertEquals("slug", story.getSlug());
        assertEquals("Summary", story.getSummary());
        assertEquals("Content", story.getContent());
        assertEquals("image.jpg", story.getCoverImageUrl());
        assertEquals(Story.StoryStatus.YAYINLANDI, story.getStatus());
        assertEquals(publishedAt, story.getPublishedAt());
        assertEquals(100L, story.getViewCount());
        assertEquals(50L, story.getLikeCount());
        assertEquals(25L, story.getCommentCount());
        assertTrue(story.getIsEditorPick());
        assertEquals("Meta", story.getMetaDescription());
        assertEquals(user, story.getUser());
        assertEquals(category, story.getCategory());
        assertEquals(tags, story.getTags());
        assertEquals(versions, story.getVersions());
        assertEquals(comments, story.getComments());
        assertEquals(likes, story.getLikes());
        assertEquals(savedStories, story.getSavedStories());
        assertEquals(analytics, story.getAnalytics());
    }

    // ========== CATEGORY ENTITY TESTS ==========

    @Test
    void category_shouldCreateWithDefaultValues() {
        Category category = new Category();
        assertNotNull(category);
        assertNotNull(category.getStories());
        assertTrue(category.getStories().isEmpty());
        assertNull(category.getName());
        assertNull(category.getDescription());
        assertNull(category.getSlug());
    }

    @Test
    void category_shouldSetAndGetAllFields() {
        Set<Story> stories = new HashSet<>();
        Category category = new Category();
        
        category.setName("Technology");
        category.setDescription("Tech category");
        category.setSlug("technology");
        category.setStories(stories);
        
        assertEquals("Technology", category.getName());
        assertEquals("Tech category", category.getDescription());
        assertEquals("technology", category.getSlug());
        assertEquals(stories, category.getStories());
    }

    @Test
    void category_shouldUseAllArgsConstructor() {
        Set<Story> stories = new HashSet<>();
        Category category = new Category("Science", "Science category", "science", stories);
        
        assertEquals("Science", category.getName());
        assertEquals("Science category", category.getDescription());
        assertEquals("science", category.getSlug());
        assertEquals(stories, category.getStories());
    }

    // ========== ROLE ENTITY TESTS ==========

    @Test
    void role_shouldCreateWithDefaultValues() {
        Role role = new Role();
        assertNotNull(role);
        assertNotNull(role.getUsers());
        assertTrue(role.getUsers().isEmpty());
        assertNull(role.getName());
        assertNull(role.getDescription());
    }

    @Test
    void role_shouldSetAndGetAllFields() {
        Set<User> users = new HashSet<>();
        Role role = new Role();
        
        role.setName("ADMIN");
        role.setDescription("Administrator role");
        role.setUsers(users);
        
        assertEquals("ADMIN", role.getName());
        assertEquals("Administrator role", role.getDescription());
        assertEquals(users, role.getUsers());
    }

    @Test
    void role_shouldUseAllArgsConstructor() {
        Set<User> users = new HashSet<>();
        Role role = new Role("WRITER", "Writer role", users);
        
        assertEquals("WRITER", role.getName());
        assertEquals("Writer role", role.getDescription());
        assertEquals(users, role.getUsers());
    }

    // ========== TAG ENTITY TESTS ==========

    @Test
    void tag_shouldCreateWithDefaultValues() {
        Tag tag = new Tag();
        assertNotNull(tag);
        assertNotNull(tag.getStories());
        assertTrue(tag.getStories().isEmpty());
        assertNull(tag.getName());
        assertNull(tag.getSlug());
    }

    @Test
    void tag_shouldSetAndGetAllFields() {
        Set<Story> stories = new HashSet<>();
        Tag tag = new Tag();
        
        tag.setName("Java");
        tag.setSlug("java");
        tag.setStories(stories);
        
        assertEquals("Java", tag.getName());
        assertEquals("java", tag.getSlug());
        assertEquals(stories, tag.getStories());
    }

    @Test
    void tag_shouldUseAllArgsConstructor() {
        Set<Story> stories = new HashSet<>();
        Tag tag = new Tag("Spring", "spring", stories);
        
        assertEquals("Spring", tag.getName());
        assertEquals("spring", tag.getSlug());
        assertEquals(stories, tag.getStories());
    }

    // ========== AUTHOR PROFILE ENTITY TESTS ==========

    @Test
    void authorProfile_shouldCreateWithDefaultValues() {
        AuthorProfile profile = new AuthorProfile();
        assertNotNull(profile);
        assertEquals(0L, profile.getTotalViewCount());
        assertEquals(0L, profile.getTotalLikeCount());
        assertNull(profile.getUser());
        assertNull(profile.getBio());
        assertNull(profile.getAvatarUrl());
        assertNull(profile.getWebsite());
        assertNull(profile.getTwitterHandle());
        assertNull(profile.getLinkedinUrl());
    }

    @Test
    void authorProfile_shouldSetAndGetAllFields() {
        User user = new User();
        AuthorProfile profile = new AuthorProfile();
        
        profile.setUser(user);
        profile.setBio("Bio text");
        profile.setAvatarUrl("https://example.com/avatar.jpg");
        profile.setWebsite("https://example.com");
        profile.setTwitterHandle("@johndoe");
        profile.setLinkedinUrl("https://linkedin.com/in/johndoe");
        profile.setTotalViewCount(1000L);
        profile.setTotalLikeCount(500L);
        
        assertEquals(user, profile.getUser());
        assertEquals("Bio text", profile.getBio());
        assertEquals("https://example.com/avatar.jpg", profile.getAvatarUrl());
        assertEquals("https://example.com", profile.getWebsite());
        assertEquals("@johndoe", profile.getTwitterHandle());
        assertEquals("https://linkedin.com/in/johndoe", profile.getLinkedinUrl());
        assertEquals(1000L, profile.getTotalViewCount());
        assertEquals(500L, profile.getTotalLikeCount());
    }

    @Test
    void authorProfile_shouldUseAllArgsConstructor() {
        User user = new User();
        AuthorProfile profile = new AuthorProfile(user, "Bio", "avatar.jpg", "website.com", 
                "@handle", "linkedin.com", 2000L, 1000L);
        
        assertEquals(user, profile.getUser());
        assertEquals("Bio", profile.getBio());
        assertEquals("avatar.jpg", profile.getAvatarUrl());
        assertEquals("website.com", profile.getWebsite());
        assertEquals("@handle", profile.getTwitterHandle());
        assertEquals("linkedin.com", profile.getLinkedinUrl());
        assertEquals(2000L, profile.getTotalViewCount());
        assertEquals(1000L, profile.getTotalLikeCount());
    }

    // ========== COMMENT ENTITY TESTS ==========

    @Test
    void comment_shouldCreateWithDefaultValues() {
        Comment comment = new Comment();
        assertNotNull(comment);
        assertEquals(Comment.CommentStatus.BEKLIYOR, comment.getStatus());
        assertEquals(0L, comment.getLikeCount());
        assertNotNull(comment.getReplies());
        assertTrue(comment.getReplies().isEmpty());
        assertNull(comment.getContent());
        assertNull(comment.getStory());
        assertNull(comment.getUser());
        assertNull(comment.getParentComment());
    }

    @Test
    void comment_shouldSetAndGetAllFields() {
        Story story = new Story();
        User user = new User();
        Comment parent = new Comment();
        Set<Comment> replies = new HashSet<>();
        Comment comment = new Comment();
        
        comment.setContent("Comment content");
        comment.setStatus(Comment.CommentStatus.ONAYLANDI);
        comment.setStory(story);
        comment.setUser(user);
        comment.setParentComment(parent);
        comment.setReplies(replies);
        comment.setLikeCount(10L);
        
        assertEquals("Comment content", comment.getContent());
        assertEquals(Comment.CommentStatus.ONAYLANDI, comment.getStatus());
        assertEquals(story, comment.getStory());
        assertEquals(user, comment.getUser());
        assertEquals(parent, comment.getParentComment());
        assertEquals(replies, comment.getReplies());
        assertEquals(10L, comment.getLikeCount());
    }

    @Test
    void comment_shouldUseAllArgsConstructor() {
        Story story = new Story();
        User user = new User();
        Comment parent = new Comment();
        Set<Comment> replies = new HashSet<>();
        Comment comment = new Comment("Content", Comment.CommentStatus.REDDEDILDI, story, 
                user, parent, replies, 5L);
        
        assertEquals("Content", comment.getContent());
        assertEquals(Comment.CommentStatus.REDDEDILDI, comment.getStatus());
        assertEquals(story, comment.getStory());
        assertEquals(user, comment.getUser());
        assertEquals(parent, comment.getParentComment());
        assertEquals(replies, comment.getReplies());
        assertEquals(5L, comment.getLikeCount());
    }

    // ========== LIST ENTITY TESTS ==========

    @Test
    void listEntity_shouldCreateWithDefaultValues() {
        ListEntity list = new ListEntity();
        assertNotNull(list);
        assertFalse(list.getIsPrivate());
        assertNotNull(list.getStories());
        assertTrue(list.getStories().isEmpty());
        assertNull(list.getName());
        assertNull(list.getSlug());
        assertNull(list.getDescription());
        assertNull(list.getUser());
    }

    @Test
    void listEntity_shouldSetAndGetAllFields() {
        User user = new User();
        Set<Story> stories = new HashSet<>();
        ListEntity list = new ListEntity();
        
        list.setName("My List");
        list.setSlug("my-list");
        list.setDescription("List description");
        list.setIsPrivate(true);
        list.setUser(user);
        list.setStories(stories);
        
        assertEquals("My List", list.getName());
        assertEquals("my-list", list.getSlug());
        assertEquals("List description", list.getDescription());
        assertTrue(list.getIsPrivate());
        assertEquals(user, list.getUser());
        assertEquals(stories, list.getStories());
    }

    @Test
    void listEntity_shouldUseAllArgsConstructor() {
        User user = new User();
        Set<Story> stories = new HashSet<>();
        ListEntity list = new ListEntity("List Name", "list-name", "Description", false, 
                user, stories);
        
        assertEquals("List Name", list.getName());
        assertEquals("list-name", list.getSlug());
        assertEquals("Description", list.getDescription());
        assertFalse(list.getIsPrivate());
        assertEquals(user, list.getUser());
        assertEquals(stories, list.getStories());
    }

    // ========== NOTIFICATION ENTITY TESTS ==========

    @Test
    void notification_shouldCreateWithDefaultValues() {
        Notification notification = new Notification();
        assertNotNull(notification);
        assertFalse(notification.getIsRead());
        assertNull(notification.getUser());
        assertNull(notification.getTitle());
        assertNull(notification.getMessage());
        assertNull(notification.getType());
        assertNull(notification.getRelatedStoryId());
        assertNull(notification.getRelatedCommentId());
    }

    @Test
    void notification_shouldSetAndGetAllFields() {
        User user = new User();
        Notification notification = new Notification();
        
        notification.setUser(user);
        notification.setTitle("New Comment");
        notification.setMessage("You have a new comment");
        notification.setType(Notification.NotificationType.YENI_YORUM);
        notification.setIsRead(true);
        notification.setRelatedStoryId(1L);
        notification.setRelatedCommentId(2L);
        
        assertEquals(user, notification.getUser());
        assertEquals("New Comment", notification.getTitle());
        assertEquals("You have a new comment", notification.getMessage());
        assertEquals(Notification.NotificationType.YENI_YORUM, notification.getType());
        assertTrue(notification.getIsRead());
        assertEquals(1L, notification.getRelatedStoryId());
        assertEquals(2L, notification.getRelatedCommentId());
    }

    @Test
    void notification_shouldUseAllArgsConstructor() {
        User user = new User();
        Notification notification = new Notification(user, "Title", "Message", 
                Notification.NotificationType.HABER_BEGENILDI, true, 1L, 2L);
        
        assertEquals(user, notification.getUser());
        assertEquals("Title", notification.getTitle());
        assertEquals("Message", notification.getMessage());
        assertEquals(Notification.NotificationType.HABER_BEGENILDI, notification.getType());
        assertTrue(notification.getIsRead());
        assertEquals(1L, notification.getRelatedStoryId());
        assertEquals(2L, notification.getRelatedCommentId());
    }

    // ========== MEDIA FILE ENTITY TESTS ==========

    @Test
    void mediaFile_shouldCreateWithDefaultValues() {
        MediaFile file = new MediaFile();
        assertNotNull(file);
        assertNull(file.getFileName());
        assertNull(file.getOriginalFileName());
        assertNull(file.getFilePath());
        assertNull(file.getMimeType());
        assertNull(file.getFileSize());
        assertNull(file.getUploaderUserId());
        assertNull(file.getFileType());
    }

    @Test
    void mediaFile_shouldSetAndGetAllFields() {
        MediaFile file = new MediaFile();
        
        file.setFileName("file.jpg");
        file.setOriginalFileName("original.jpg");
        file.setFilePath("/path/to/file.jpg");
        file.setMimeType("image/jpeg");
        file.setFileSize(1024L);
        file.setUploaderUserId(1L);
        file.setFileType(MediaFile.FileType.RESIM);
        
        assertEquals("file.jpg", file.getFileName());
        assertEquals("original.jpg", file.getOriginalFileName());
        assertEquals("/path/to/file.jpg", file.getFilePath());
        assertEquals("image/jpeg", file.getMimeType());
        assertEquals(1024L, file.getFileSize());
        assertEquals(1L, file.getUploaderUserId());
        assertEquals(MediaFile.FileType.RESIM, file.getFileType());
    }

    @Test
    void mediaFile_shouldUseAllArgsConstructor() {
        MediaFile file = new MediaFile("file.pdf", "original.pdf", "/path/to/file.pdf", 
                "application/pdf", 2048L, 2L, MediaFile.FileType.DOKUMAN);
        
        assertEquals("file.pdf", file.getFileName());
        assertEquals("original.pdf", file.getOriginalFileName());
        assertEquals("/path/to/file.pdf", file.getFilePath());
        assertEquals("application/pdf", file.getMimeType());
        assertEquals(2048L, file.getFileSize());
        assertEquals(2L, file.getUploaderUserId());
        assertEquals(MediaFile.FileType.DOKUMAN, file.getFileType());
    }

    // ========== FOLLOW ENTITY TESTS ==========

    @Test
    void follow_shouldCreateWithDefaultValues() {
        Follow follow = new Follow();
        assertNotNull(follow);
        assertNull(follow.getFollower());
        assertNull(follow.getFollowed());
    }

    @Test
    void follow_shouldSetAndGetAllFields() {
        User follower = new User();
        User followed = new User();
        Follow follow = new Follow();
        
        follow.setFollower(follower);
        follow.setFollowed(followed);
        
        assertEquals(follower, follow.getFollower());
        assertEquals(followed, follow.getFollowed());
    }

    @Test
    void follow_shouldUseAllArgsConstructor() {
        User follower = new User();
        User followed = new User();
        Follow follow = new Follow(follower, followed);
        
        assertEquals(follower, follow.getFollower());
        assertEquals(followed, follow.getFollowed());
    }

    // ========== LIKE ENTITY TESTS ==========

    @Test
    void like_shouldCreateWithDefaultValues() {
        Like like = new Like();
        assertNotNull(like);
        assertNull(like.getUser());
        assertNull(like.getStory());
    }

    @Test
    void like_shouldSetAndGetAllFields() {
        User user = new User();
        Story story = new Story();
        Like like = new Like();
        
        like.setUser(user);
        like.setStory(story);
        
        assertEquals(user, like.getUser());
        assertEquals(story, like.getStory());
    }

    @Test
    void like_shouldUseAllArgsConstructor() {
        User user = new User();
        Story story = new Story();
        Like like = new Like(user, story);
        
        assertEquals(user, like.getUser());
        assertEquals(story, like.getStory());
    }

    // ========== SAVED STORY ENTITY TESTS ==========

    @Test
    void savedStory_shouldCreateWithDefaultValues() {
        SavedStory savedStory = new SavedStory();
        assertNotNull(savedStory);
        assertNull(savedStory.getUser());
        assertNull(savedStory.getStory());
    }

    @Test
    void savedStory_shouldSetAndGetAllFields() {
        User user = new User();
        Story story = new Story();
        SavedStory savedStory = new SavedStory();
        
        savedStory.setUser(user);
        savedStory.setStory(story);
        
        assertEquals(user, savedStory.getUser());
        assertEquals(story, savedStory.getStory());
    }

    @Test
    void savedStory_shouldUseAllArgsConstructor() {
        User user = new User();
        Story story = new Story();
        SavedStory savedStory = new SavedStory(user, story);
        
        assertEquals(user, savedStory.getUser());
        assertEquals(story, savedStory.getStory());
    }

    // ========== HELPER METHODS ==========

    private <T> boolean contains(T[] array, T value) {
        for (T item : array) {
            if (item.equals(value)) {
                return true;
            }
        }
        return false;
    }
}

