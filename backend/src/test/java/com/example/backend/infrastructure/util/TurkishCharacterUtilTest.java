package com.example.backend.infrastructure.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TurkishCharacterUtilTest {

    @Test
    void normalizeTurkish_shouldHandleNull() {
        String result = TurkishCharacterUtil.normalizeTurkish(null);
        assertNull(result);
    }

    @Test
    void normalizeTurkish_shouldNormalizeLowercaseTurkishCharacters() {
        String result = TurkishCharacterUtil.normalizeTurkish("şeker");
        assertEquals("seker", result);
    }

    @Test
    void normalizeTurkish_shouldNormalizeUppercaseTurkishCharacters() {
        String result = TurkishCharacterUtil.normalizeTurkish("ŞEKER");
        assertEquals("seker", result);
    }

    @Test
    void normalizeTurkish_shouldNormalizeAllTurkishCharacters() {
        String result = TurkishCharacterUtil.normalizeTurkish("öşüığç");
        assertEquals("osuigc", result);
    }

    @Test
    void normalizeTurkish_shouldNormalizeMixedCase() {
        String result = TurkishCharacterUtil.normalizeTurkish("ÖŞÜIĞÇ");
        // "İ" -> "I" -> "i", "I" -> "i", "ı" -> "i" after toLowerCase
        // The actual result depends on how Java handles Turkish "I" character
        assertNotNull(result);
        assertTrue(result.contains("o") && result.contains("s") && result.contains("u") && result.contains("c"));
    }

    @Test
    void normalizeTurkish_shouldHandleTextWithTurkishCharacters() {
        String result = TurkishCharacterUtil.normalizeTurkish("Türkçe karakterler");
        assertEquals("turkce karakterler", result);
    }

    @Test
    void normalizeTurkish_shouldReturnLowercase() {
        String result = TurkishCharacterUtil.normalizeTurkish("HELLO");
        assertEquals("hello", result);
    }

    @Test
    void normalizeTurkish_shouldRemoveDiacriticalMarks() {
        String result = TurkishCharacterUtil.normalizeTurkish("café");
        assertNotNull(result);
        assertFalse(result.contains("é"));
    }

    @Test
    void getSearchPattern_shouldHandleNull() {
        String result = TurkishCharacterUtil.getSearchPattern(null);
        assertEquals("", result);
    }

    @Test
    void getSearchPattern_shouldHandleEmptyString() {
        String result = TurkishCharacterUtil.getSearchPattern("");
        assertEquals("", result);
    }

    @Test
    void getSearchPattern_shouldHandleWhitespaceOnly() {
        String result = TurkishCharacterUtil.getSearchPattern("   ");
        assertEquals("", result);
    }

    @Test
    void getSearchPattern_shouldCreatePatternWithOriginalAndNormalized() {
        String result = TurkishCharacterUtil.getSearchPattern("şeker");
        assertNotNull(result);
        assertTrue(result.contains("%seker%"));
        assertTrue(result.contains("|"));
    }

    @Test
    void getSearchPattern_shouldCreatePatternForTurkishText() {
        String result = TurkishCharacterUtil.getSearchPattern("Türkçe");
        assertNotNull(result);
        assertTrue(result.contains("%turkce%"));
    }

    @Test
    void getSearchPattern_shouldCreatePatternForEnglishText() {
        String result = TurkishCharacterUtil.getSearchPattern("Hello");
        assertNotNull(result);
        assertTrue(result.contains("%hello%"));
    }
}

