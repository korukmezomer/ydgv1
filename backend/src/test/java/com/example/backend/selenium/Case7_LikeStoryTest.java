package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 7: Story Beğenme (Like Story)
 * 
 * Use Case: Kullanıcı bir story'yi beğenebilmeli
 * Senaryo:
 * - Onaylanmış bir story olmalı (Case7a ve Case7b çalıştırılmış olmalı)
 * - Kullanıcı giriş yapar
 * - Story sayfasına gider
 * - Beğeni butonuna tıklar
 * - Beğeninin eklendiğini doğrula
 * 
 * Not: Bu test Case7a ve Case7b'den sonra çalıştırılmalı
 */
@DisplayName("Case 7: Story Beğenme")
public class Case7_LikeStoryTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 7: Kullanıcı story'yi beğenebilmeli")
    public void case7_LikeStory() {
        try {
            // Önce onaylanmış bir story bul (Case7a ve Case7b'den sonra)
            String storySlug = null;
            String storyTitle = null;
            
            try (java.sql.Connection conn = getTestDatabaseConnection()) {
                String sql = "SELECT slug, baslik FROM stories WHERE durum = 'YAYINLANDI' ORDER BY yayinlanma_tarihi DESC LIMIT 1";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            storySlug = rs.getString("slug");
                            storyTitle = rs.getString("baslik");
                            System.out.println("Case 7: Onaylanmış story bulundu: " + storyTitle);
                        }
                    }
                }
            } catch (java.sql.SQLException e) {
                System.err.println("Case 7: Veritabanı hatası: " + e.getMessage());
                fail("Case 7: Veritabanı hatası - " + e.getMessage());
                return;
            }
            
            if (storySlug == null) {
                fail("Case 7: Onaylanmış story bulunamadı. Önce Case7a ve Case7b testlerini çalıştırın.");
                return;
            }
            
            // Kullanıcı (Liker) oluştur
            java.util.Random random = new java.util.Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String likerEmail = "liker" + randomSuffix + "@example.com";
            String likerPassword = "Test123456";
            
            try {
                driver.get(BASE_URL + "/logout");
                Thread.sleep(2000);
            } catch (Exception e) {
                // Logout sayfası yoksa devam et
            }
            
            driver.get(BASE_URL + "/register");
            waitForPageLoad();
            
            WebElement firstNameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
            );
            firstNameInput.sendKeys("Liker");
            driver.findElement(By.id("lastName")).sendKeys("Test");
            driver.findElement(By.id("email")).sendKeys(likerEmail);
            driver.findElement(By.id("username")).sendKeys("liker" + randomSuffix);
            driver.findElement(By.id("password")).sendKeys(likerPassword);
            
            WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
            );
            submitButton.click();
            
            Thread.sleep(3000);
            
            // Kayıt sonrası zaten dashboard'a yönlendirilmiş, direkt story sayfasına gidebiliriz
            // Story ID'sini al
            Long storyId = getStoryIdFromSlug(storySlug);
            if (storyId == null) {
                fail("Case 7: Story ID alınamadı");
                return;
            }
            
            // Kullanıcı ID'sini al
            Long userId = getUserIdByEmail(likerEmail);
            if (userId == null) {
                fail("Case 7: Kullanıcı ID alınamadı");
                return;
            }
            
            // Story sayfasına git (kullanıcı zaten giriş yapmış durumda)
            driver.get(BASE_URL + "/haberler/" + storySlug);
            waitForPageLoad();
            Thread.sleep(3000); // Sayfanın tam yüklenmesi için bekle
            
            // Beğeni butonunu bul ve tıkla (Medium temasında action-btn class'ı ve title="Beğen" kullanılıyor)
            WebElement likeButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@title='Beğen' or contains(@class, 'action-btn')] | //button[contains(@class, 'action-btn') and .//svg]")
                )
            );
            likeButton.click();
            Thread.sleep(3000); // Beğeninin kaydedilmesi için bekle
            
            // Beğeninin veritabanına kaydedildiğini doğrula
            boolean likeExists = false;
            try (java.sql.Connection conn = getTestDatabaseConnection()) {
                String sql = "SELECT COUNT(*) FROM likes WHERE kullanici_id = ? AND story_id = ? AND is_active = true";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, userId);
                    stmt.setLong(2, storyId);
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            likeExists = rs.getInt(1) > 0;
                        }
                    }
                }
            } catch (java.sql.SQLException e) {
                System.err.println("Case 7: Beğeni kontrolü hatası: " + e.getMessage());
            }
            
            assertTrue(likeExists, "Case 7: Beğeni veritabanına kaydedilmedi. User ID: " + userId + ", Story ID: " + storyId);
            System.out.println("Case 7: Beğeni işlemi başarıyla tamamlandı ve veritabanına kaydedildi");
            
        } catch (AssertionError e) {
            // Assertion hataları zaten fail ediyor
            throw e;
        } catch (Exception e) {
            System.err.println("Case 7: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 7: Test başarısız - " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Case 7 Negative: Beğeni toggle işlevi - beğenilmiş story tekrar tıklanınca beğeni kaldırılmalı")
    public void case7_Negative_ToggleLike() {
        try {
            // Önce onaylanmış bir story bul (Case7a ve Case7b'den sonra)
            String storySlug = null;
            String storyTitle = null;
            
            try (java.sql.Connection conn = getTestDatabaseConnection()) {
                String sql = "SELECT slug, baslik FROM stories WHERE durum = 'YAYINLANDI' ORDER BY yayinlanma_tarihi DESC LIMIT 1";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            storySlug = rs.getString("slug");
                            storyTitle = rs.getString("baslik");
                            System.out.println("Case 7 Negative: Onaylanmış story bulundu: " + storyTitle);
                        }
                    }
                }
            } catch (java.sql.SQLException e) {
                System.err.println("Case 7 Negative: Veritabanı hatası: " + e.getMessage());
                fail("Case 7 Negative: Veritabanı hatası - " + e.getMessage());
                return;
            }
            
            if (storySlug == null) {
                fail("Case 7 Negative: Onaylanmış story bulunamadı. Önce Case7a ve Case7b testlerini çalıştırın.");
                return;
            }
            
            // Kullanıcı (Liker) oluştur
            java.util.Random random = new java.util.Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String likerEmail = "liker_neg" + randomSuffix + "@example.com";
            String likerPassword = "Test123456";
            
            try {
                driver.get(BASE_URL + "/logout");
                Thread.sleep(2000);
            } catch (Exception e) {
                // Logout sayfası yoksa devam et
            }
            
            driver.get(BASE_URL + "/register");
            waitForPageLoad();
            
            WebElement firstNameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
            );
            firstNameInput.sendKeys("Liker");
            driver.findElement(By.id("lastName")).sendKeys("Test");
            driver.findElement(By.id("email")).sendKeys(likerEmail);
            driver.findElement(By.id("username")).sendKeys("liker_neg" + randomSuffix);
            driver.findElement(By.id("password")).sendKeys(likerPassword);
            
            WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
            );
            submitButton.click();
            Thread.sleep(3000);
            
            // Story ID'sini al
            Long storyId = getStoryIdFromSlug(storySlug);
            if (storyId == null) {
                fail("Case 7 Negative: Story ID alınamadı");
                return;
            }
            
            // Kullanıcı ID'sini al
            Long userId = getUserIdByEmail(likerEmail);
            if (userId == null) {
                fail("Case 7 Negative: Kullanıcı ID alınamadı");
                return;
            }
            
            // Story sayfasına git (kullanıcı zaten giriş yapmış durumda)
            driver.get(BASE_URL + "/haberler/" + storySlug);
            waitForPageLoad();
            Thread.sleep(3000); // Sayfanın tam yüklenmesi için bekle
            
            // İlk beğeni
            WebElement likeButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@title='Beğen' or contains(@class, 'action-btn')] | //button[contains(@class, 'action-btn') and .//svg]")
                )
            );
            
            // İlk tıklamadan önce butonun active olmadığını kontrol et
            String buttonClassBefore = likeButton.getAttribute("class");
            assertTrue(buttonClassBefore == null || !buttonClassBefore.contains("active"),
                "Case 7 Negative: İlk beğeniden önce buton active olmamalı");
            
            likeButton.click();
            Thread.sleep(3000); // Beğeninin kaydedilmesi için bekle
            
            // Beğeninin veritabanına kaydedildiğini doğrula
            boolean likeExists = false;
            try (java.sql.Connection conn = getTestDatabaseConnection()) {
                String sql = "SELECT COUNT(*) FROM likes WHERE kullanici_id = ? AND story_id = ? AND is_active = true";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, userId);
                    stmt.setLong(2, storyId);
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            likeExists = rs.getInt(1) > 0;
                        }
                    }
                }
            } catch (java.sql.SQLException e) {
                System.err.println("Case 7 Negative: Beğeni kontrolü hatası: " + e.getMessage());
            }
            
            assertTrue(likeExists, "Case 7 Negative: İlk beğeni veritabanına kaydedilmedi");
            
            // Buton artık active olmalı
            WebElement likeButtonAfter = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[@title='Beğen' or contains(@class, 'action-btn')] | //button[contains(@class, 'action-btn') and .//svg]")
                )
            );
            String buttonClassAfter = likeButtonAfter.getAttribute("class");
            assertTrue(buttonClassAfter != null && buttonClassAfter.contains("active"),
                "Case 7 Negative: Beğenildikten sonra buton active olmalı");
            
            // Tekrar tıkla - beğeni kaldırılmalı (toggle)
            likeButtonAfter.click();
            Thread.sleep(3000); // Beğeninin kaldırılması için bekle
            
            // Beğeninin veritabanından kaldırıldığını doğrula
            boolean likeStillExists = false;
            try (java.sql.Connection conn = getTestDatabaseConnection()) {
                String sql = "SELECT COUNT(*) FROM likes WHERE kullanici_id = ? AND story_id = ? AND is_active = true";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, userId);
                    stmt.setLong(2, storyId);
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            likeStillExists = rs.getInt(1) > 0;
                        }
                    }
                }
            } catch (java.sql.SQLException e) {
                System.err.println("Case 7 Negative: Beğeni kontrolü hatası: " + e.getMessage());
            }
            
            assertTrue(!likeStillExists, "Case 7 Negative: İkinci tıklamadan sonra beğeni veritabanından kaldırılmalı");
            
            // Buton artık active olmamalı
            WebElement likeButtonFinal = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[@title='Beğen' or contains(@class, 'action-btn')] | //button[contains(@class, 'action-btn') and .//svg]")
                )
            );
            String buttonClassFinal = likeButtonFinal.getAttribute("class");
            assertTrue(buttonClassFinal == null || !buttonClassFinal.contains("active"),
                "Case 7 Negative: Beğeni kaldırıldıktan sonra buton active olmamalı");
            
            System.out.println("Case 7 Negative: Beğeni toggle işlevi başarıyla test edildi");
            
        } catch (AssertionError e) {
            // Assertion hataları zaten fail ediyor
            throw e;
        } catch (Exception e) {
            System.err.println("Case 7 Negative: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 7 Negative: Test başarısız - " + e.getMessage());
        }
    }
}

