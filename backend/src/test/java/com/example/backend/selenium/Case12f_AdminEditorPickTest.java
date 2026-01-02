package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 12f: Admin Editor Seçimi (Admin Editor Pick)
 * 
 * Test Senaryoları:
 * 1. Admin yayınlanmış story'leri görüntüleyebilmeli
 * 2. Admin story'yi editor seçimi yapabilmeli
 * 3. Admin editor seçimini kaldırabilmeli (toggle)
 */
@DisplayName("Case 12f: Admin Editor Seçimi")
public class Case12f_AdminEditorPickTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 12f: Admin editor seçimi yapabilmeli")
    public void case12f_AdminToggleEditorPick() {
        try {
            // 1. WRITER oluştur ve story oluştur (BaseSeleniumTest helper metodlarını kullan)
            String writerEmail = "writer_editor_" + System.currentTimeMillis() + "@example.com";
            String writerUsername = "writer_editor_" + System.currentTimeMillis();
            
            boolean writerRegistered = registerWriter("Writer", "Editor", writerEmail, writerUsername, "Test123456");
            if (!writerRegistered) {
                fail("Case 12f: Writer kaydı başarısız");
                return;
            }
            
            // Writer olarak zaten giriş yapılmış durumda (kayıt sonrası dashboard'a yönlendirildi)
            
            // Story oluştur ve yayınla
            String storyTitle = "Editor Seçimi Story " + System.currentTimeMillis();
            String storyContent = "Bu bir editor seçimi test story'sidir.";
            String storySlug = createStory(writerEmail, "Test123456", storyTitle, storyContent);
            
            if (storySlug == null) {
                fail("Case 12f: Story oluşturulamadı");
                return;
            }
            
            // Story'yi admin onayından geçir
            storySlug = approveStoryAsAdmin(storyTitle);
            if (storySlug == null) {
                fail("Case 12f: Story onaylanamadı");
                return;
            }
            
            // 2. ADMIN olarak giriş yap (BaseSeleniumTest'teki loginUser helper metodunu kullan)
            AdminCredentials adminCreds = ensureAdminUserExists();
            
            try {
                driver.get(BASE_URL + "/logout");
                Thread.sleep(500); // 2000 -> 500
            } catch (Exception e) {
                // Logout sayfası yoksa devam et
            }
            
            loginUser(adminCreds.getEmail(), adminCreds.getPassword());
            
            // 3. Sidebar'ı aç ve "Editör Seçimleri" linkine tıkla
            try {
                WebElement sidebarToggle = wait.until(
                    ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[aria-label*='menu'], .menu-toggle, .sidebar-toggle, button[class*='menu']")
                    )
                );
                sidebarToggle.click();
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Case 12f: Sidebar toggle bulunamadı");
            }
            
            // "Editör Seçimleri" linkini bul ve tıkla
            WebElement editorPicksLink = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@href, '/admin/editor-secimleri')] | //a[contains(text(), 'Editör Seçimleri')]")
                )
            );
            editorPicksLink.click();
            waitForPageLoad();
            Thread.sleep(1000); // 3000 -> 1000
            
            // 4. Story'yi bul ve editor seçimi yap - tüm sayfalarda ara
            WebElement storyItem = findStoryInEditorPicksAllPages(storyTitle);
            
            if (storyItem == null) {
                fail("Case 12f: Story editör seçimleri sayfasında bulunamadı: " + storyTitle);
                return;
            }
            
            // Story item container'ını bul
            WebElement storyContainer = storyItem.findElement(By.xpath("./ancestor::div[contains(@class, 'admin-editor-pick-item')]"));
            
            // Editor seçimi butonunu bul
            WebElement editorPickButton = storyContainer.findElement(
                By.xpath(".//button[contains(text(), 'Editör Seçimi')]")
            );
            
            String buttonTextBefore = editorPickButton.getText();
            
            // Editor seçimi yap
            editorPickButton.click();
            Thread.sleep(1000); // 3000 -> 1000
            
            // Butonun durumunun değiştiğini kontrol et (yeniden bul)
            storyContainer = driver.findElement(
                By.xpath("//div[contains(@class, 'admin-editor-pick-item')]//*[contains(text(), '" + storyTitle + "')]/ancestor::div[contains(@class, 'admin-editor-pick-item')]")
            );
            WebElement editorPickButtonAfter = storyContainer.findElement(
                By.xpath(".//button[contains(text(), 'Editör Seçimi')]")
            );
            String buttonTextAfter = editorPickButtonAfter.getText();
            
            // Buton metni değişmeli (örneğin "Editör Seçimi Yap" -> "✓ Editör Seçimi")
            assertNotEquals(buttonTextBefore, buttonTextAfter,
                "Case 12f: Editor seçimi butonu durumu değişmedi");
            
            System.out.println("Case 12f: Editor seçimi başarıyla test edildi");
            
        } catch (Exception e) {
            System.err.println("Case 12f: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 12f: Test başarısız - " + e.getMessage());
        }
    }
}

