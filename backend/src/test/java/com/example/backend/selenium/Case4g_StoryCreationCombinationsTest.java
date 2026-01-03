package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 4g: Story Oluşturma - Farklı Blok Kombinasyonları
 * 
 * Öncelik: YÜKSEK
 * 
 * Use Case: WRITER rolündeki kullanıcı farklı blok kombinasyonları ile story oluşturabilmeli
 * Senaryolar:
 * 1. Kod + Yazı + Link (Gömülü İçerik)
 * 2. Resim + Yazı + Kod
 * 3. Video + Kod + Liste
 * 4. Başlık + Kod + Resim + Yazı
 */
@DisplayName("Case 4g: Story Oluşturma - Blok Kombinasyonları")
@TestMethodOrder(OrderAnnotation.class)
public class Case4g_StoryCreationCombinationsTest extends BaseSeleniumTest {
    
    /**
     * Senaryo 1: Kod + Yazı + Link kombinasyonu
     */
    @Test
    @DisplayName("Case 4g.1: Kod + Yazı + Link kombinasyonu")
    @Order(1)
    public void case4g_1_CodeTextLink() {
        try {
            // 1. WRITER olarak kayıt ol
            String randomSuffix = registerWriter();
            
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
            titleInput.sendKeys("Kod + Yazı + Link " + randomSuffix);
            
            // 4. İlk text bloğunu bul (BOŞ BIRAK)
            WebElement firstTextBlock = waitForTextBlock();
            
            // 5. Kod bloğu ekle
            addCodeBlock(firstTextBlock, "const test = 'Hello';");
            
            // 6. Yeni text bloğuna yazı ekle
            WebElement secondTextBlock = waitForNewTextBlock();
            secondTextBlock.sendKeys("Bu bir test yazısıdır. Kod bloğundan sonra yazı eklendi.");
            secondTextBlock.sendKeys(Keys.END);
            secondTextBlock.sendKeys(Keys.ENTER);
            Thread.sleep(1000);
            
            // 7. Yeni boş text bloğuna link (gömülü içerik) ekle
            WebElement thirdTextBlock = waitForNewTextBlock();
            addEmbedBlock(thirdTextBlock, "https://github.com");
            
            // 8. Story'yi yayınla
            publishStory();
            
            System.out.println("Case 4g.1: Kod + Yazı + Link kombinasyonu başarılı");
            
        } catch (Exception e) {
            System.out.println("Case 4g.1: " + e.getMessage());
            e.printStackTrace();
            fail("Case 4g.1: Test başarısız oldu: " + e.getMessage());
        }
    }
    
    /**
     * Senaryo 2: Resim + Yazı + Kod kombinasyonu
     */
    @Test
    @DisplayName("Case 4g.2: Resim + Yazı + Kod kombinasyonu")
    @Order(2)
    public void case4g_2_ImageTextCode() {
        try {
            // 1. WRITER olarak kayıt ol
            String randomSuffix = registerWriter();
            
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
            titleInput.sendKeys("Resim + Yazı + Kod " + randomSuffix);
            
            // 4. İlk text bloğunu bul (BOŞ BIRAK)
            WebElement firstTextBlock = waitForTextBlock();
            
            // 5. Resim bloğu ekle (test resmi oluştur)
            java.nio.file.Path testImagePath = createTestImage();
            addImageBlock(firstTextBlock, testImagePath);
            
            // 6. Yeni text bloğuna yazı ekle
            WebElement secondTextBlock = waitForNewTextBlock();
            secondTextBlock.sendKeys("Bu bir test yazısıdır. Resim bloğundan sonra yazı eklendi.");
            secondTextBlock.sendKeys(Keys.END);
            secondTextBlock.sendKeys(Keys.ENTER);
            Thread.sleep(1000);
            
            // 7. Yeni boş text bloğuna kod ekle
            WebElement thirdTextBlock = waitForNewTextBlock();
            addCodeBlock(thirdTextBlock, "const imageTest = 'World';");
            
            // 8. Story'yi yayınla
            publishStory();
            
            // Test resmini temizle
            try {
                java.nio.file.Files.deleteIfExists(testImagePath);
            } catch (Exception e) {
                // Ignore
            }
            
            System.out.println("Case 4g.2: Resim + Yazı + Kod kombinasyonu başarılı");
            
        } catch (Exception e) {
            System.out.println("Case 4g.2: " + e.getMessage());
            e.printStackTrace();
            fail("Case 4g.2: Test başarısız oldu: " + e.getMessage());
        }
    }
    
    /**
     * Senaryo 3: Video + Kod + Liste kombinasyonu
     */
    @Test
    @DisplayName("Case 4g.3: Video + Kod + Liste kombinasyonu")
    @Order(3)
    public void case4g_3_VideoCodeList() {
        try {
            // 1. WRITER olarak kayıt ol
            String randomSuffix = registerWriter();
            
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
            titleInput.sendKeys("Video + Kod + Liste " + randomSuffix);
            
            // 4. İlk text bloğunu bul (BOŞ BIRAK)
            WebElement firstTextBlock = waitForTextBlock();
            
            // 5. Video bloğu ekle
            addVideoBlock(firstTextBlock, "https://www.youtube.com/watch?v=dQw4w9WgXcQ");
            
            // 6. Yeni text bloğuna kod ekle
            WebElement secondTextBlock = waitForNewTextBlock();
            addCodeBlock(secondTextBlock, "const videoTest = 'Test';");
            
            // 7. Yeni text bloğuna liste ekle
            WebElement thirdTextBlock = waitForNewTextBlock();
            addListBlock(thirdTextBlock, new String[]{"Liste öğesi 1", "Liste öğesi 2", "Liste öğesi 3"});
            
            // 8. Story'yi yayınla
            publishStory();
            
            System.out.println("Case 4g.3: Video + Kod + Liste kombinasyonu başarılı");
            
        } catch (Exception e) {
            System.out.println("Case 4g.3: " + e.getMessage());
            e.printStackTrace();
            fail("Case 4g.3: Test başarısız oldu: " + e.getMessage());
        }
    }
    
    /**
     * Senaryo 4: Başlık + Kod + Resim + Yazı kombinasyonu
     */
    @Test
    @DisplayName("Case 4g.4: Başlık + Kod + Resim + Yazı kombinasyonu")
    @Order(4)
    public void case4g_4_HeadingCodeImageText() {
        try {
            // 1. WRITER olarak kayıt ol
            String randomSuffix = registerWriter();
            
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
            titleInput.sendKeys("Başlık + Kod + Resim + Yazı " + randomSuffix);
            
            // 4. İlk text bloğunu bul (BOŞ BIRAK)
            WebElement firstTextBlock = waitForTextBlock();
            
            // 5. Başlık bloğu ekle
            addHeadingBlock(firstTextBlock, "Alt Başlık");
            
            // 6. Yeni text bloğuna kod ekle
            WebElement secondTextBlock = waitForNewTextBlock();
            addCodeBlock(secondTextBlock, "const headingTest = 'Code';");
            
            // 7. Yeni text bloğuna resim ekle
            java.nio.file.Path testImagePath = createTestImage();
            WebElement thirdTextBlock = waitForNewTextBlock();
            addImageBlock(thirdTextBlock, testImagePath);
            
            // 8. Yeni text bloğuna yazı ekle
            WebElement fourthTextBlock = waitForNewTextBlock();
            fourthTextBlock.sendKeys("Bu bir test yazısıdır. Başlık, kod ve resim bloğundan sonra yazı eklendi.");
            
            // 9. Story'yi yayınla
            publishStory();
            
            // Test resmini temizle
            try {
                java.nio.file.Files.deleteIfExists(testImagePath);
            } catch (Exception e) {
                // Ignore
            }
            
            System.out.println("Case 4g.4: Başlık + Kod + Resim + Yazı kombinasyonu başarılı");
            
        } catch (Exception e) {
            System.out.println("Case 4g.4: " + e.getMessage());
            e.printStackTrace();
            fail("Case 4g.4: Test başarısız oldu: " + e.getMessage());
        }
    }
    
    // Helper methods
    private String registerWriter() throws Exception {
        Random random = new Random();
        String randomSuffix = String.valueOf(random.nextInt(10000));
        String email = "writer" + randomSuffix + "@example.com";
        String username = "writer" + randomSuffix;
        
        // BaseSeleniumTest'teki registerWriter metodunu kullan (React event'lerini doğru şekilde tetikler)
        boolean registered = registerWriter("Writer", "Test", email, username, "Test123456");
        if (!registered) {
            throw new Exception("Kullanıcı kaydı başarısız oldu");
        }
        
        return randomSuffix;
    }
    
    private WebElement waitForTextBlock() throws Exception {
        Thread.sleep(1000);
        return wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("textarea.block-textarea, .editor-blocks textarea, textarea[placeholder*='Hikayenizi']")
            )
        );
    }
    
    private WebElement waitForNewTextBlock() throws Exception {
        Thread.sleep(2000);
        java.util.List<WebElement> textBlocks = driver.findElements(
            By.cssSelector("textarea.block-textarea")
        );
        return textBlocks.get(textBlocks.size() - 1);
    }
    
    private void addImageBlock(WebElement textBlock, java.nio.file.Path imagePath) throws Exception {
        // Text bloğunun boş olduğundan emin ol (artı butonu sadece boş text bloğunda görünür)
        String textContent = textBlock.getAttribute("value");
        if (textContent != null && !textContent.trim().isEmpty()) {
            // Text bloğu dolu, boşalt
            textBlock.clear();
            Thread.sleep(500);
        }
        
        Actions actions = new Actions(driver);
        actions.moveToElement(textBlock).perform();
        Thread.sleep(1500); // Hover sonrası artı butonunun görünür olması için bekle
        
        // Artı butonunu bul (retry logic ile)
        WebElement addButton = findAddButtonWithRetry(textBlock, actions);
        safeClick(addButton);
        Thread.sleep(1000);
        
        WebElement imageMenuButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".block-add-menu button[title='Resim'], .block-add-menu button:nth-child(1)")
            )
        );
        imageMenuButton.click();
        Thread.sleep(2000);
        
        // File input'u bul
        WebElement fileInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[type='file'], input[accept*='image']")
            )
        );
        
        // Resmi yükle
        fileInput.sendKeys(imagePath.toAbsolutePath().toString());
        
        // Loading overlay'in kaybolmasını bekle
        Thread.sleep(2000);
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector(".loading-overlay, .loading-spinner")
            ));
        } catch (Exception e) {
            // Loading overlay yoksa devam et
        }
        
        // Resim bloğunun oluşmasını bekle (görünürlük yerine presence, renk/beyaz tema sorunları için)
        WebElement imageElement = null;
        for (int i = 0; i < 6 && imageElement == null; i++) {
            try {
                imageElement = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".image-block-container img.block-image, .editor-block.image-block-container img, .image-block img, img.block-image")
                    )
                );
            } catch (Exception ex) {
                Thread.sleep(2000); // yeniden deneme öncesi bekle
            }
        }
        
        if (imageElement == null) {
            fail("Resim elementi oluşmadı (retry sonrası)");
        }
        
        // Resim URL'sinin doğru olduğunu kontrol et (Case 4d'deki gibi görünürlük yerine src kontrolü)
        String imageSrc = imageElement.getAttribute("src");
        assertTrue(
            imageSrc != null && (imageSrc.startsWith("http") || imageSrc.startsWith("/") || imageSrc.startsWith("data:")),
            "Resim URL'si geçersiz. URL: " + imageSrc
        );
        // Bazı temalarda resim beyaz zeminde görünmeyebiliyor; src varlığı yeterli kabul edilir
        System.out.println("Case 4g: Resim yüklendi, src: " + imageSrc);
        Thread.sleep(500);
    }
    
    private void addVideoBlock(WebElement textBlock, String videoUrl) throws Exception {
        // Text bloğunun boş olduğundan emin ol (artı butonu sadece boş text bloğunda görünür)
        String textContent = textBlock.getAttribute("value");
        if (textContent != null && !textContent.trim().isEmpty()) {
            // Text bloğu dolu, boşalt
            textBlock.clear();
            Thread.sleep(500);
        }
        
        Actions actions = new Actions(driver);
        actions.moveToElement(textBlock).perform();
        Thread.sleep(1500); // Hover sonrası artı butonunun görünür olması için bekle
        
        // Artı butonunu bul (retry logic ile)
        WebElement addButton = findAddButtonWithRetry(textBlock, actions);
        safeClick(addButton);
        Thread.sleep(1000);
        
        // Prompt'u ÖNCE override et
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "window.prompt = function() { return '" + videoUrl + "'; }"
        );
        Thread.sleep(500);
        
        WebElement videoMenuButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".block-add-menu button[title='Video'], .block-add-menu button:nth-child(3)")
            )
        );
        videoMenuButton.click();
        Thread.sleep(2000);
        
        // Eğer alert açıldıysa handle et
        try {
            org.openqa.selenium.Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            if (alertText.contains("Video URL") || alertText.contains("URL")) {
                alert.sendKeys(videoUrl);
                alert.accept();
            } else {
                alert.accept();
            }
            Thread.sleep(1000);
        } catch (Exception alertEx) {
            // Alert yoksa devam et
        }
    }
    
    private void addEmbedBlock(WebElement textBlock, String embedUrl) throws Exception {
        // Text bloğunun boş olduğundan emin ol (artı butonu sadece boş text bloğunda görünür)
        String textContent = textBlock.getAttribute("value");
        if (textContent != null && !textContent.trim().isEmpty()) {
            textBlock.clear();
            Thread.sleep(500);
        }
        
        Actions actions = new Actions(driver);
        actions.moveToElement(textBlock).perform();
        Thread.sleep(1500);
        
        // Artı butonunu bul (retry logic ile)
        WebElement addButton = findAddButtonWithRetry(textBlock, actions);
        safeClick(addButton);
        Thread.sleep(1000);
        
        // Prompt'u ÖNCE override et
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "window.prompt = function() { return '" + embedUrl + "'; }"
        );
        Thread.sleep(500);
        
        WebElement embedMenuButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".block-add-menu button[title='Gömülü İçerik'], .block-add-menu button:nth-child(5)")
            )
        );
        embedMenuButton.click();
        Thread.sleep(2000);
        
        // Eğer alert açıldıysa handle et
        try {
            org.openqa.selenium.Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            if (alertText.contains("Embed URL") || alertText.contains("URL")) {
                alert.sendKeys(embedUrl);
                alert.accept();
            } else {
                alert.accept();
            }
            Thread.sleep(1000);
        } catch (Exception alertEx) {
            // Alert yoksa devam et
        }
    }
    
    private void addHeadingBlock(WebElement textBlock, String headingText) throws Exception {
        // Text bloğunun boş olduğundan emin ol (artı butonu sadece boş text bloğunda görünür)
        String textContent = textBlock.getAttribute("value");
        if (textContent != null && !textContent.trim().isEmpty()) {
            textBlock.clear();
            Thread.sleep(500);
        }
        
        Actions actions = new Actions(driver);
        actions.moveToElement(textBlock).perform();
        Thread.sleep(1500);
        
        // Artı butonunu bul (retry logic ile)
        WebElement addButton = findAddButtonWithRetry(textBlock, actions);
        safeClick(addButton);
        Thread.sleep(1000);
        
        WebElement headingMenuButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".block-add-menu button[title='Başlık'], .block-add-menu button:nth-child(2)")
            )
        );
        headingMenuButton.click();
        Thread.sleep(1000);
        
        WebElement headingInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input.block-heading, .block-heading")
            )
        );
        headingInput.sendKeys(headingText);
        Thread.sleep(500);
        headingInput.sendKeys(Keys.ENTER);
        Thread.sleep(1000);
    }
    
    private void addListBlock(WebElement textBlock, String[] items) throws Exception {
        // Text bloğunun boş olduğundan emin ol (artı butonu sadece boş text bloğunda görünür)
        String textContent = textBlock.getAttribute("value");
        if (textContent != null && !textContent.trim().isEmpty()) {
            textBlock.clear();
            Thread.sleep(500);
        }
        
        Actions actions = new Actions(driver);
        actions.moveToElement(textBlock).perform();
        Thread.sleep(1500);
        
        // Artı butonunu bul (retry logic ile)
        WebElement addButton = findAddButtonWithRetry(textBlock, actions);
        safeClick(addButton);
        Thread.sleep(1000);
        
        WebElement listMenuButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".block-add-menu button[title='Liste'], .block-add-menu button:nth-child(6)")
            )
        );
        listMenuButton.click();
        Thread.sleep(2000);
        
        for (int i = 0; i < items.length; i++) {
            // Doğru selector'ları kullan
            java.util.List<WebElement> listInputs = driver.findElements(
                By.cssSelector(".block-list input, .list-block input, li input[type='text'], input[data-list-item-index]")
            );
            assertTrue(listInputs.size() > i, 
                "Liste öğesi " + (i + 1) + " bulunamadı. Mevcut öğe sayısı: " + listInputs.size());
            
            WebElement listInput = listInputs.get(i);
            listInput.click(); // Focus yap
            Thread.sleep(200);
            listInput.clear();
            listInput.sendKeys(items[i]);
            
            if (i < items.length - 1) {
                // Son öğe değilse Enter'a bas (yeni öğe oluştur)
                listInput.sendKeys(Keys.ENTER);
                Thread.sleep(1000); // Yeni öğenin oluşmasını bekle
            } else {
                Thread.sleep(500);
            }
        }
        Thread.sleep(1000);
    }
    
    // Helper method: Artı butonunu retry logic ile bul
    private WebElement findAddButtonWithRetry(WebElement textBlock, Actions actions) throws Exception {
        WebElement addButton = null;
        int retryCount = 0;
        int maxRetries = 6;
        while (retryCount < maxRetries && addButton == null) {
            try {
                // Önce tüm artı butonlarını bul
                java.util.List<WebElement> addButtons = driver.findElements(
                    By.cssSelector(".block-add-button, .editor-block .block-add-button")
                );
                
                // Görünür olanı bul
                for (WebElement btn : addButtons) {
                    try {
                        if (btn.isDisplayed() && btn.isEnabled()) {
                            addButton = btn;
                            break;
                        }
                    } catch (Exception e) {
                        // Element artık DOM'da yok, devam et
                    }
                }
                
                if (addButton == null) {
                    // Hover'ı tekrar yap
                    actions.moveToElement(textBlock).perform();
                    Thread.sleep(1000);
                    retryCount++;
                }
            } catch (Exception e) {
                retryCount++;
                Thread.sleep(1000);
            }
        }
        
        if (addButton == null) {
            throw new Exception("Artı butonu bulunamadı (retry sonrası)");
        }
        
        return addButton;
    }
    
    private java.nio.file.Path createTestImage() throws Exception {
        java.nio.file.Path testImagePath = java.nio.file.Paths.get(
            System.getProperty("java.io.tmpdir"), 
            "test-image-" + System.currentTimeMillis() + ".png"
        );
        
        byte[] pngData = new byte[]{
            (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x02, 0x00, 0x00, 0x00, (byte)0x90, 0x77, 0x53, (byte)0xDE,
            0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54,
            (byte)0x78, 0x01, 0x63, 0x00, 0x00, 0x00, 0x02, 0x00, 0x01,
            (byte)0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte)0xAE, 0x42, 0x60, (byte)0x82
        };
        
        java.nio.file.Files.write(testImagePath, pngData);
        return testImagePath;
    }
}

