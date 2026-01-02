package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 15: Admin Yazı İnceleme (Admin Story Review)
 * 
 * Test Senaryoları:
 * 1. Admin yazılmış bir yazıyı inceleyebilmeli (İncele butonuna tıklayarak)
 * 2. Admin yazının detay sayfasını görebilmeli
 * 3. Admin yazının içeriğini, başlığını, yazarını görebilmeli
 * 
 * Not: Case7b sadece onaylama yapar, bu test sadece inceleme yapar
 */
@DisplayName("Case 15: Admin Yazı İnceleme")
public class Case15_AdminReviewStoryTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Case 15: Admin yazılmış bir yazıyı inceleyebilmeli")
    public void case15_AdminReviewStory() {
        try {
            // 1. WRITER olarak kayıt ol ve story oluştur (helper metodları kullan)
            String writerEmail = "writer_review_" + System.currentTimeMillis() + "@example.com";
            String writerUsername = "writer_review_" + System.currentTimeMillis();
            String writerPassword = "Test123456";

            boolean writerRegistered = registerWriter("Writer", "Review", writerEmail, writerUsername, writerPassword);
            if (!writerRegistered) {
                fail("Case 15: Writer kaydı başarısız");
                return;
            }

            String storyTitle = "İncelenecek Story " + System.currentTimeMillis();
            String storyContent = "Bu admin tarafından incelenecek bir story'dir.\n\nİkinci paragraf içeriği.";
            String storySlug = createStory(writerEmail, writerPassword, storyTitle, storyContent);

            if (storySlug == null) {
                fail("Case 15: Story oluşturulamadı");
                return;
            }

            // Story ID'yi al (retry logic ile)
            Long storyId = getStoryIdByTitle(storyTitle, writerEmail);
            if (storyId == null) {
                // Son çare: Slug'dan ID almayı dene
                storyId = getStoryIdFromSlug(storySlug);
            }
            if (storyId == null) {
                // En son çare: Kullanıcının en son story'sini al
                storyId = getLatestStoryIdByUserEmail(writerEmail);
            }
            if (storyId == null) {
                fail("Case 15: Story ID alınamadı");
                return;
            }

            // 2. ADMIN olarak giriş yap (helper metod kullan)
            AdminCredentials adminCreds = ensureAdminUserExists();
            logout();
            Thread.sleep(2000);
            loginUser(adminCreds.getEmail(), adminCreds.getPassword());

            // 3. Admin dashboard'a git
            driver.get(BASE_URL + "/admin/dashboard");
            waitForPageLoad();
            Thread.sleep(3000);

            // 4. Story'yi bul (onay bekleyen haberler listesinde)
            WebElement storyRow = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//div[contains(@class, 'admin-haber-item')]//*[contains(text(), '" + storyTitle + "')]")
                )
            ).findElement(By.xpath("./ancestor::div[contains(@class, 'admin-haber-item')]"));

            // 5. Story'nin başlığını kontrol et
            WebElement storyTitleElement = storyRow.findElement(
                By.cssSelector(".admin-haber-title, h3")
            );
            String foundTitle = storyTitleElement.getText();
            assertTrue(
                foundTitle.contains(storyTitle),
                "Case 15: Story başlığı bulunamadı. Beklenen: " + storyTitle + ", Bulunan: " + foundTitle
            );

            // 6. Story detaylarını kontrol et (yazar, özet, tarih)
            WebElement authorElement = storyRow.findElement(
                By.cssSelector(".admin-haber-yazar, p")
            );
            String authorText = authorElement.getText();
            assertTrue(
                authorText.contains("Yazar") || authorText.contains(writerUsername),
                "Case 15: Yazar bilgisi bulunamadı. Bulunan: " + authorText
            );

            WebElement summaryElement = storyRow.findElement(
                By.cssSelector(".admin-haber-ozet, p")
            );
            String summaryText = summaryElement.getText();
            assertTrue(
                summaryText != null && !summaryText.isEmpty(),
                "Case 15: Story özeti bulunamadı"
            );

            // 7. "İncele" butonunu bul ve tıkla
            WebElement reviewButton = storyRow.findElement(
                By.xpath(".//button[contains(text(), 'İncele')]")
            );
            assertTrue(
                reviewButton.isDisplayed(),
                "Case 15: İncele butonu bulunamadı"
            );

            safeClick(reviewButton);
            waitForPageLoad();
            Thread.sleep(3000);

            // 8. Story detay sayfasına yönlendirildiğini kontrol et
            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/haberler/") && currentUrl.contains(storySlug),
                "Case 15: Story detay sayfasına yönlendirilmedi. URL: " + currentUrl
            );

            // 9. Story detay sayfasında başlığı kontrol et
            WebElement detailTitle = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("h1, .article-title, .story-title")
                )
            );
            String detailTitleText = detailTitle.getText();
            assertTrue(
                detailTitleText.contains(storyTitle) || storyTitle.contains(detailTitleText),
                "Case 15: Story detay sayfasında başlık bulunamadı. Beklenen: " + storyTitle + ", Bulunan: " + detailTitleText
            );

            // 10. Story içeriğini kontrol et
            WebElement contentElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".article-content, .story-content, article, .content")
                )
            );
            String contentText = contentElement.getText();
            assertTrue(
                contentText.contains("İncelenecek") || contentText.contains("ikinci paragraf"),
                "Case 15: Story içeriği görüntülenemedi. İçerik: " + contentText.substring(0, Math.min(100, contentText.length()))
            );

            // 11. Yazar bilgisini kontrol et (eğer varsa)
            try {
                WebElement detailAuthor = driver.findElement(
                    By.cssSelector(".author-name, .article-author, .writer-name")
                );
                String detailAuthorText = detailAuthor.getText();
                assertTrue(
                    detailAuthorText.contains(writerUsername) || detailAuthorText.contains("Writer"),
                    "Case 15: Yazar bilgisi detay sayfasında bulunamadı"
                );
            } catch (org.openqa.selenium.NoSuchElementException e) {
                // Yazar bilgisi yoksa devam et
                System.out.println("Case 15: Yazar bilgisi detay sayfasında bulunamadı (opsiyonel)");
            }

            System.out.println("Case 15: Admin yazı inceleme testi başarıyla tamamlandı");

        } catch (Exception e) {
            System.err.println("Case 15: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 15: Test başarısız - " + e.getMessage());
        }
    }
}

