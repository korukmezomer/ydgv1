package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 11: Bildirim Testleri (Notification Tests)
 * 
 * Test Senaryoları:
 * 1. Yazarın yazısına kullanıcı yorum yapınca bildirim gidiyor mu?
 * 2. Yazarın yazısı beğenilince bildirim gidiyor mu?
 * 3. Bildirimler doğru görüntüleniyor mu?
 * 4. Bildirim okundu işaretleniyor mu?
 */
@DisplayName("Case 11: Bildirim Testleri")
public class Case11_NotificationTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 11.1: Yorum bildirimi - Yazar yazısına yorum yapılınca bildirim gidiyor mu?")
    public void case11_1_CommentNotification() {
        try {
            // 1. WRITER rolünde bir kullanıcı oluştur ve story oluştur
        String writerEmail = "writer_notif_" + System.currentTimeMillis() + "@example.com";
        String writerUsername = "writer_notif_" + System.currentTimeMillis();
        
        // Writer kaydı
        driver.get(BASE_URL + "/register");
        waitForPageLoad();
        
        WebElement firstNameInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
        );
        firstNameInput.sendKeys("Writer");
        driver.findElement(By.id("lastName")).sendKeys("Notification");
        driver.findElement(By.id("email")).sendKeys(writerEmail);
        driver.findElement(By.id("username")).sendKeys(writerUsername);
        driver.findElement(By.id("password")).sendKeys("Test123456");
        
        // Role seçimi - WRITER
        try {
            WebElement roleSelectElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("roleName"))
            );
            Select roleSelect = new Select(roleSelectElement);
            try {
                roleSelect.selectByValue("WRITER");
            } catch (Exception e) {
                try {
                    roleSelect.selectByVisibleText("WRITER");
                } catch (Exception e2) {
                    // JavaScript ile değer set et
                    ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("arguments[0].value = 'WRITER';", roleSelectElement);
                }
            }
        } catch (Exception e) {
            // Role select yoksa devam et
            System.out.println("Role select bulunamadı: " + e.getMessage());
        }
        
        WebElement form = driver.findElement(By.cssSelector("form"));
        WebElement submitButton = form.findElement(By.cssSelector("button[type='submit']"));
        safeSubmitForm(submitButton, form);
        Thread.sleep(3000);
        
            // Story oluştur
            driver.get(BASE_URL + "/reader/new-story");
            waitForPageLoad();
            Thread.sleep(2000);
            
            // Başlık ve içerik gir
            WebElement titleInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input.story-title-input, input[placeholder*='Başlık']")
                )
            );
            String storyTitle = "Bildirim Test Story " + System.currentTimeMillis();
            titleInput.sendKeys(storyTitle);
            
            Thread.sleep(1000);
            
            WebElement contentTextarea = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.block-textarea")
                )
            );
            contentTextarea.sendKeys("Bu bir bildirim test story'sidir.");
            
            Thread.sleep(1000);
            
            // Yayınla
            WebElement publishButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".publish-button, button.publish-button")
                )
            );
            publishButton.click();
            Thread.sleep(5000);
            
            // Story slug'ı ve ID'yi API'den al
            Long storyId = getStoryIdByTitle(storyTitle, writerEmail);
            String storySlug = null;
            if (storyId != null) {
                storySlug = getStorySlugViaApi(storyId);
            }
        
        // Story oluşturuldu, şimdi admin onayı gerekiyor
        assertNotNull(storyId, "Story ID alınamadı");
        assertTrue(approveStoryViaApi(storyId), "Story API ile onaylanamadı");
        if (storySlug == null) {
            storySlug = getStorySlugViaApi(storyId);
        }
        assertNotNull(storySlug, "Story slug alınamadı");
        
        // 2. Farklı bir kullanıcı (USER) oluştur ve yorum yap
        String commenterEmail = "commenter_notif_" + System.currentTimeMillis() + "@example.com";
        String commenterUsername = "commenter_notif_" + System.currentTimeMillis();
        
        // Yeni tarayıcı oturumu için logout yap
        try {
            driver.get(BASE_URL + "/logout");
            Thread.sleep(2000);
        } catch (Exception e) {
            // Logout sayfası yoksa devam et
        }
        
        // Commenter kaydı
        driver.get(BASE_URL + "/register");
        waitForPageLoad();
        
        firstNameInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
        );
        firstNameInput.sendKeys("Commenter");
        driver.findElement(By.id("lastName")).sendKeys("Notification");
        driver.findElement(By.id("email")).sendKeys(commenterEmail);
        driver.findElement(By.id("username")).sendKeys(commenterUsername);
        driver.findElement(By.id("password")).sendKeys("Test123456");
        
        WebElement commenterForm = driver.findElement(By.cssSelector("form"));
        WebElement commenterSubmitButton = commenterForm.findElement(By.cssSelector("button[type='submit']"));
        safeSubmitForm(commenterSubmitButton, commenterForm);
        Thread.sleep(3000);
        
        // Story sayfasına git
        driver.get(BASE_URL + "/haberler/" + storySlug);
        waitForPageLoad();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".article-detail-page, .article-container")));
        Thread.sleep(500);
        
        // Yorum alanı ve sayfa yüklenmesi
        WebElement commentTextarea = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("textarea.comment-textarea, textarea[placeholder*='yorum'], textarea[placeholder*='Yorum'], textarea.comment-input")
            )
        );
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", commentTextarea);
        Thread.sleep(300);
        String commentText = "Bu bir test yorumudur - bildirim testi için.";
        commentTextarea.sendKeys(commentText);
        
        Thread.sleep(1000);
        
        WebElement submitCommentButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Gönder') or contains(text(), 'Yorum')] | //button[@type='submit']")
            )
        );
        try {
            submitCommentButton.click();
        } catch (Exception clickEx) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", submitCommentButton);
        }
        Thread.sleep(1500);
        
        // 3. Writer olarak giriş yap ve bildirimleri kontrol et
        try {
            driver.get(BASE_URL + "/logout");
            Thread.sleep(2000);
        } catch (Exception e) {
            // Logout sayfası yoksa devam et
        }
        
        driver.get(BASE_URL + "/login");
        waitForPageLoad();
        
        WebElement emailInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("email"))
        );
        emailInput.sendKeys(writerEmail);
        driver.findElement(By.id("password")).sendKeys("Test123456");
        
        WebElement writerLoginFormFinal = driver.findElement(By.cssSelector("form"));
        WebElement writerLoginSubmitButtonFinal = writerLoginFormFinal.findElement(By.cssSelector("button[type='submit']"));
        safeSubmitForm(writerLoginSubmitButtonFinal, writerLoginFormFinal);
        Thread.sleep(3000);
        
        // Bildirimler sayfasına git
        driver.get(BASE_URL + "/reader/notifications");
        waitForPageLoad();
        Thread.sleep(3000);
        
        // Bildirimi kontrol et (30 sn'ye kadar bekle; yoksa bir kez refresh dene)
        WebElement notificationElement = null;
        boolean notificationFound = false;
        for (int i = 0; i < 2 && !notificationFound; i++) {
            try {
                notificationElement = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(30)).until(
                    ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[contains(text(), 'yorum') or contains(text(), 'Yorum')] | //*[contains(text(), '" + commenterUsername + "')]")
                    )
                );
                notificationFound = notificationElement.isDisplayed();
            } catch (Exception ex) {
                driver.navigate().refresh();
                waitForPageLoad();
                Thread.sleep(1000);
            }
        }
        
        assertTrue(notificationFound, "Case 11.1: Yorum bildirimi görüntülenmedi");
        
        // Bildirim mesajını kontrol et
        String notificationText = notificationElement.getText();
        assertTrue(
            notificationText.contains(commenterUsername) || 
            notificationText.contains("yorum") ||
            notificationText.contains("Yorum"),
            "Case 11.1: Bildirim mesajı yanlış. Mesaj: " + notificationText
        );
        
        System.out.println("Case 11.1: Yorum bildirimi başarıyla test edildi");
        
        } catch (Exception ex) {
            System.out.println("Case 11.1: Hata - " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    
    @Test
    @DisplayName("Case 11.2: Beğeni bildirimi - Yazar yazısı beğenilince bildirim gidiyor mu?")
    public void case11_2_LikeNotification() {
        try {
        // 1. WRITER rolünde bir kullanıcı oluştur ve story oluştur
        String writerEmail = "writer_like_" + System.currentTimeMillis() + "@example.com";
        String writerUsername = "writer_like_" + System.currentTimeMillis();
        
        // Writer kaydı
        driver.get(BASE_URL + "/register");
        waitForPageLoad();
        
        WebElement firstNameInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
        );
        firstNameInput.sendKeys("Writer");
        driver.findElement(By.id("lastName")).sendKeys("Like");
        driver.findElement(By.id("email")).sendKeys(writerEmail);
        driver.findElement(By.id("username")).sendKeys(writerUsername);
        driver.findElement(By.id("password")).sendKeys("Test123456");
        
        // Role seçimi - WRITER
        try {
            WebElement roleSelectElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("roleName"))
            );
            Select roleSelect = new Select(roleSelectElement);
            try {
                roleSelect.selectByValue("WRITER");
            } catch (Exception e) {
                try {
                    roleSelect.selectByVisibleText("WRITER");
                } catch (Exception e2) {
                    // JavaScript ile değer set et
                    ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("arguments[0].value = 'WRITER';", roleSelectElement);
                }
            }
        } catch (Exception e) {
            // Role select yoksa devam et
            System.out.println("Role select bulunamadı: " + e.getMessage());
        }
        
        WebElement form = driver.findElement(By.cssSelector("form"));
        WebElement submitButton = form.findElement(By.cssSelector("button[type='submit']"));
        safeSubmitForm(submitButton, form);
        Thread.sleep(3000);
        
        // Story oluştur
        driver.get(BASE_URL + "/reader/new-story");
        waitForPageLoad();
        Thread.sleep(2000);
        
        // Başlık ve içerik gir
        WebElement titleInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input.story-title-input, input[placeholder*='Başlık']")
            )
        );
        String storyTitle = "Beğeni Bildirim Test " + System.currentTimeMillis();
        titleInput.sendKeys(storyTitle);
        
        Thread.sleep(1000);
        
        WebElement contentTextarea = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("textarea.block-textarea")
            )
        );
        contentTextarea.sendKeys("Bu bir beğeni bildirim test story'sidir.");
        
        Thread.sleep(1000);
        
        // Yayınla
        WebElement publishButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".publish-button, button.publish-button")
            )
        );
        publishButton.click();
        Thread.sleep(5000);
        
        Long storyId = getStoryIdByTitle(storyTitle, writerEmail);
        String storySlug = null;
        if (storyId != null) {
            storySlug = getStorySlugViaApi(storyId);
        }
        
        // Story oluşturuldu, şimdi admin onayı gerekiyor
        assertNotNull(storyId, "Story ID alınamadı");
        assertTrue(approveStoryViaApi(storyId), "Story API ile onaylanamadı");
        if (storySlug == null) {
            storySlug = getStorySlugViaApi(storyId);
        }
        assertNotNull(storySlug, "Story slug alınamadı");
        
        // 2. Farklı bir kullanıcı (USER) oluştur ve beğen
        String likerEmail = "liker_notif_" + System.currentTimeMillis() + "@example.com";
        String likerUsername = "liker_notif_" + System.currentTimeMillis();
        
        // Logout
        try {
            driver.get(BASE_URL + "/logout");
            Thread.sleep(2000);
        } catch (Exception e) {
            // Logout sayfası yoksa devam et
        }
        
        // Liker kaydı
        driver.get(BASE_URL + "/register");
        waitForPageLoad();
        
        firstNameInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
        );
        firstNameInput.sendKeys("Liker");
        driver.findElement(By.id("lastName")).sendKeys("Notification");
        driver.findElement(By.id("email")).sendKeys(likerEmail);
        driver.findElement(By.id("username")).sendKeys(likerUsername);
        driver.findElement(By.id("password")).sendKeys("Test123456");
        
        WebElement likerForm = driver.findElement(By.cssSelector("form"));
        WebElement likerSubmitButton = likerForm.findElement(By.cssSelector("button[type='submit']"));
        safeSubmitForm(likerSubmitButton, likerForm);
        Thread.sleep(3000);
        
        // Story sayfasına git
        driver.get(BASE_URL + "/haberler/" + storySlug);
        waitForPageLoad();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".article-detail-page, .article-container")));
        Thread.sleep(500);
        
        // Beğen
        WebElement likeButton = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//button[contains(text(), 'Beğen') or contains(text(), 'beğen')] | //button[@aria-label[contains(., 'like') or contains(., 'beğen')]] | //*[contains(@class, 'like-button')] | //button[@title='Beğen' or contains(@class, 'action-btn') or contains(@class, 'article-action-btn')]")
            )
        );
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", likeButton);
        WebElement clickableLike = wait.until(
            ExpectedConditions.elementToBeClickable(likeButton)
        );
        try {
            clickableLike.click();
        } catch (Exception clickEx) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", clickableLike);
        }
        Thread.sleep(1500);
        
        // 3. Writer olarak giriş yap ve bildirimleri kontrol et
        try {
            driver.get(BASE_URL + "/logout");
            Thread.sleep(2000);
        } catch (Exception e) {
            // Logout sayfası yoksa devam et
        }
        
        driver.get(BASE_URL + "/login");
        waitForPageLoad();
        
        WebElement emailInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("email"))
        );
        emailInput.sendKeys(writerEmail);
        driver.findElement(By.id("password")).sendKeys("Test123456");
        
        WebElement writerLikeLoginForm = driver.findElement(By.cssSelector("form"));
        WebElement writerLikeLoginSubmitButton = writerLikeLoginForm.findElement(By.cssSelector("button[type='submit']"));
        safeSubmitForm(writerLikeLoginSubmitButton, writerLikeLoginForm);
        Thread.sleep(3000);
        
        // Bildirimler sayfasına git
        driver.get(BASE_URL + "/reader/notifications");
        waitForPageLoad();
        Thread.sleep(3000);
        
        // Bildirimi kontrol et (30 sn'ye kadar bekle; yoksa bir kez refresh dene)
        WebElement notificationElement = null;
        boolean likeNotificationFound = false;
        for (int i = 0; i < 2 && !likeNotificationFound; i++) {
            try {
                notificationElement = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(30)).until(
                    ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[contains(text(), 'beğen') or contains(text(), 'Beğen')] | //*[contains(text(), '" + likerUsername + "')]")
                    )
                );
                likeNotificationFound = notificationElement.isDisplayed();
            } catch (Exception ex) {
                driver.navigate().refresh();
                waitForPageLoad();
                Thread.sleep(1000);
            }
        }
        
        assertTrue(likeNotificationFound, "Case 11.2: Beğeni bildirimi görüntülenmedi");
        
        // Bildirim mesajını kontrol et
        String notificationText = notificationElement.getText();
        assertTrue(
            notificationText.contains(likerUsername) || 
            notificationText.contains("beğen") ||
            notificationText.contains("Beğen"),
            "Case 11.2: Bildirim mesajı yanlış. Mesaj: " + notificationText
        );
        
        System.out.println("Case 11.2: Beğeni bildirimi başarıyla test edildi");
        
        } catch (Exception ex) {
            System.out.println("Case 11.2: Hata - " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}

