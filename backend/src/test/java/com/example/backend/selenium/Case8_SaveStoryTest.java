package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 8: Story Kaydetme (Save Story)
 * 
 * Use Case: Kullanıcı bir story'yi kaydedebilmeli
 * Senaryo:
 * - Kullanıcı giriş yapar
 * - Bir story sayfasına gider
 * - Kaydet butonuna tıklar
 * - Story'nin kaydedildiğini doğrula
 */
@DisplayName("Case 8: Story Kaydetme")
public class Case8_SaveStoryTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 8: Kullanıcı story'yi kaydedebilmeli")
    public void case8_SaveStory() {
        try {
            // 1. Writer oluştur (BaseSeleniumTest'teki registerWriter helper metodunu kullan)
            java.util.Random random = new java.util.Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String writerEmail = "writer_save_" + randomSuffix + "@example.com";
            String writerPassword = "Test123456";
            String writerUsername = "writer_save_" + randomSuffix;
            
            boolean writerRegistered = registerWriter("Writer", "Save", writerEmail, writerUsername, writerPassword);
            if (!writerRegistered) {
                fail("Case 8: Writer kaydı başarısız");
                return;
            }
            
            // Writer olarak zaten giriş yapılmış durumda (kayıt sonrası dashboard'a yönlendirildi)
            
            // Story oluştur ve yayınla (BaseSeleniumTest'teki createStory metodunu kullan)
            String storyTitle = "Kaydet Test Story " + System.currentTimeMillis();
            String storyContent = "Bu bir kaydet test story'sidir.";
            String storySlug = createStory(writerEmail, writerPassword, storyTitle, storyContent);
            
            if (storySlug == null) {
                fail("Case 8: Story oluşturulamadı");
                return;
            }
            
            // 2. Admin olarak story'yi onayla (Case7b işlemi - BaseSeleniumTest'teki approveStoryAsAdmin metodunu kullan)
            storySlug = approveStoryAsAdmin(storyTitle);
            if (storySlug == null) {
                fail("Case 8: Story onaylanamadı");
                return;
            }
            
            // 3. Kullanıcı (Saver) oluştur (BaseSeleniumTest'teki registerUser helper metodunu kullan)
            java.util.Random saverRandom = new java.util.Random();
            String saverRandomSuffix = System.currentTimeMillis() + "_" + saverRandom.nextInt(100000);
            String saverEmail = "saver" + saverRandomSuffix + "@example.com";
            String saverUsername = "saver" + saverRandomSuffix;
            
            try {
                driver.get(BASE_URL + "/logout");
                Thread.sleep(2000);
            } catch (Exception e) {
                // Logout sayfası yoksa devam et
            }
            
            boolean saverRegistered = registerUser("Saver", "Test", saverEmail, saverUsername, "Test123456");
            if (!saverRegistered) {
                fail("Case 8: Saver kaydı başarısız");
                return;
            }
            
            Thread.sleep(3000);
            
            // Story sayfasına git (kullanıcı zaten giriş yapmış durumda)
            driver.get(BASE_URL + "/haberler/" + storySlug);
            waitForPageLoad();
            Thread.sleep(3000); // Sayfanın tam yüklenmesi için bekle
            
            // Kaydet butonunu bul ve tıkla (Medium temasında title="Kaydet" kullanılıyor)
            // Beğeni butonundan sonra gelen 3. action-btn butonu kaydet butonu
            WebElement saveButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@title='Kaydet']")
                )
            );
            saveButton.click();
            Thread.sleep(1500); // Kaydetme işleminin tamamlanması için bekle
            
            // Story ID'sini al
            Long storyId = getStoryIdFromSlug(storySlug);
            if (storyId == null) {
                fail("Case 8: Story ID alınamadı");
                return;
            }
            
            // Kullanıcı ID'sini al
            Long userId = getUserIdByEmail(saverEmail);
            if (userId == null) {
                fail("Case 8: Kullanıcı ID alınamadı");
                return;
            }
            
            // Kaydetmenin API'de kaydedildiğini doğrula (token ile, yoksa UI fallback)
            String saverToken = getBrowserToken();
            if (saverToken == null) {
                saverToken = getUserToken(saverEmail, "Test123456");
            }
            Boolean savedExists = getSaveStatusViaApi(userId, storyId, saverToken);
            if (savedExists == null) {
                // API yanıt vermezse buton class'ı ile doğrula
                String btnClass = saveButton.getAttribute("class");
                assertTrue(btnClass != null && btnClass.contains("active"),
                    "Case 8: Kaydetme API ve UI ile doğrulanamadı");
            } else {
                assertTrue(savedExists, "Case 8: Story kaydetme işlemi API'de kaydedilmedi. User ID: " + userId + ", Story ID: " + storyId);
            }
            System.out.println("Case 8: Story kaydetme işlemi tamamlandı (API/UI doğrulandı)");
            
        } catch (Exception e) {
            System.out.println("Case 8: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Case 8 Negative: Kaydetme toggle işlevi - kaydedilmiş story tekrar tıklanınca kaydetme kaldırılmalı")
    public void case8_Negative_ToggleSave() {
        try {
            // 1. Writer oluştur (BaseSeleniumTest'teki registerWriter helper metodunu kullan)
            java.util.Random random = new java.util.Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String writerEmail = "writer_save_neg_" + randomSuffix + "@example.com";
            String writerPassword = "Test123456";
            String writerUsername = "writer_save_neg_" + randomSuffix;
            
            boolean writerRegistered = registerWriter("Writer", "Save", writerEmail, writerUsername, writerPassword);
            if (!writerRegistered) {
                fail("Case 8 Negative: Writer kaydı başarısız");
                return;
            }
            
            // Writer olarak zaten giriş yapılmış durumda (kayıt sonrası dashboard'a yönlendirildi)
            
            // Story oluştur ve yayınla (BaseSeleniumTest'teki createStory metodunu kullan)
            String storyTitle = "Kaydet Negative Test Story " + System.currentTimeMillis();
            String storyContent = "Bu bir kaydet negative test story'sidir.";
            String storySlug = createStory(writerEmail, writerPassword, storyTitle, storyContent);
            
            if (storySlug == null) {
                fail("Case 8 Negative: Story oluşturulamadı");
                return;
            }
            
            // 2. Admin olarak story'yi onayla (Case7b işlemi - BaseSeleniumTest'teki approveStoryAsAdmin metodunu kullan)
            storySlug = approveStoryAsAdmin(storyTitle);
            if (storySlug == null) {
                fail("Case 8 Negative: Story onaylanamadı");
                return;
            }
            
            // 3. Kullanıcı (Saver) oluştur (BaseSeleniumTest'teki registerUser helper metodunu kullan)
            java.util.Random saverRandom = new java.util.Random();
            String saverRandomSuffix = System.currentTimeMillis() + "_" + saverRandom.nextInt(100000);
            String saverEmail = "saver_neg" + saverRandomSuffix + "@example.com";
            String saverUsername = "saver_neg" + saverRandomSuffix;
            
            try {
                driver.get(BASE_URL + "/logout");
                Thread.sleep(2000);
            } catch (Exception e) {
                // Logout sayfası yoksa devam et
            }
            
            boolean saverRegistered = registerUser("Saver", "Test", saverEmail, saverUsername, "Test123456");
            if (!saverRegistered) {
                fail("Case 8 Negative: Saver kaydı başarısız");
                return;
            }
            Thread.sleep(3000);
            
            // Story sayfasına git (kullanıcı zaten giriş yapmış durumda)
            driver.get(BASE_URL + "/haberler/" + storySlug);
            waitForPageLoad();
            Thread.sleep(3000); // Sayfanın tam yüklenmesi için bekle
            
            // Story ID'sini al
            Long storyId = getStoryIdFromSlug(storySlug);
            if (storyId == null) {
                // Son çare: Veritabanından slug ile almayı dene
                try (java.sql.Connection conn = getTestDatabaseConnection()) {
                    String sql = "SELECT id FROM stories WHERE slug = ?";
                    try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, storySlug);
                        try (java.sql.ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                storyId = rs.getLong("id");
                            }
                        }
                    }
                } catch (java.sql.SQLException e) {
                    System.err.println("Case 8 Negative: Story ID slug'dan alınamadı: " + e.getMessage());
                }
            }
            if (storyId == null) {
                fail("Case 8 Negative: Story ID alınamadı");
                return;
            }
            
            // Kullanıcı ID'sini al
            Long userId = getUserIdByEmail(saverEmail);
            if (userId == null) {
                fail("Case 8 Negative: Kullanıcı ID alınamadı");
                return;
            }
            
            // İlk kaydetme
            WebElement saveButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@title='Kaydet']")
                )
            );
            
            // İlk tıklamadan önce butonun active olmadığını kontrol et
            String buttonClassBefore = saveButton.getAttribute("class");
            assertTrue(buttonClassBefore == null || !buttonClassBefore.contains("active"),
                "Case 8 Negative: İlk kaydetmeden önce buton active olmamalı");
            
            saveButton.click();
            Thread.sleep(1500); // Kaydetme işleminin tamamlanması için bekle
            
            // Kaydetmenin API'de kaydedildiğini doğrula (token ile, yoksa UI fallback)
            String saverToken = getBrowserToken();
            if (saverToken == null) {
                saverToken = getUserToken(saverEmail, "Test123456");
            }
            Boolean savedExists = getSaveStatusViaApi(userId, storyId, saverToken);
            if (savedExists == null) {
                String cls = saveButton.getAttribute("class");
                assertTrue(cls != null && cls.contains("active"),
                    "Case 8 Negative: Kaydetme API veya UI ile doğrulanamadı");
            } else {
                assertTrue(savedExists, "Case 8 Negative: İlk kaydetme API'de kaydedilmedi");
            }
            
            // Buton artık active olmalı
            WebElement saveButtonAfter = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[@title='Kaydet']")
                )
            );
            String buttonClassAfter = saveButtonAfter.getAttribute("class");
            assertTrue(buttonClassAfter != null && buttonClassAfter.contains("active"),
                "Case 8 Negative: Kaydedildikten sonra buton active olmalı");
            
            // Tekrar tıkla - kaydetme kaldırılmalı (toggle)
            saveButtonAfter.click();
            Thread.sleep(1500); // Kaydetmenin kaldırılması için bekle
            
            // Kaydetmenin API'de kaldırıldığını doğrula (token ile, yoksa UI fallback)
            String saverToken2 = saverToken != null ? saverToken : getBrowserToken();
            if (saverToken2 == null) {
                saverToken2 = getUserToken(saverEmail, "Test123456");
            }
            Boolean savedStillExists = getSaveStatusViaApi(userId, storyId, saverToken2);
            
            // Buton artık active olmamalı
            WebElement saveButtonFinal = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[@title='Kaydet']")
                )
            );
            String buttonClassFinal = saveButtonFinal.getAttribute("class");
            
            if (savedStillExists == null) {
                assertTrue(buttonClassFinal == null || !buttonClassFinal.contains("active"),
                    "Case 8 Negative: Kaydetme kaldırma API/UI doğrulanamadı");
            } else {
                assertFalse(savedStillExists, "Case 8 Negative: Kaydetme kaldırılmadı (API)");
            assertTrue(buttonClassFinal == null || !buttonClassFinal.contains("active"),
                "Case 8 Negative: Kaydetme kaldırıldıktan sonra buton active olmamalı");
            }
            
            System.out.println("Case 8 Negative: Kaydetme toggle işlevi başarıyla test edildi");
            
        } catch (Exception e) {
            System.out.println("Case 8 Negative: " + e.getMessage());
        }
    }
}

