package com.example.backend.infrastructure.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class TurkishCharacterUtil {
    
    /**
     * Türkçe karakterleri İngilizce karşılıklarına çevirir
     * ö -> o, ş -> s, ı -> i, ü -> u, ğ -> g, ç -> c
     */
    public static String normalizeTurkish(String text) {
        if (text == null) {
            return null;
        }
        
        // Unicode normalizasyonu
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        
        // Türkçe karakterleri değiştir
        normalized = normalized.replaceAll("ö", "o")
                              .replaceAll("Ö", "O")
                              .replaceAll("ş", "s")
                              .replaceAll("Ş", "S")
                              .replaceAll("ı", "i")
                              .replaceAll("İ", "I")
                              .replaceAll("ü", "u")
                              .replaceAll("Ü", "U")
                              .replaceAll("ğ", "g")
                              .replaceAll("Ğ", "G")
                              .replaceAll("ç", "c")
                              .replaceAll("Ç", "C");
        
        // Diğer özel karakterleri kaldır
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        normalized = pattern.matcher(normalized).replaceAll("");
        
        return normalized.toLowerCase();
    }
    
    /**
     * Arama için normalize edilmiş string döndürür
     * Hem orijinal hem normalize edilmiş versiyonu içerir
     */
    public static String getSearchPattern(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        String normalized = normalizeTurkish(text);
        String original = text.toLowerCase();
        
        // Hem orijinal hem normalize edilmiş karakterleri içeren pattern
        return "%" + original + "%|%" + normalized + "%";
    }
}

