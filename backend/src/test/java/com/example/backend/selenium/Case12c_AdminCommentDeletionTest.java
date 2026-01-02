package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 12c: Admin Yorum Silme (Admin Comment Deletion)
 * 
 * Test Senaryoları:
 * 1. Kullanıcı yorum yapar (yorum direkt yayınlanır, onay gerekmez)
 * 2. Admin yorumu görüntüleyebilmeli
 * 3. Admin yorumu silebilmeli
 * 
 * Not: Yorumlar direkt yayınlanır, admin onaylamaz. Admin sadece yorumu silebilir.
 */
@DisplayName("Case 12c: Admin Yorum Silme")
public class Case12c_AdminCommentDeletionTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 12c: Admin yorumu silebilmeli")
    public void case12c_AdminDeleteComment() {
        try {
            // 1. Writer oluştur, story oluştur ve admin onayla (mevcut helper metodları kullan)
            String writerEmail = "writer_comment_del_" + System.currentTimeMillis() + "@example.com";
            String writerUsername = "writer_comment_del_" + System.currentTimeMillis();
            
            boolean writerRegistered = registerWriter("Writer", "CommentDel", writerEmail, writerUsername, "Test123456");
            if (!writerRegistered) {
                fail("Case 12c: Writer kaydı başarısız");
                return;
            }
            
            // Story oluştur
            String storyTitle = "Yorum Test Story " + System.currentTimeMillis();
            String storyContent = "Bu bir yorum test story'sidir.";
            String storySlug = createStory(writerEmail, "Test123456", storyTitle, storyContent);
            
            if (storySlug == null) {
                fail("Case 12c: Story oluşturulamadı");
                return;
            }
            
            // Story'yi admin onayından geçir
            storySlug = approveStoryAsAdmin(storyTitle);
            if (storySlug == null) {
                fail("Case 12c: Story onaylanamadı");
                return;
            }
            
            // 2. Kullanıcı kaydı yap (zaten giriş yapılmış durumda)
            String userEmail = "user_comment_del_" + System.currentTimeMillis() + "@example.com";
            String username = "user_comment_del_" + System.currentTimeMillis();
            
            // Önce logout yap
            try {
                driver.get(BASE_URL + "/logout");
                Thread.sleep(3000);
                waitForPageLoad();
            } catch (Exception e) {
                // Logout sayfası yoksa devam et
            }
            
            // Eğer hala giriş yapılmış durumdaysa, direkt register sayfasına git
            String registerUrl = driver.getCurrentUrl();
            if (!registerUrl.contains("/register") && !registerUrl.contains("/login")) {
                driver.get(BASE_URL + "/register");
                Thread.sleep(2000);
                waitForPageLoad();
            }
            
            boolean userRegistered = registerUser("User", "CommentDel", userEmail, username, "Test123456");
            if (!userRegistered) {
                // Kayıt başarısız oldu, tekrar dene
                Thread.sleep(2000);
                driver.get(BASE_URL + "/register");
                Thread.sleep(2000);
                waitForPageLoad();
                userRegistered = registerUser("User", "CommentDel", userEmail, username, "Test123456");
                if (!userRegistered) {
                    fail("Case 12c: Kullanıcı kaydı başarısız");
                    return;
                }
            }
            
            // Story sayfasına git ve yorum yap
            driver.get(BASE_URL + "/haberler/" + storySlug);
            waitForPageLoad();
            Thread.sleep(3000);
            
            String commentText = "Admin tarafından silinecek yorum " + System.currentTimeMillis();
            
            // Yorum alanını bul ve yorum yap
            WebElement commentInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea[placeholder*='yorum'], textarea[placeholder*='Yorum'], textarea")
                )
            );
            commentInput.sendKeys(commentText);
            Thread.sleep(1000);
            
            // Yorum gönder butonunu bul ve tıkla
            WebElement submitCommentButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Gönder') or contains(text(), 'Yorum')] | //button[@type='submit']")
                )
            );
            safeClick(submitCommentButton);
            Thread.sleep(3000); // Yorumun yayınlanması için bekle
            
            // 3. ADMIN olarak giriş yap
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
                "Case 12c: Admin olarak giriş yapılamadı. URL: " + currentUrl
            );
            
            // 4. Sidebar'ı aç ve "Yorumlar" linkine tıkla
            try {
                // Header içindeki hamburger menu butonunu bul (daha spesifik)
                WebElement sidebarToggle = wait.until(
                    ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".reader-header button.hamburger-menu, .reader-header-content button.hamburger-menu, button.hamburger-menu[aria-label='Menu']")
                    )
                );
                
                // Butonun görünür olduğundan emin ol
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", sidebarToggle);
                Thread.sleep(500);
                
                // JavaScript ile tıkla (React state güncellemesi için)
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", sidebarToggle);
                Thread.sleep(1000);
                
                // Sidebar'ın açılmasını bekle
                wait.until(
                    ExpectedConditions.and(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(".writer-sidebar.open")),
                        ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".writer-sidebar.open"))
                    )
                );
                Thread.sleep(1000);
            } catch (Exception e) {
                // Sidebar toggle bulunamadıysa direkt URL'e git
                System.out.println("Case 12c: Sidebar toggle bulunamadı, direkt URL'e gidiliyor: " + e.getMessage());
                driver.get(BASE_URL + "/admin/comments");
                waitForPageLoad();
                Thread.sleep(2000);
            }
            
            // "Yorumlar" linkini bul ve tıkla (sidebar açıksa)
            try {
            WebElement commentsLink = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".writer-sidebar.open .sidebar-link[href='/admin/comments'], .writer-sidebar.open a[href*='/admin/comments']")
                )
            );
                safeClick(commentsLink);
            } catch (Exception e) {
                // Link bulunamazsa direkt URL'e git
                driver.get(BASE_URL + "/admin/comments");
            }
            waitForPageLoad();
            Thread.sleep(3000);
            
            // 5. Yorumu tüm sayfalarda ara ve sil
            WebElement commentItem = findCommentInAllPages(commentText, "ONAYLANDI");
            
            if (commentItem == null) {
                fail("Case 12c: Yorum listede bulunamadı: " + commentText);
                return;
            }
            
            // Comment item container'ını bul
            WebElement commentContainer = commentItem.findElement(By.xpath("./ancestor::div[contains(@class, 'admin-haber-item')]"));
            
            // Sil butonunu bul ve tıkla
            WebElement deleteButton = commentContainer.findElement(
                By.xpath(".//button[contains(text(), 'Sil')]")
            );
            safeClick(deleteButton);
            
            // Confirm dialog'u kabul et
            Thread.sleep(1000);
            try {
                driver.switchTo().alert().accept();
            } catch (Exception e) {
                // Alert yoksa devam et
            }
            
            Thread.sleep(3000);
            
            // Yorumun silindiğini kontrol et - tüm sayfalarda ara
            Thread.sleep(2000);
            WebElement deletedComment = findCommentInAllPages(commentText, "ONAYLANDI");
            
            if (deletedComment != null) {
                fail("Case 12c: Yorum silinmedi (hala listede görünüyor)");
            } else {
                assertTrue(true, "Case 12c: Yorum başarıyla silindi");
            }
            
            System.out.println("Case 12c: Yorum silme başarıyla test edildi");
            
        } catch (Exception e) {
            System.err.println("Case 12c: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 12c: Test başarısız - " + e.getMessage());
        }
    }
}

