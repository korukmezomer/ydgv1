package com.example.backend.domain.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

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
        Comment.CommentStatus status = Comment.CommentStatus.valueOf("BEKLIYOR");
        assertEquals(Comment.CommentStatus.BEKLIYOR, status);
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
    void baseEntity_shouldHaveDefaultValues() {
        // Test that BaseEntity fields are accessible
        User user = new User();
        assertNotNull(user);
        assertTrue(user.getIsActive()); // Default value from BaseEntity
    }

    @Test
    void user_shouldCreateWithDefaultValues() {
        User user = new User();
        assertNotNull(user);
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().isEmpty());
    }

    @Test
    void story_shouldCreateWithDefaultValues() {
        Story story = new Story();
        assertNotNull(story);
        assertEquals(Story.StoryStatus.TASLAK, story.getStatus());
        assertEquals(0L, story.getViewCount());
        assertEquals(0L, story.getLikeCount());
        assertEquals(0L, story.getCommentCount());
    }

    @Test
    void comment_shouldCreateWithDefaultValues() {
        Comment comment = new Comment();
        assertNotNull(comment);
        assertEquals(Comment.CommentStatus.BEKLIYOR, comment.getStatus());
        assertEquals(0L, comment.getLikeCount());
        assertNotNull(comment.getReplies());
        assertTrue(comment.getReplies().isEmpty());
    }

    @Test
    void category_shouldCreateWithDefaultValues() {
        Category category = new Category();
        assertNotNull(category);
        assertNotNull(category.getStories());
        assertTrue(category.getStories().isEmpty());
    }

    @Test
    void tag_shouldCreateWithDefaultValues() {
        Tag tag = new Tag();
        assertNotNull(tag);
        assertNotNull(tag.getStories());
        assertTrue(tag.getStories().isEmpty());
    }

    private <T> boolean contains(T[] array, T value) {
        for (T item : array) {
            if (item.equals(value)) {
                return true;
            }
        }
        return false;
    }
}

