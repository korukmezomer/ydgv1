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
 * - Writer olarak story oluştur ve yayınla
 * - Admin olarak story'yi onayla
 * - Kullanıcı giriş yapar
 * - Story sayfasına gider
 * - Beğeni butonuna tıklar
 * - Beğeninin eklendiğini doğrula
 */
@DisplayName("Case 7: Story Beğenme")
public class Case7_LikeStoryTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 7: Kullanıcı story'yi beğenebilmeli")
    public void case7_LikeStory() {
        try {
            // 1. Writer oluştur ve story yayınla (Case7a mantığı)
            java.util.Random random = new java.util.Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String writerEmail = "writer_like_" + randomSuffix + "@example.com";
            String writerPassword = "Test123456";
            
            if (!registerWriter("Writer", "Like", writerEmail, "writer_like_" + randomSuffix, writerPassword)) {
                fail("Case 7: Writer kaydı başarısız");
                return;
            }
            
            // Story oluştur ve yayınla
            String storyTitle = "Beğenme Test Story " + System.currentTimeMillis();
            String storyContent = "Bu bir beğenme test story'sidir.";
            String storySlug = createStory(writerEmail, writerPassword, storyTitle, storyContent);
            
            if (storySlug == null) {
                fail("Case 7: Story oluşturulamadı");
                return;
            }
            
            // 2. Admin olarak story'yi onayla (Case7b mantığı)
            String approvedSlug = approveStoryAsAdmin(storyTitle);
            if (approvedSlug == null) {
                fail("Case 7: Story onaylanamadı");
                return;
            }
            
            // 3. Kullanıcı (Liker) oluştur
            String likerEmail = "liker" + randomSuffix + "@example.com";
            String likerPassword = "Test123456";
            
            logout();
            
            if (!registerUser("Liker", "Test", likerEmail, "liker" + randomSuffix, likerPassword)) {
                fail("Case 7: Kullanıcı kaydı başarısız");
                return;
            }
            
            // Kayıt sonrası zaten dashboard'a yönlendirilmiş, direkt story sayfasına gidebiliriz
            // Story ID'sini al
            Long storyId = getStoryIdFromSlug(approvedSlug);
            if (storyId == null) {
                // Slug'dan alınamazsa, story title'dan almayı dene
                storyId = getStoryIdByTitle(storyTitle, writerEmail);
            }
            if (storyId == null) {
                // Son çare: Writer'ın en son story'sini al
                storyId = getLatestStoryIdByUserEmail(writerEmail);
            }
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
            driver.get(BASE_URL + "/haberler/" + approvedSlug);
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
            
            Long likeCountAfter = getLikeCountViaApi(storyId);
            assertNotNull(likeCountAfter, "Case 7: Beğeni sayısı API'den alınamadı");
            assertTrue(likeCountAfter > 0, "Case 7: Beğeni kaydedilmedi (API beğeni sayısı 0)");
            System.out.println("Case 7: Beğeni işlemi başarıyla tamamlandı, API beğeni sayısı: " + likeCountAfter);
            
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
            // 1. Writer oluştur ve story yayınla
            java.util.Random random = new java.util.Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String writerEmail = "writer_toggle_" + randomSuffix + "@example.com";
            String writerPassword = "Test123456";
            
            if (!registerWriter("Writer", "Toggle", writerEmail, "writer_toggle_" + randomSuffix, writerPassword)) {
                fail("Case 7 Negative: Writer kaydı başarısız");
                return;
            }
            
            // Story oluştur ve yayınla
            String storyTitle = "Toggle Test Story " + System.currentTimeMillis();
            String storyContent = "Bu bir toggle test story'sidir.";
            String storySlug = createStory(writerEmail, writerPassword, storyTitle, storyContent);
            
            if (storySlug == null) {
                fail("Case 7 Negative: Story oluşturulamadı");
                return;
            }
            
            // 2. Admin olarak story'yi onayla
            String approvedSlug = approveStoryAsAdmin(storyTitle);
            if (approvedSlug == null) {
                fail("Case 7 Negative: Story onaylanamadı");
                return;
            }
            
            // 3. Kullanıcı (Liker) oluştur
            String likerEmail = "liker_neg" + randomSuffix + "@example.com";
            String likerPassword = "Test123456";
            
            logout();
            
            if (!registerUser("Liker", "Test", likerEmail, "liker_neg" + randomSuffix, likerPassword)) {
                fail("Case 7 Negative: Kullanıcı kaydı başarısız");
                return;
            }
            
            // Story ID'sini al
            Long storyId = getStoryIdFromSlug(approvedSlug);
            if (storyId == null) {
                // Slug'dan alınamazsa, story title'dan almayı dene
                storyId = getStoryIdByTitle(storyTitle, writerEmail);
            }
            if (storyId == null) {
                // Son çare: Writer'ın en son story'sini al
                storyId = getLatestStoryIdByUserEmail(writerEmail);
            }
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
            driver.get(BASE_URL + "/haberler/" + approvedSlug);
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
            
            Long initialLikeCount = getLikeCountViaApi(storyId);
            assertNotNull(initialLikeCount, "Case 7 Negative: Başlangıç beğeni sayısı alınamadı");
            
            likeButton.click();
            Thread.sleep(3000); // Beğeninin kaydedilmesi için bekle
            
            Long likeCountAfterFirst = getLikeCountViaApi(storyId);
            assertNotNull(likeCountAfterFirst, "Case 7 Negative: Beğeni sonrası beğeni sayısı alınamadı");
            assertTrue(likeCountAfterFirst >= initialLikeCount + 1, "Case 7 Negative: İlk beğeni kaydedilmedi (API)");
            
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
            
            Long likeCountAfterSecond = getLikeCountViaApi(storyId);
            assertNotNull(likeCountAfterSecond, "Case 7 Negative: Beğeni kaldırma sonrası beğeni sayısı alınamadı");
            assertTrue(likeCountAfterSecond <= initialLikeCount, "Case 7 Negative: Beğeni kaldırılmadı (API)");
            
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

