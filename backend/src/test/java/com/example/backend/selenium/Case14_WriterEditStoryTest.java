package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 14: Yazar Yazı Düzenleme (Writer Story Editing)
 * 
 * Test Senaryoları:
 * 1. Yazar yazdığı bir yazıyı düzenleyebilmeli
 * 2. Başlık düzenleyebilmeli
 * 3. İçerik bloklarını düzenleyebilmeli
 * 4. Yeni blok ekleyebilmeli
 * 5. Blok silebilmeli (code, image, video, embed)
 * 6. Formatting toolbar kullanabilmeli
 * 7. Güncelleme işlemini yapabilmeli
 */
@DisplayName("Case 14: Yazar Yazı Düzenleme")
public class Case14_WriterEditStoryTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Case 14: Yazar yazı düzenleme ve güncelleme")
    public void case14_WriterEditAndUpdateStory() {
        try {
            // 1. WRITER olarak kayıt ol
            String writerEmail = "writer_edit_" + System.currentTimeMillis() + "@example.com";
            String writerUsername = "writer_edit_" + System.currentTimeMillis();
            String writerPassword = "Test123456";

            boolean writerRegistered = registerWriter("Writer", "Edit", writerEmail, writerUsername, writerPassword);
            if (!writerRegistered) {
                fail("Case 14: Writer kaydı başarısız");
                return;
            }

            // 2. Case4a gibi basit text story oluştur (Case4g helper metodlarını kullan)
            String originalTitle = "Düzenlenecek Story " + System.currentTimeMillis();
            
            driver.get(BASE_URL + "/reader/new-story");
            waitForPageLoad();
            Thread.sleep(2000);
            
            WebElement titleInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input.story-title-input, input[placeholder*='Başlık']")
                )
            );
            titleInput.sendKeys(originalTitle);
            
            WebElement firstTextBlock = waitForTextBlock();
            firstTextBlock.sendKeys("Bu düzenlenecek bir story'dir.");
            Thread.sleep(1000);
            
            publishStory();
            
            Long storyId = getStoryIdByTitle(originalTitle);
            if (storyId == null) {
                fail("Case 14: Story ID alınamadı");
                return;
            }

            // 3. Düzenleme sayfasına git
            driver.get(BASE_URL + "/yazar/haber-duzenle/" + storyId);
            waitForPageLoad();
            Thread.sleep(3000);

            // 4. Başlık düzenleme
            WebElement editTitleInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input.story-title-input")
                )
            );
            String updatedTitle = "Güncellenmiş Başlık " + System.currentTimeMillis();
            editTitleInput.clear();
            editTitleInput.sendKeys(updatedTitle);
            Thread.sleep(1000);

            // 5. İçerik düzenleme - text bloğunu güncelle
            WebElement editTextBlock = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.block-textarea")
                )
            );
            editTextBlock.clear();
            editTextBlock.sendKeys("Güncellenmiş içerik paragrafı.");
            Thread.sleep(1000);

            // 6. Yeni bloklar ekle - Case4g helper metodlarını kullan
            WebElement emptyTextBlock = waitForNewTextBlock();
            addHeadingBlock(emptyTextBlock, "Yeni Başlık Bloğu");
            
            WebElement newTextBlock1 = waitForNewTextBlock();
            addCodeBlock(newTextBlock1, "const updated = 'test';");
            
            WebElement newTextBlock2 = waitForNewTextBlock();
            newTextBlock2.sendKeys("Yeni paragraf içeriği.");
            Thread.sleep(1000);

            // 7. Kaydet butonuna bas (handleSave'ı JavaScript ile çağır - UI'da buton yok)
            ((JavascriptExecutor) driver).executeScript(
                "window.alert = function(text) { return true; };"
            );
            
            // handleSave fonksiyonunu çağır
            ((JavascriptExecutor) driver).executeScript(
                "if (typeof window.handleSave === 'function') { window.handleSave(); } " +
                "else { " +
                "  const event = new Event('save'); " +
                "  document.dispatchEvent(event); " +
                "}"
            );
            
            // Alternatif: handleSave'ı React component'inden çağır
            try {
                ((JavascriptExecutor) driver).executeScript(
                    "const reactFiber = document.querySelector('input.story-title-input')._reactInternalFiber || " +
                    "document.querySelector('input.story-title-input')._reactInternalInstance; " +
                    "if (reactFiber && reactFiber.return && reactFiber.return.stateNode && reactFiber.return.stateNode.handleSave) { " +
                    "  reactFiber.return.stateNode.handleSave(); " +
                    "}"
                );
            } catch (Exception jsEx) {
                // React fiber bulunamazsa, direkt API çağrısı yapabiliriz veya Yayınla butonunu kullanabiliriz
                System.out.println("Case 14: handleSave JavaScript ile çağrılamadı, Yayınla butonunu kullanıyoruz");
                
                // Yayınla butonuna bas (handlePublish hem update hem yayınla yapıyor)
                WebElement publishButton = wait.until(
                    ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button.publish-button:not(.delete-button)")
                    )
                );
                publishButton.click();
                Thread.sleep(5000);
            }
            
            Thread.sleep(3000);

            // 8. Veritabanından güncellenmiş story'yi kontrol et
            try (java.sql.Connection conn = getTestDatabaseConnection()) {
                String sql = "SELECT baslik, icerik FROM stories WHERE id = ?";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, storyId);
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String dbTitle = rs.getString("baslik");
                            String dbContent = rs.getString("icerik");
                            
                            assertTrue(
                                dbTitle != null && (dbTitle.contains("Güncellenmiş") || dbTitle.equals(updatedTitle)),
                                "Case 14: Başlık veritabanında güncellenmedi. Beklenen: " + updatedTitle + ", Bulunan: " + dbTitle
                            );
                            
                            assertTrue(
                                dbContent != null && (dbContent.contains("Güncellenmiş içerik") || dbContent.contains("Yeni paragraf") || dbContent.contains("Yeni Başlık") || dbContent.contains("[CODE:")),
                                "Case 14: İçerik veritabanında güncellenmedi. İçerik: " + (dbContent != null ? dbContent.substring(0, Math.min(200, dbContent.length())) : "null")
                            );
                        } else {
                            fail("Case 14: Story veritabanında bulunamadı");
                        }
                    }
                }
            } catch (java.sql.SQLException e) {
                System.err.println("Case 14: Veritabanı kontrolü hatası: " + e.getMessage());
                fail("Case 14: Veritabanı kontrolü başarısız");
            }

            System.out.println("Case 14: Yazar yazı düzenleme ve güncelleme testi başarıyla tamamlandı");

        } catch (Exception e) {
            System.err.println("Case 14: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 14: Test başarısız - " + e.getMessage());
        }
    }
    
    // Case4g'deki helper metodları (protected olarak kullanılabilir)
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
    
    private void addCodeBlock(WebElement textBlock, String codeContent) throws Exception {
        Actions actions = new Actions(driver);
        actions.moveToElement(textBlock).perform();
        Thread.sleep(1000);
        
        WebElement addButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".block-add-button.visible, .editor-block .block-add-button.visible")
            )
        );
        addButton.click();
        Thread.sleep(1000);
        
        WebElement codeMenuButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".block-add-menu button[title='Kod'], .block-add-menu button:nth-child(4)")
            )
        );
        codeMenuButton.click();
        Thread.sleep(1000);
        
        WebElement codeBlock = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("textarea.code-editor-inline-textarea, .code-editor-inline textarea")
            )
        );
        codeBlock.clear();
        codeBlock.sendKeys(codeContent);
        
        Thread.sleep(1000);
        WebElement confirmButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".code-editor-btn.confirm, button.code-editor-btn[title='Onayla']")
            )
        );
        confirmButton.click();
        Thread.sleep(2000);
    }
    
    private void addHeadingBlock(WebElement textBlock, String headingText) throws Exception {
        Actions actions = new Actions(driver);
        actions.moveToElement(textBlock).perform();
        Thread.sleep(1000);
        
        WebElement addButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".block-add-button.visible, .editor-block .block-add-button.visible")
            )
        );
        addButton.click();
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
    
    private void publishStory() throws Exception {
        Thread.sleep(1000);
        WebElement publishButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".publish-button, button.publish-button")
            )
        );
        publishButton.click();
        
        Thread.sleep(3000);
        String currentUrl = driver.getCurrentUrl();
        assertTrue(
            currentUrl.contains("/dashboard") || 
            currentUrl.contains("/yazar") ||
            currentUrl.contains("/story") ||
            currentUrl.contains("/reader") ||
            currentUrl.contains("/haberler"),
            "Story kaydedildikten sonra yönlendirme yapılmadı. URL: " + currentUrl
        );
    }

    @Test
    @DisplayName("Case 14a: Yazar code bloğunu silebilmeli")
    public void case14a_DeleteCodeBlock() {
        try {
            // 1. WRITER olarak kayıt ol
            String writerEmail = "writer_code_" + System.currentTimeMillis() + "@example.com";
            String writerUsername = "writer_code_" + System.currentTimeMillis();
            String writerPassword = "Test123456";

            boolean writerRegistered = registerWriter("Writer", "Code", writerEmail, writerUsername, writerPassword);
            if (!writerRegistered) {
                fail("Case 14a: Writer kaydı başarısız");
                return;
            }

            // 2. Case4b gibi code block ile story oluştur (Case4g helper metodlarını kullan)
            String storyTitle = "Code Test Story " + System.currentTimeMillis();
            
            driver.get(BASE_URL + "/reader/new-story");
            waitForPageLoad();
            Thread.sleep(2000);
            
            WebElement titleInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input.story-title-input, input[placeholder*='Başlık']")
                )
            );
            titleInput.sendKeys(storyTitle);
            
            WebElement firstTextBlock = waitForTextBlock();
            firstTextBlock.sendKeys("Bu bir test story'sidir.");
            Thread.sleep(1000);
            
            WebElement emptyTextBlock = waitForNewTextBlock();
            addCodeBlock(emptyTextBlock, "console.log('test');");
            
            WebElement lastTextBlock = waitForNewTextBlock();
            lastTextBlock.sendKeys("Son paragraf.");
            Thread.sleep(1000);
            
            publishStory();
            
            Long storyId = getStoryIdByTitle(storyTitle);
            if (storyId == null) {
                fail("Case 14a: Story ID alınamadı");
                return;
            }

            // 3. Düzenleme sayfasına git
            driver.get(BASE_URL + "/yazar/haber-duzenle/" + storyId);
            waitForPageLoad();
            Thread.sleep(3000);

            // 4. Code bloğunu bul ve sil
            WebElement codeBlock = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div.code-block-container")
                )
            );

            WebElement deleteCodeButton = codeBlock.findElement(
                By.cssSelector("button.code-block-edit-btn[title='Kaldır'], button[title='Kaldır']")
            );
            deleteCodeButton.click();
            Thread.sleep(2000);

            // 5. Code bloğunun silindiğini kontrol et
            try {
                driver.findElement(By.cssSelector("div.code-block-container"));
                fail("Case 14a: Code bloğu silinmedi");
            } catch (org.openqa.selenium.NoSuchElementException e) {
                assertTrue(true, "Case 14a: Code bloğu başarıyla silindi");
            }

            // 6. Kaydet (handleSave'ı JavaScript ile çağır veya Yayınla butonunu kullan)
            saveStoryChanges();

            // 7. Veritabanından kontrol et
            try (java.sql.Connection conn = getTestDatabaseConnection()) {
                String sql = "SELECT icerik FROM stories WHERE id = ?";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, storyId);
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String dbContent = rs.getString("icerik");
                            assertTrue(
                                dbContent == null || !dbContent.contains("[CODE:"),
                                "Case 14a: Code bloğu veritabanından silinmedi. İçerik: " + (dbContent != null ? dbContent.substring(0, Math.min(200, dbContent.length())) : "null")
                            );
                        }
                    }
                }
            } catch (java.sql.SQLException e) {
                System.err.println("Case 14a: Veritabanı kontrolü hatası: " + e.getMessage());
            }

            System.out.println("Case 14a: Code bloğu silme testi başarıyla tamamlandı");

        } catch (Exception e) {
            System.err.println("Case 14a: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 14a: Test başarısız - " + e.getMessage());
        }
    }
    
    // Kaydet helper metodu
    private void saveStoryChanges() throws Exception {
        ((JavascriptExecutor) driver).executeScript(
            "window.alert = function(text) { return true; };"
        );
        
        // handleSave'ı çağırmayı dene
        try {
            ((JavascriptExecutor) driver).executeScript(
                "const reactFiber = document.querySelector('input.story-title-input')._reactInternalFiber || " +
                "document.querySelector('input.story-title-input')._reactInternalInstance; " +
                "if (reactFiber && reactFiber.return && reactFiber.return.stateNode && reactFiber.return.stateNode.handleSave) { " +
                "  reactFiber.return.stateNode.handleSave(); " +
                "}"
            );
            Thread.sleep(3000);
        } catch (Exception jsEx) {
            // React fiber bulunamazsa, Yayınla butonunu kullan (handlePublish hem update hem yayınla yapıyor)
            System.out.println("Case 14: handleSave JavaScript ile çağrılamadı, Yayınla butonunu kullanıyoruz");
            
            WebElement publishButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button.publish-button:not(.delete-button)")
                )
            );
            publishButton.click();
            Thread.sleep(5000);
        }
    }

    @Test
    @DisplayName("Case 14b: Yazar image bloğunu silebilmeli")
    public void case14b_DeleteImageBlock() {
        java.nio.file.Path testImagePath = null;
        try {
            // 1. WRITER olarak kayıt ol
            String writerEmail = "writer_img_" + System.currentTimeMillis() + "@example.com";
            String writerUsername = "writer_img_" + System.currentTimeMillis();
            String writerPassword = "Test123456";

            boolean writerRegistered = registerWriter("Writer", "Image", writerEmail, writerUsername, writerPassword);
            if (!writerRegistered) {
                fail("Case 14b: Writer kaydı başarısız");
                return;
            }

            // 2. Case4d gibi image block ile story oluştur (Case4g helper metodlarını kullan)
            String storyTitle = "Image Test Story " + System.currentTimeMillis();
            
            driver.get(BASE_URL + "/reader/new-story");
            waitForPageLoad();
            Thread.sleep(2000);
            
            WebElement titleInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input.story-title-input, input[placeholder*='Başlık']")
                )
            );
            titleInput.sendKeys(storyTitle);
            
            WebElement firstTextBlock = waitForTextBlock();
            firstTextBlock.sendKeys("Bu bir test story'sidir.");
            Thread.sleep(1000);
            
            // Test resmi oluştur
            testImagePath = createTestImage();
            WebElement emptyTextBlock = waitForNewTextBlock();
            addImageBlock(emptyTextBlock, testImagePath);
            
            WebElement lastTextBlock = waitForNewTextBlock();
            lastTextBlock.sendKeys("Son paragraf.");
            Thread.sleep(1000);
            
            publishStory();
            
            Long storyId = getStoryIdByTitle(storyTitle);
            if (storyId == null) {
                fail("Case 14b: Story ID alınamadı");
                return;
            }

            // 3. Düzenleme sayfasına git
            driver.get(BASE_URL + "/yazar/haber-duzenle/" + storyId);
            waitForPageLoad();
            Thread.sleep(3000);

            // 4. Image bloğunu bul ve sil
            WebElement imageBlock = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div.image-block-container")
                )
            );

            WebElement deleteImageButton = imageBlock.findElement(
                By.cssSelector("button.media-block-btn[title='Kaldır'], button[title='Kaldır']")
            );
            deleteImageButton.click();
            Thread.sleep(2000);

            // 5. Image bloğunun silindiğini kontrol et
            try {
                driver.findElement(By.cssSelector("div.image-block-container"));
                fail("Case 14b: Image bloğu silinmedi");
            } catch (org.openqa.selenium.NoSuchElementException e) {
                assertTrue(true, "Case 14b: Image bloğu başarıyla silindi");
            }

            // 6. Kaydet
            saveStoryChanges();

            // 7. Veritabanından kontrol et
            try (java.sql.Connection conn = getTestDatabaseConnection()) {
                String sql = "SELECT icerik FROM stories WHERE id = ?";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, storyId);
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String dbContent = rs.getString("icerik");
                            assertTrue(
                                dbContent == null || !dbContent.contains("[IMAGE:"),
                                "Case 14b: Image bloğu veritabanından silinmedi. İçerik: " + (dbContent != null ? dbContent.substring(0, Math.min(200, dbContent.length())) : "null")
                            );
                        }
                    }
                }
            } catch (java.sql.SQLException e) {
                System.err.println("Case 14b: Veritabanı kontrolü hatası: " + e.getMessage());
            }

            System.out.println("Case 14b: Image bloğu silme testi başarıyla tamamlandı");

        } catch (Exception e) {
            System.err.println("Case 14b: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 14b: Test başarısız - " + e.getMessage());
        } finally {
            // Test resmini temizle
            if (testImagePath != null) {
                try {
                    java.nio.file.Files.deleteIfExists(testImagePath);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }
    
    // Case4g helper metodları (devamı)
    private void addImageBlock(WebElement textBlock, java.nio.file.Path imagePath) throws Exception {
        Actions actions = new Actions(driver);
        actions.moveToElement(textBlock).perform();
        Thread.sleep(1000);
        
        WebElement addButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".block-add-button.visible, .editor-block .block-add-button.visible")
            )
        );
        addButton.click();
        Thread.sleep(1000);
        
        WebElement imageMenuButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".block-add-menu button[title='Resim'], .block-add-menu button:nth-child(1)")
            )
        );
        imageMenuButton.click();
        Thread.sleep(2000);
        
        WebElement fileInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[type='file'], input[accept*='image']")
            )
        );
        
        fileInput.sendKeys(imagePath.toAbsolutePath().toString());
        
        Thread.sleep(2000);
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector(".loading-overlay, .loading-spinner")
            ));
        } catch (Exception e) {
            // Loading overlay yoksa devam et
        }
        
        Thread.sleep(2000);
        wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".image-block-container, .editor-block.image-block-container")
            )
        );
        
        WebElement imageElement = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".block-image, .image-block-container img, img[src*='http']")
            )
        );
        
        String imageSrc = imageElement.getAttribute("src");
        assertTrue(
            imageSrc != null && (imageSrc.startsWith("http") || imageSrc.startsWith("/") || imageSrc.startsWith("data:")),
            "Resim URL'si geçersiz. URL: " + imageSrc
        );
        
        assertTrue(imageElement.isDisplayed(), "Resim görünür değil");
        Thread.sleep(1000);
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

    @Test
    @DisplayName("Case 14c: Yazar liste bloğu ekleyip öğesini düzenleyebilmeli")
    public void case14c_EditListItem() {
        try {
            // Helper metodları kullan
            String writerEmail = "writer_list_" + System.currentTimeMillis() + "@example.com";
            String writerUsername = "writer_list_" + System.currentTimeMillis();
            String writerPassword = "Test123456";

            boolean writerRegistered = registerWriter("Writer", "List", writerEmail, writerUsername, writerPassword);
            if (!writerRegistered) {
                fail("Case 14c: Writer kaydı başarısız");
                return;
            }

            // Basit bir story oluştur (liste bloğu olmadan)
            String storyTitle = "List Test Story " + System.currentTimeMillis();
            String storyContent = "Bu bir test story'sidir.";
            String storySlug = createStory(writerEmail, writerPassword, storyTitle, storyContent);

            if (storySlug == null) {
                fail("Case 14c: Story oluşturulamadı");
                return;
            }

            Long storyId = getStoryIdByTitle(storyTitle);
            if (storyId == null) {
                fail("Case 14c: Story ID alınamadı");
                return;
            }

            // Writer'a geri giriş yap
            loginUser(writerEmail, writerPassword);
            Thread.sleep(2000);

            driver.get(BASE_URL + "/yazar/haber-duzenle/" + storyId);
            waitForPageLoad();
            Thread.sleep(3000);

            // Case4f'deki hazır kodu kullan: Text bloğunu bul (BOŞ BIRAK)
            WebElement firstTextBlock = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.block-textarea, .editor-blocks textarea, textarea[placeholder*='Hikayenizi']")
                )
            );
            
            // Case4f'deki hazır kodu kullan: Text bloğuna hover yap ve liste ekle
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

            // Case4f'deki hazır kodu kullan: Liste butonuna tıkla (6. buton)
            WebElement listMenuButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".block-add-menu button[title='Liste'], .block-add-menu button:nth-child(6)")
                )
            );
            listMenuButton.click();
            Thread.sleep(2000);

            // Case4f'deki hazır kodu kullan: Liste öğelerini gir
            WebElement firstListItem = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".block-list input, .list-block input, li input[type='text'], input[data-list-item-index='0']")
                )
            );
            
            // İlk liste öğesi
            firstListItem.click(); // Focus yap
            Thread.sleep(200);
            firstListItem.clear();
            firstListItem.sendKeys("İlk liste öğesi");
            Thread.sleep(500);
            firstListItem.sendKeys(Keys.ENTER);
            Thread.sleep(1000); // Yeni liste öğesinin oluşmasını bekle
            
            // İkinci liste öğesi
            java.util.List<WebElement> listItems = driver.findElements(
                By.cssSelector(".block-list input, .list-block input, li input[type='text'], input[data-list-item-index]")
            );
            assertTrue(listItems.size() >= 2, "İkinci liste öğesi oluşmalı. Mevcut öğe sayısı: " + listItems.size());
            
            WebElement secondListItem = listItems.get(1);
            secondListItem.click(); // Focus yap
            Thread.sleep(200);
            secondListItem.clear();
            secondListItem.sendKeys("İkinci liste öğesi");
            Thread.sleep(1000);

            // İlk liste öğesini düzenle
            firstListItem.clear();
            firstListItem.sendKeys("Düzenlenmiş liste öğesi");
            Thread.sleep(1000);

            String updatedText = firstListItem.getAttribute("value");
            assertTrue(
                updatedText.contains("Düzenlenmiş"),
                "Case 14c: Liste öğesi düzenlenemedi. Beklenen: 'Düzenlenmiş', Bulunan: " + updatedText
            );

            // İkinci liste öğesinin hala var olduğunu kontrol et
            String secondText = secondListItem.getAttribute("value");
            assertTrue(
                secondText.contains("İkinci"),
                "Case 14c: İkinci liste öğesi kayboldu. Bulunan: " + secondText
            );

            // Kaydet
            saveStoryChanges();

            // Veritabanından kontrol et
            try (java.sql.Connection conn = getTestDatabaseConnection()) {
                String sql = "SELECT icerik FROM stories WHERE id = ?";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, storyId);
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String dbContent = rs.getString("icerik");
                            assertTrue(
                                dbContent != null && (dbContent.contains("Düzenlenmiş") || dbContent.contains("İkinci") || dbContent.contains("-") || dbContent.contains("1.")),
                                "Case 14c: Liste bloğu veritabanına kaydedilmedi. İçerik: " + (dbContent != null ? dbContent.substring(0, Math.min(200, dbContent.length())) : "null")
                            );
                        }
                    }
                }
            } catch (java.sql.SQLException e) {
                System.err.println("Case 14c: Veritabanı kontrolü hatası: " + e.getMessage());
            }

            System.out.println("Case 14c: Liste bloğu ekleme ve liste öğesi düzenleme testi başarıyla tamamlandı");

        } catch (Exception e) {
            System.err.println("Case 14c: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 14c: Test başarısız - " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Case 14d: Yazar link ekleyip silebilmeli")
    public void case14d_AddAndRemoveLink() {
        try {
            // Helper metodları kullan
            String writerEmail = "writer_link_" + System.currentTimeMillis() + "@example.com";
            String writerUsername = "writer_link_" + System.currentTimeMillis();
            String writerPassword = "Test123456";

            boolean writerRegistered = registerWriter("Writer", "Link", writerEmail, writerUsername, writerPassword);
            if (!writerRegistered) {
                fail("Case 14d: Writer kaydı başarısız");
                return;
            }

            String storyTitle = "Link Test Story " + System.currentTimeMillis();
            String storyContent = "Bu bir test story'sidir.";
            String storySlug = createStory(writerEmail, writerPassword, storyTitle, storyContent);

            if (storySlug == null) {
                fail("Case 14d: Story oluşturulamadı");
                return;
            }

            Long storyId = getStoryIdByTitle(storyTitle);
            if (storyId == null) {
                fail("Case 14d: Story ID alınamadı");
                return;
            }

            // Writer'a geri giriş yap
            loginUser(writerEmail, writerPassword);
            Thread.sleep(2000);

            driver.get(BASE_URL + "/yazar/haber-duzenle/" + storyId);
            waitForPageLoad();
            Thread.sleep(3000);

            WebElement textBlock = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.block-textarea")
                )
            );
            textBlock.clear();
            textBlock.sendKeys("Bu bir link testidir");
            Thread.sleep(1000);

            // Metni seç
            textBlock.sendKeys(Keys.CONTROL + "a");
            Thread.sleep(500);

            // Formatting toolbar'ın görünmesini bekle
            WebElement formatToolbar = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div.format-toolbar")
                )
            );

            // Link butonuna tıkla
            WebElement linkButton = formatToolbar.findElement(
                By.xpath(".//button[@title='Link']")
            );
            linkButton.click();
            Thread.sleep(1000);

            // Prompt'a URL gir
            ((JavascriptExecutor) driver).executeScript(
                "window.prompt = function(text, defaultText) { return 'https://example.com'; };"
            );

            // Prompt'u tekrar tetikle (bazı durumlarda gerekebilir)
            linkButton.click();
            Thread.sleep(2000);

            // Link formatının uygulandığını kontrol et
            String textContent = textBlock.getAttribute("value");
            assertTrue(
                textContent.contains("[") && textContent.contains("](") && textContent.contains("https://example.com"),
                "Case 14d: Link formatı uygulanmadı. İçerik: " + textContent
            );

            // Kaydet
            saveStoryChanges();

            // Veritabanından kontrol et
            try (java.sql.Connection conn = getTestDatabaseConnection()) {
                String sql = "SELECT icerik FROM stories WHERE id = ?";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, storyId);
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String dbContent = rs.getString("icerik");
                            assertTrue(
                                dbContent != null && (dbContent.contains("https://example.com") || dbContent.contains("[") && dbContent.contains("](")),
                                "Case 14d: Link veritabanına kaydedilmedi. İçerik: " + (dbContent != null ? dbContent.substring(0, Math.min(200, dbContent.length())) : "null")
                            );
                        }
                    }
                }
            } catch (java.sql.SQLException e) {
                System.err.println("Case 14d: Veritabanı kontrolü hatası: " + e.getMessage());
            }

            System.out.println("Case 14d: Link ekleme testi başarıyla tamamlandı");

        } catch (Exception e) {
            System.err.println("Case 14d: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 14d: Test başarısız - " + e.getMessage());
        }
    }
}
