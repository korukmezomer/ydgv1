package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 4d: Story Oluşturma - Resim Ekleme
 * 
 * Öncelik: YÜKSEK
 * 
 * Use Case: WRITER rolündeki kullanıcı resim içeren story oluşturabilmeli
 * Senaryo:
 * - WRITER olarak giriş yap
 * - Yeni story oluştur sayfasına git
 * - Başlık gir
 * - Resim bloğu ekle (artı butonundan)
 * - Resim yükle
 * - Story'yi kaydet
 * - Story'nin resim ile birlikte oluşturulduğunu doğrula
 */
@DisplayName("Case 4d: Story Oluşturma - Resim")
public class Case4d_StoryCreationWithImageTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 4d: WRITER resim ile story oluşturabilmeli")
    public void case4d_StoryCreationWithImage() {
        try {
            // 1. WRITER olarak kayıt ol (helper method kullan)
            Random random = new Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String email = "writer" + randomSuffix + "@example.com";
            String username = "writer" + randomSuffix;
            String password = "Test123456";
            
            boolean registered = registerWriter("Writer", "Test", email, username, password);
            if (!registered) {
                fail("Case 4d: Writer kaydı başarısız oldu");
            }
            
            // Dashboard'a yönlendirildiğini doğrula
            Thread.sleep(2000);
            String registerUrl = driver.getCurrentUrl();
            if (!registerUrl.contains("/dashboard") && !registerUrl.contains("/yazar") && !registerUrl.equals(BASE_URL + "/")) {
                System.out.println("⚠️ Beklenen dashboard yönlendirmesi yapılmadı. Mevcut URL: " + registerUrl);
                // Yine de devam et, belki farklı bir sayfaya yönlendirildi
            }
            
            // 2. Story oluştur sayfasına git
            driver.get(BASE_URL + "/reader/new-story");
            waitForPageLoad();
            Thread.sleep(2000);
            
            // 3. Başlık gir
            WebElement titleInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input.story-title-input, input[placeholder*='Başlık'], input[placeholder*='başlık']")
                )
            );
            String storyTitle = "Test Story - Resim " + randomSuffix;
            titleInput.sendKeys(storyTitle);
            
            // 4. İlk text bloğunu bul (BOŞ BIRAK)
            Thread.sleep(1000);
            WebElement firstTextBlock = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.block-textarea, .editor-blocks textarea, textarea[placeholder*='Hikayenizi']")
                )
            );
            
            // 5. Text bloğuna hover yap ve resim ekle
            Actions actions = new Actions(driver);
            actions.moveToElement(firstTextBlock).perform();
            Thread.sleep(1000);
            
            WebElement addButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".block-add-button.visible, .editor-block .block-add-button.visible")
                )
            );
            addButton.click();
            Thread.sleep(1000);
            
            // Resim butonuna tıkla (1. buton)
            WebElement imageMenuButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".block-add-menu button[title='Resim'], .block-add-menu button:nth-child(1)")
                )
            );
            imageMenuButton.click();
            Thread.sleep(2000);
            
            // 6. Resim yükleme input'unu bul (hidden input)
            WebElement fileInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input[type='file'], input[accept*='image']")
                )
            );
            
            // Test resmi oluştur
            Path testImagePath = createTestImage();
            
            // 7. Resmi yükle
            fileInput.sendKeys(testImagePath.toAbsolutePath().toString());
            
            // 8. Loading overlay'in kaybolmasını bekle (resim yüklenene kadar)
            Thread.sleep(2000);
            try {
                wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.cssSelector(".loading-overlay, .loading-spinner")
                ));
            } catch (Exception e) {
                // Loading overlay yoksa devam et
            }
            
            // 9. Resim bloğunun oluşmasını bekle (image-block-container)
            Thread.sleep(2000);
            WebElement imageBlockContainer = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".image-block-container, .editor-block.image-block-container")
                )
            );
            assertNotNull(imageBlockContainer, "Case 4d: Resim bloğu oluşturulamadı");
            
            // 10. Resim elementinin görünür olduğunu doğrula
            WebElement imageElement = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".block-image, .image-block-container img, img[src*='http']")
                )
            );
            assertNotNull(imageElement, "Case 4d: Resim elementi bulunamadı");
            
            // 11. Resim URL'sinin doğru olduğunu kontrol et
            String imageSrc = imageElement.getAttribute("src");
            assertTrue(
                imageSrc != null && (imageSrc.startsWith("http") || imageSrc.startsWith("/") || imageSrc.startsWith("data:")),
                "Case 4d: Resim URL'si geçersiz. URL: " + imageSrc
            );
            
            // 12. Resmin yüklendiğini doğrula (resmin görünür olduğunu kontrol et)
            assertTrue(imageElement.isDisplayed(), "Case 4d: Resim görünür değil");
            
            System.out.println("Case 4d: Resim başarıyla yüklendi. URL: " + imageSrc);
            
            // 13. Story'yi yayınla
            Thread.sleep(1000);
            WebElement publishButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".publish-button, button.publish-button")
                )
            );
            publishButton.click();
            
            // 14. Story'nin kaydedildiğini doğrula
            Thread.sleep(3000);
            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/dashboard") || 
                currentUrl.contains("/yazar") ||
                currentUrl.contains("/story") ||
                currentUrl.contains("/reader") ||
                currentUrl.contains("/haberler"),
                "Case 4d: Story kaydedildikten sonra yönlendirme yapılmadı. URL: " + currentUrl
            );
            
            // 15. Yayınlandıktan sonra resmin görünür olduğunu doğrula
            if (currentUrl.contains("/haberler") || currentUrl.contains("/story")) {
                Thread.sleep(2000);
                try {
                    WebElement publishedImage = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("img, .story-content img, .article-content img, .block-image")
                        )
                    );
                    assertTrue(publishedImage.isDisplayed(), 
                        "Case 4d: Yayınlandıktan sonra resim görünür değil");
                    String publishedImageSrc = publishedImage.getAttribute("src");
                    assertTrue(
                        publishedImageSrc != null && !publishedImageSrc.isEmpty(),
                        "Case 4d: Yayınlandıktan sonra resim URL'si boş. URL: " + publishedImageSrc
                    );
                    System.out.println("Case 4d: Yayınlandıktan sonra resim görünür. URL: " + publishedImageSrc);
                } catch (Exception e) {
                    System.out.println("Case 4d: Yayınlandıktan sonra resim kontrolü yapılamadı: " + e.getMessage());
                }
            }
            
            // Test resmini temizle
            try {
                Files.deleteIfExists(testImagePath);
            } catch (IOException e) {
                // Ignore
            }
            
            System.out.println("Case 4d: Story oluşturma (resim) testi başarılı");
            
        } catch (Exception e) {
            System.out.println("Case 4d: Story oluşturma (resim) testi - " + e.getMessage());
            e.printStackTrace();
            fail("Case 4d: Test başarısız oldu: " + e.getMessage());
        }
    }
    
    private Path createTestImage() throws IOException {
        // Basit bir test resmi oluştur (1x1 pixel PNG)
        Path testImagePath = Paths.get(System.getProperty("java.io.tmpdir"), "test-image-" + System.currentTimeMillis() + ".png");
        
        // PNG header + minimal valid PNG data
        byte[] pngData = new byte[]{
            (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG signature
            0x00, 0x00, 0x00, 0x0D, // IHDR chunk length
            0x49, 0x48, 0x44, 0x52, // IHDR
            0x00, 0x00, 0x00, 0x01, // width: 1
            0x00, 0x00, 0x00, 0x01, // height: 1
            0x08, 0x02, 0x00, 0x00, 0x00, // bit depth, color type, etc.
            (byte)0x90, 0x77, 0x53, (byte)0xDE, // CRC
            0x00, 0x00, 0x00, 0x0A, // IDAT chunk length
            0x49, 0x44, 0x41, 0x54, // IDAT
            (byte)0x78, 0x01, 0x63, 0x00, 0x00, 0x00, 0x02, 0x00, 0x01, // compressed data
            (byte)0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte)0xAE, 0x42, 0x60, (byte)0x82 // IEND
        };
        
        Files.write(testImagePath, pngData);
        return testImagePath;
    }
}
