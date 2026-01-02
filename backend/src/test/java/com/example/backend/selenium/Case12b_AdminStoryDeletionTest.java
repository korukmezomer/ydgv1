package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 12b: Admin Story Reddetme (Admin Story Rejection)
 * 
 * Test Senaryoları:
 * 1. Writer story oluşturur ve yayınlar (onay bekleyen durumda)
 * 2. Admin story'yi bulur ve reddeder
 * 3. Reddedilen story'nin listeden çıktığını doğrular
 */
@DisplayName("Case 12b: Admin Story Reddetme")
public class Case12b_AdminStoryDeletionTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 12b: Admin story'yi reddedebilmeli")
    public void case12b_AdminRejectStory() {
        try {
            // 1. WRITER oluştur ve story oluştur
            String writerEmail = "writer_delete_" + System.currentTimeMillis() + "@example.com";
            String writerUsername = "writer_delete_" + System.currentTimeMillis();
            
            boolean writerRegistered = registerWriter("Writer", "Delete", writerEmail, writerUsername, "Test123456");
            if (!writerRegistered) {
                fail("Case 12b: Writer kaydı başarısız");
                return;
            }
            
            // Writer olarak zaten giriş yapılmış durumda (kayıt sonrası dashboard'a yönlendirildi)
            
            // Story oluştur ve yayınla (onay bekleyen durumda)
            String storyTitle = "Reddedilecek Story " + System.currentTimeMillis();
            String storyContent = "Bu bir reddedilecek story'dir.";
            String storySlug = createStory(writerEmail, "Test123456", storyTitle, storyContent);
            
            if (storySlug == null) {
                fail("Case 12b: Story oluşturulamadı");
                return;
            }
            
            // 2. ADMIN olarak giriş yap
            AdminCredentials adminCreds = ensureAdminUserExists();
            
            try {
                driver.get(BASE_URL + "/logout");
                Thread.sleep(500); // 2000 -> 500
            } catch (Exception e) {
                // Logout sayfası yoksa devam et
            }
            
            loginUser(adminCreds.getEmail(), adminCreds.getPassword());
            
            // Admin dashboard'a yönlendirildiğini kontrol et
            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/admin") || currentUrl.contains("/dashboard"),
                "Case 12b: Admin olarak giriş yapılamadı. URL: " + currentUrl
            );
            
            // 3. Admin dashboard'a git (onay bekleyen story'ler burada görünür)
            driver.get(BASE_URL + "/admin/dashboard");
            waitForPageLoad();
            Thread.sleep(1000); // 3000 -> 1000
            
            // 4. Story'yi bul ve reddet
            try {
                WebElement storyRow = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//div[contains(@class, 'admin-haber-item')]//*[contains(text(), '" + storyTitle + "')]")
                    )
                );
                
                // Story item container'ını bul (parent'a çık)
                WebElement storyItem = storyRow.findElement(By.xpath("./ancestor::div[contains(@class, 'admin-haber-item')]"));
                
                // Reddet butonunu bul ve tıkla (admin-haber-actions içinde)
                WebElement rejectButton = storyItem.findElement(
                    By.xpath(".//button[contains(text(), 'Reddet')]")
                );
                
                // Prompt'u override et (butona tıklamadan önce)
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "window.prompt = function() { return 'Test red sebebi - uygun değil'; }"
                );
                
                rejectButton.click();
                
                // Red sebebi prompt'unu handle et (JavaScript prompt)
                Thread.sleep(500); // 2000 -> 500
                
                // Alert'i bekle ve handle et (reddedildi mesajı için)
                try {
                    org.openqa.selenium.Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                    alert.accept();
                } catch (Exception e) {
                    // Alert yoksa devam et
                    System.out.println("Case 12b: Alert beklenmedi: " + e.getMessage());
                }
                
                Thread.sleep(1000); // 3000 -> 1000
                
                // Story'nin reddedildiğini kontrol et (artık onay bekleyen listede olmamalı)
                driver.navigate().refresh();
                Thread.sleep(1000); // 3000 -> 1000
                
                try {
                    driver.findElement(
                        By.xpath("//*[contains(text(), '" + storyTitle + "')]")
                    );
                    // Hala görünüyorsa test başarısız
                    fail("Case 12b: Story reddedilmedi (hala listede görünüyor)");
                } catch (org.openqa.selenium.NoSuchElementException e) {
                    // Story bulunamadı - bu beklenen davranış (reddedildi, listeden çıktı)
                    assertTrue(true, "Case 12b: Story başarıyla reddedildi");
                }
                
                System.out.println("Case 12b: Story reddetme başarıyla test edildi");
                
            } catch (Exception e) {
                System.out.println("Case 12b: Story reddetme testi - " + e.getMessage());
                e.printStackTrace();
                fail("Case 12b: Story reddetme testi başarısız - " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Case 12b: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 12b: Test başarısız - " + e.getMessage());
        }
    }
}

