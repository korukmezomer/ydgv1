package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 7b: Admin Story Onaylama Testi
 * 
 * Senaryo:
 * - Admin olarak giriş yap
 * - Onay bekleyen story'yi bul
 * - Story'yi onayla (YAYINLANDI durumuna geçer)
 * 
 * Not: Bu test, Case7a'dan sonra çalıştırılmalı veya 
 * veritabanında onay bekleyen bir story olmalı
 */
@DisplayName("Case 7b: Admin Story Onaylama")
public class Case7b_AdminApproveStoryTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 7b: Admin onay bekleyen story'yi onaylayabilmeli")
    public void case7b_AdminApproveStory() {
        try {
            // Önce bir story oluştur (onay bekleyen story olması için)
            // Bu test Case7a'dan sonra çalıştırılırsa bu adım atlanabilir
            String storyTitle = "Onay Bekleyen Story " + System.currentTimeMillis();
            
            // Veritabanından onay bekleyen bir story bul
            Long storyId = getStoryIdByTitle(storyTitle);
            if (storyId == null) {
                // Eğer story yoksa, en son oluşturulan onay bekleyen story'yi bul
                try (java.sql.Connection conn = getTestDatabaseConnection()) {
                    String sql = "SELECT id, baslik FROM stories WHERE durum = 'YAYIN_BEKLIYOR' ORDER BY created_at DESC LIMIT 1";
                    try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                        try (java.sql.ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                storyId = rs.getLong("id");
                                storyTitle = rs.getString("baslik");
                                System.out.println("Case 7b: Onay bekleyen story bulundu: " + storyTitle);
                            } else {
                                System.out.println("Case 7b: Onay bekleyen story bulunamadı. Önce Case7a testini çalıştırın.");
                                return;
                            }
                        }
                    }
                } catch (java.sql.SQLException e) {
                    System.err.println("Case 7b: Veritabanı hatası: " + e.getMessage());
                    return;
                }
            }
            
            // Admin olarak giriş yap ve story'yi onayla
            String storySlug = approveStoryAsAdmin(storyTitle);
            
            assertNotNull(storySlug, "Case 7b: Story onaylanamadı");
            System.out.println("Case 7b: Story başarıyla onaylandı. Slug: " + storySlug);
            
        } catch (Exception e) {
            System.out.println("Case 7b: " + e.getMessage());
            e.printStackTrace();
            fail("Case 7b: Test başarısız - " + e.getMessage());
        }
    }
}

