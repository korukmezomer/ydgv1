package com.example.backend.infrastructure.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SlugUtilTest {

    @Test
    void toSlug_shouldHandleNull() {
        String result = SlugUtil.toSlug(null);
        assertEquals("", result);
    }

    @Test
    void toSlug_shouldHandleEmptyString() {
        String result = SlugUtil.toSlug("");
        assertEquals("", result);
    }

    @Test
    void toSlug_shouldConvertToLowercase() {
        String result = SlugUtil.toSlug("HELLO WORLD");
        assertEquals("hello-world", result);
    }

    @Test
    void toSlug_shouldReplaceSpacesWithDashes() {
        String result = SlugUtil.toSlug("hello world");
        assertEquals("hello-world", result);
    }

    @Test
    void toSlug_shouldRemoveNonLatinCharacters() {
        String result = SlugUtil.toSlug("hello@world#test");
        assertEquals("helloworldtest", result);
    }

    @Test
    void toSlug_shouldRemoveLeadingAndTrailingDashes() {
        String result = SlugUtil.toSlug("-hello-world-");
        assertEquals("hello-world", result);
    }

    @Test
    void toSlug_shouldHandleMultipleSpaces() {
        String result = SlugUtil.toSlug("hello    world");
        assertEquals("hello----world", result);
    }

    @Test
    void toSlug_shouldHandleSpecialCharacters() {
        String result = SlugUtil.toSlug("hello!@#$%^&*()world");
        assertEquals("helloworld", result);
    }

    @Test
    void toSlug_shouldNormalizeUnicode() {
        String result = SlugUtil.toSlug("café");
        assertNotNull(result);
        assertFalse(result.contains("é"));
    }

    @Test
    void toSlug_shouldHandleTurkishCharacters() {
        String result = SlugUtil.toSlug("şeker çay");
        assertNotNull(result);
    }
}

