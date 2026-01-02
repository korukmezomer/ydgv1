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
            
            // Story oluştur ve yayınla (onay bekleyen durumda)
            String storyTitle = "Reddedilecek Story " + System.currentTimeMillis();
            String storyContent = "Bu bir reddedilecek story'dir.";
            String storySlug = createStory(writerEmail, "Test123456", storyTitle, storyContent);
            
            if (storySlug == null) {
                fail("Case 12b: Story oluşturulamadı");
                return;
            }
            
            // Veritabanı transaction'ının commit olması için bekle
            Thread.sleep(2000);
            
            // 2. ADMIN olarak giriş yap
            AdminCredentials adminCreds = ensureAdminUserExists();
            
            try {
                driver.get(BASE_URL + "/logout");
                Thread.sleep(2000);
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
            
            // 3. Direkt admin dashboard'a git (onay bekleyen story'ler burada)
            driver.get(BASE_URL + "/admin/dashboard");
            waitForPageLoad();
            Thread.sleep(3000);
            
            // Sayfa yüklemesini bekle
            wait.until(
                ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-dashboard-container")),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-haber-item"))
                )
            );
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
            Thread.sleep(2000);
            
            // 4. Story'yi bul (ilk iki sayfada ara - en yeni en başta olduğu için)
            System.out.println("Story aranıyor: " + storyTitle);
            
            WebElement storyElement = null;
            try {
                // İlk sayfada ara
                storyElement = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//div[contains(@class, 'admin-haber-item')]//*[contains(text(), '" + storyTitle + "')]")
                    )
                );
                System.out.println("Story ilk sayfada bulundu: " + storyTitle);
            } catch (org.openqa.selenium.TimeoutException e) {
                // İlk sayfada bulunamadı, ikinci sayfayı kontrol et
                System.out.println("Story ilk sayfada bulunamadı, ikinci sayfa kontrol ediliyor...");
                try {
                    WebElement nextButton = driver.findElement(
                        By.xpath("//div[contains(@class, 'admin-pagination')]//button[contains(text(), 'Sonraki') or contains(text(), 'Next')]")
                    );
                    if (nextButton.getAttribute("disabled") == null) {
                        safeClick(nextButton);
                        Thread.sleep(2000);
                        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
                        Thread.sleep(2000);
                        
                        storyElement = wait.until(
                            ExpectedConditions.presenceOfElementLocated(
                                By.xpath("//div[contains(@class, 'admin-haber-item')]//*[contains(text(), '" + storyTitle + "')]")
                            )
                        );
                        System.out.println("Story ikinci sayfada bulundu: " + storyTitle);
                    }
                } catch (Exception ex) {
                    System.out.println("Story bulunamadı: " + storyTitle);
                }
            }
            
            if (storyElement == null) {
                fail("Case 12b: Story admin dashboard'da bulunamadı: " + storyTitle);
                return;
            }
            
            // 5. Story item container'ını bul
            WebElement storyItem = storyElement.findElement(By.xpath("./ancestor::div[contains(@class, 'admin-haber-item')]"));
            
            // 6. Reddet butonunu bul ve tıkla
            WebElement rejectButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    storyItem.findElement(By.xpath(".//button[contains(text(), 'Reddet')]"))
                )
            );
            
            System.out.println("Reddet butonu bulundu, tıklanıyor...");
            
            // Prompt'u override et (butona tıklamadan önce)
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "window.prompt = function() { return 'Test red sebebi - uygun değil'; }"
            );
            
            safeClick(rejectButton);
            
            // Red sebebi prompt'unu handle et
            Thread.sleep(1000);
            
            // Alert'i bekle ve handle et (reddedildi mesajı için)
            try {
                org.openqa.selenium.Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                alert.accept();
            } catch (Exception e) {
                // Alert yoksa devam et
                System.out.println("Case 12b: Alert beklenmedi");
            }
            
            // Silme işleminin tamamlanmasını bekle
            Thread.sleep(3000);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
            Thread.sleep(2000);
            
            // 7. Story'nin reddedildiğini kontrol et - sayfayı yenile
            driver.navigate().refresh();
            Thread.sleep(3000);
            waitForPageLoad();
            
            wait.until(
                ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-dashboard-container")),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-haber-item"))
                )
            );
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
            Thread.sleep(2000);
            
            // Story artık listede olmamalı
            try {
                driver.findElement(
                    By.xpath("//div[contains(@class, 'admin-haber-item')]//*[contains(text(), '" + storyTitle + "')]")
                );
                // Hala görünüyorsa test başarısız
                fail("Case 12b: Story reddedilmedi (hala listede görünüyor)");
            } catch (org.openqa.selenium.NoSuchElementException e) {
                // Story bulunamadı - bu beklenen davranış (reddedildi, listeden çıktı)
                System.out.println("Case 12b: Story listede bulunamadı (reddedildi)");
                assertTrue(true, "Case 12b: Story başarıyla reddedildi");
            }
            
            System.out.println("Case 12b: Story reddetme başarıyla test edildi");
            
        } catch (Exception e) {
            System.err.println("Case 12b: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 12b: Test başarısız - " + e.getMessage());
        }
    }
}
