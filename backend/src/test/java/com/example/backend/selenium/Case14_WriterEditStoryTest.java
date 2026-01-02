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
 * Senaryo:
 * 1. Yazar giriş yapar
 * 2. Yazı oluşturur ve yayınlar
 * 3. Düzenleme sayfasına gider
 * 4. Başlık ve içeriği düzenler
 * 5. Kod bloğu ekler/düzenler/siler
 * 6. Liste bloğu ekler/düzenler
 * 7. Değişiklikleri kaydeder
 * 8. Veritabanından düzenlenmiş mi kontrol eder
 */
@DisplayName("Case 14: Yazar Yazı Düzenleme")
public class Case14_WriterEditStoryTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Case 14: Yazar yazı düzenleme ve güncelleme")
    public void case14_WriterEditAndUpdateStory() {
        try {
            // 1. WRITER olarak kayıt ol ve giriş yap
            String writerEmail = "writer_edit_" + System.currentTimeMillis() + "@example.com";
            String writerUsername = "writer_edit_" + System.currentTimeMillis();
            String writerPassword = "Test123456";

            boolean writerRegistered = registerWriter("Writer", "Edit", writerEmail, writerUsername, writerPassword);
            if (!writerRegistered) {
                fail("Case 14: Writer kaydı başarısız");
                return;
            }

            // Writer zaten giriş yapmış durumda (registerWriter sonrası)
            // Eğer değilse giriş yap
            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains("/dashboard") && !currentUrl.contains("/yazar/") && !currentUrl.contains("/reader/")) {
                loginUser(writerEmail, writerPassword);
            }

            // 2. Story oluştur ve yayınla (createStory helper metodunu kullan)
            String originalTitle = "Düzenlenecek Story " + System.currentTimeMillis();
            String storyContent = "Bu düzenlenecek bir story'dir.";
            String storySlug = createStory(writerEmail, writerPassword, originalTitle, storyContent);

            if (storySlug == null) {
                fail("Case 14: Story oluşturulamadı");
                return;
            }

            // Story ID'yi al (retry logic ile)
            Long storyId = getStoryIdByTitle(originalTitle, writerEmail);
            if (storyId == null) {
                // Son çare: Kullanıcının en son story'sini al
                storyId = getLatestStoryIdByUserEmail(writerEmail);
            }

            if (storyId == null) {
                fail("Case 14: Story ID alınamadı");
                return;
            }

            // Writer'a tekrar giriş yap (createStory logout yapmış olabilir)
            loginUser(writerEmail, writerPassword);

            // 3. Düzenleme sayfasına git
            driver.get(BASE_URL + "/yazar/haber-duzenle/" + storyId);
            waitForPageLoad();
            Thread.sleep(2000);

            // 4. Başlık düzenleme
            WebElement editTitleInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input.story-title-input")
                )
            );
            String updatedTitle = "Güncellenmiş Başlık " + System.currentTimeMillis();
            editTitleInput.clear();
            editTitleInput.sendKeys(updatedTitle);
            // React onChange ve input event'lerini tetikle (value property'sini de güncelle)
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]; " +
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true })); " +
                "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", 
                editTitleInput, updatedTitle);
            Thread.sleep(1500);

            // 5. İçerik düzenleme - text bloğunu güncelle
            WebElement editTextBlock = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.block-textarea")
                )
            );
            String updatedContent = "Güncellenmiş içerik paragrafı.";
            editTextBlock.clear();
            editTextBlock.sendKeys(updatedContent);
            // React onChange ve input event'lerini tetikle (value property'sini de güncelle)
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]; " +
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true })); " +
                "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", 
                editTextBlock, updatedContent);
            Thread.sleep(1500);
            
            // React state'inin güncellenmesi için ek bekleme
            Thread.sleep(1000);

            // 6. Yayınla butonunu bul ve bas (header içindeki publish-button)
            // Alert ve confirm'i override et
            ((JavascriptExecutor) driver).executeScript(
                "window.alert = function(text) { console.log('Alert: ' + text); return true; };"
            );
            ((JavascriptExecutor) driver).executeScript(
                "window.confirm = function(text) { console.log('Confirm: ' + text); return true; };"
            );
            
            Thread.sleep(1000);
            
            // Header içindeki Yayınla butonunu bul (delete-button olmayan)
            WebElement publishButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".story-header-actions .publish-button:not(.delete-button), " +
                                  "header .publish-button:not(.delete-button), " +
                                  "button.publish-button:not(.delete-button)")
                )
            );
            
            // Butonun disabled olmadığından emin ol
            String disabledAttr = publishButton.getAttribute("disabled");
            if (disabledAttr != null && !disabledAttr.equals("false")) {
                System.out.println("Yayınla butonu disabled, başlık kontrolü yapılıyor...");
                // Başlık değerini tekrar kontrol et
                String titleValue = editTitleInput.getAttribute("value");
                if (titleValue == null || titleValue.trim().isEmpty()) {
                    editTitleInput.sendKeys("Test Başlık " + System.currentTimeMillis());
                    ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", editTitleInput);
                    Thread.sleep(1000);
                }
                // Butonu tekrar bul
                publishButton = wait.until(
                    ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".story-header-actions .publish-button:not(.delete-button)")
                    )
                );
            }
            
            // Butonun görünür olduğundan emin ol
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", publishButton);
            Thread.sleep(500);
            
            // Butona tıkla
            try {
                publishButton.click();
            } catch (Exception e) {
                // Normal click başarısız olursa JavaScript click
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", publishButton);
            }
            
            Thread.sleep(5000); // Yayınlama işlemi için bekle
            
            // Alert'leri kontrol et ve kabul et
            try {
                org.openqa.selenium.Alert alert = driver.switchTo().alert();
                String alertText = alert.getText();
                System.out.println("Yayınla sonrası alert: " + alertText);
                alert.accept();
                Thread.sleep(2000);
            } catch (Exception alertEx) {
                // Alert yoksa devam et
            }
            
            waitForPageLoad();
            Thread.sleep(3000);

            // 7. API üzerinden güncellenmiş story'yi kontrol et
            String apiTitle = getStoryTitleViaApi(storyId);
            String apiContent = getStoryContentViaApi(storyId);
                            
                            assertTrue(
                apiTitle != null && apiTitle.contains("Güncellenmiş"),
                "Case 14: Başlık API'de güncellenmedi. Beklenen: " + updatedTitle + ", Bulunan: " + apiTitle
                            );
                            
                            assertTrue(
                apiContent != null && apiContent.contains("Güncellenmiş içerik"),
                "Case 14: İçerik API'de güncellenmedi. İçerik: " + (apiContent != null ? apiContent.substring(0, Math.min(200, apiContent.length())) : "null")
                            );

            System.out.println("Case 14: Yazar yazı düzenleme ve güncelleme testi başarıyla tamamlandı");

        } catch (Exception e) {
            System.err.println("Case 14: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 14: Test başarısız - " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Case 14a: Yazar kod bloğunu ekleyip silebilmeli")
    public void case14a_AddAndDeleteCodeBlock() {
        try {
            // 1. WRITER olarak kayıt ol ve giriş yap
            String writerEmail = "writer_code_" + System.currentTimeMillis() + "@example.com";
            String writerUsername = "writer_code_" + System.currentTimeMillis();
            String writerPassword = "Test123456";

            boolean writerRegistered = registerWriter("Writer", "Code", writerEmail, writerUsername, writerPassword);
            if (!writerRegistered) {
                fail("Case 14a: Writer kaydı başarısız");
                return;
            }

            // Writer zaten giriş yapmış durumda
            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains("/dashboard") && !currentUrl.contains("/yazar/") && !currentUrl.contains("/reader/")) {
                loginUser(writerEmail, writerPassword);
            }

            // 2. Basit story oluştur
            String storyTitle = "Code Test Story " + System.currentTimeMillis();
            String storyContent = "Bu bir test story'sidir.";
            String storySlug = createStory(writerEmail, writerPassword, storyTitle, storyContent);

            if (storySlug == null) {
                fail("Case 14a: Story oluşturulamadı");
                return;
            }

            // Story ID'yi al (retry logic ile)
            Long storyId = getStoryIdByTitle(storyTitle, writerEmail);
            if (storyId == null) {
                storyId = getLatestStoryIdByUserEmail(writerEmail);
            }

            if (storyId == null) {
                fail("Case 14a: Story ID alınamadı");
                return;
            }

            // Writer'a tekrar giriş yap
            loginUser(writerEmail, writerPassword);

            // 3. Düzenleme sayfasına git
            driver.get(BASE_URL + "/yazar/haber-duzenle/" + storyId);
            waitForPageLoad();
            Thread.sleep(2000);

            // 4. Kod bloğu ekle (Case4b'deki mantığı kullan - boş text bloğu bul ve hover yap)
            // Önce tüm textarea'ları bul
            java.util.List<WebElement> allTextBlocks = driver.findElements(
                By.cssSelector("textarea.block-textarea")
            );
            
            // Boş text bloğu bul (içeriği boş olan)
            WebElement emptyTextBlock = null;
            for (WebElement block : allTextBlocks) {
                String content = block.getAttribute("value");
                if (content == null || content.trim().isEmpty()) {
                    emptyTextBlock = block;
                    break;
                }
            }
            
            // Eğer boş text bloğu yoksa, Enter tuşuna basarak yeni bir boş text bloğu oluştur
            if (emptyTextBlock == null && !allTextBlocks.isEmpty()) {
                WebElement firstTextBlock = allTextBlocks.get(0);
                firstTextBlock.click();
                Thread.sleep(500);
                // Cursor'ı sona taşı
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].setSelectionRange(arguments[0].value.length, arguments[0].value.length);", 
                    firstTextBlock);
                Thread.sleep(500);
                // Enter tuşuna bas (yeni text bloğu oluştur)
                firstTextBlock.sendKeys(Keys.ENTER);
                Thread.sleep(2000); // Yeni bloğun oluşması için bekle
                
                // Yeni oluşan boş text bloğunu bul
                allTextBlocks = driver.findElements(By.cssSelector("textarea.block-textarea"));
                for (WebElement block : allTextBlocks) {
                    String content = block.getAttribute("value");
                    if (content == null || content.trim().isEmpty()) {
                        emptyTextBlock = block;
                        break;
                    }
                }
            }
            
            if (emptyTextBlock == null) {
                fail("Case 14a: Boş text bloğu bulunamadı");
                return;
            }
            
            // Boş text bloğuna hover yap (artı butonunu görünür yapmak için)
            Actions actions = new Actions(driver);
            actions.moveToElement(emptyTextBlock).perform();
            Thread.sleep(1000);
            
            // JavaScript ile hover event'ini tetikle (React'ın hover state'ini güncellemek için)
            ((JavascriptExecutor) driver).executeScript(
                "var event = new MouseEvent('mouseenter', { bubbles: true, cancelable: true }); " +
                "arguments[0].dispatchEvent(event);", emptyTextBlock);
            Thread.sleep(500);
            
            // '+' butonunu bul ve tıkla (block-add-button) - sadece boş text bloğunda görünür
            WebElement addButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".block-add-button.visible, .editor-block .block-add-button.visible")
                )
            );
            addButton.click();
            Thread.sleep(1000);
            
            // Menü açıldıktan sonra kod butonunu bul ve tıkla
            // Menü butonları: Resim (1.), Başlık (2.), Video (3.), Kod (4.), Gömülü İçerik (5.), Liste (6.)
            WebElement codeMenuButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".block-add-menu button[title='Kod'], .block-add-menu button:nth-child(4)")
                )
            );
            codeMenuButton.click();
            Thread.sleep(1000);
            
            // Kod bloğu editörü açıldı - kod içeriğini gir
            WebElement codeBlock = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.code-editor-inline-textarea, .code-editor-inline textarea")
                )
            );
            String codeContent = "console.log('test');";
            codeBlock.clear();
            codeBlock.sendKeys(codeContent);
            
            // Kod bloğunu onayla (onay butonu - checkmark icon)
            Thread.sleep(1000);
            WebElement confirmButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".code-editor-btn.confirm, button.code-editor-btn[title='Onayla'], " +
                        ".code-editor-inline-actions button.confirm")
                )
            );
            confirmButton.click();
            Thread.sleep(2000);

            // 5. Kod bloğunun eklendiğini kontrol et
            WebElement codeBlockContainer = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div.code-block-container")
                )
            );
            assertNotNull(codeBlockContainer, "Case 14a: Kod bloğu eklenmedi");
            System.out.println("Case 14a: Kod bloğu başarıyla eklendi");

            // 6. Yayınla (kaydet ve yayınla)
            // Alert ve confirm'i override et
            ((JavascriptExecutor) driver).executeScript(
                "window.alert = function(text) { console.log('Alert: ' + text); return true; };"
            );
            ((JavascriptExecutor) driver).executeScript(
                "window.confirm = function(text) { console.log('Confirm: ' + text); return true; };"
            );
            
            Thread.sleep(1000);
            
            // Header içindeki Yayınla butonunu bul
            WebElement publishButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".story-header-actions .publish-button:not(.delete-button), " +
                                  "header .publish-button:not(.delete-button), " +
                                  "button.publish-button:not(.delete-button)")
                )
            );
            
            // Butonun disabled olmadığından emin ol
            String disabledAttr = publishButton.getAttribute("disabled");
            if (disabledAttr != null && !disabledAttr.equals("false")) {
                System.out.println("Yayınla butonu disabled, başlık kontrolü yapılıyor...");
                WebElement titleInput = driver.findElement(By.cssSelector("input.story-title-input"));
                String titleValue = titleInput.getAttribute("value");
                if (titleValue == null || titleValue.trim().isEmpty()) {
                    titleInput.sendKeys("Test Başlık " + System.currentTimeMillis());
                    ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", titleInput);
                    Thread.sleep(1000);
                }
                publishButton = wait.until(
                    ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".story-header-actions .publish-button:not(.delete-button)")
                    )
                );
            }
            
            // Butonun görünür olduğundan emin ol
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", publishButton);
            Thread.sleep(500);
            
            // Butona tıkla
            try {
                publishButton.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", publishButton);
            }
            
            Thread.sleep(5000); // Yayınlama işlemi için bekle
            
            // Alert'leri kontrol et ve kabul et
            try {
                org.openqa.selenium.Alert alert = driver.switchTo().alert();
                String alertText = alert.getText();
                System.out.println("Yayınla sonrası alert: " + alertText);
                alert.accept();
                Thread.sleep(2000);
            } catch (Exception alertEx) {
                // Alert yoksa devam et
            }
            
            waitForPageLoad();
            Thread.sleep(3000);
            
            // Yayınlama sonrası story sayfasına yönlendirilmiş olabilir, tekrar düzenleme sayfasına git
            String publishUrl = driver.getCurrentUrl();
            System.out.println("Case 14a: Yayınlama sonrası URL: " + publishUrl);
            
            // Eğer story sayfasındaysak, tekrar düzenleme sayfasına git
            if (publishUrl.contains("/haberler/") || publishUrl.contains("/story/")) {
                // Writer'a tekrar giriş yap (gerekirse)
                loginUser(writerEmail, writerPassword);
                Thread.sleep(2000);
            }
            
            // Tekrar düzenleme sayfasına git
            driver.get(BASE_URL + "/yazar/haber-duzenle/" + storyId);
            waitForPageLoad();
            Thread.sleep(3000);

            // 7. Kod bloğunu bul ve sil
            WebElement codeBlockContainerToDelete = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div.code-block-container")
                )
            );
            assertNotNull(codeBlockContainerToDelete, "Case 14a: Kod bloğu düzenleme sayfasında bulunamadı");
            
            // Kaldır butonunu bul (code-block-actions içindeki ikinci buton - silme butonu)
            WebElement deleteCodeButton = codeBlockContainerToDelete.findElement(
                By.cssSelector(".code-block-actions button.code-block-edit-btn[title='Kaldır'], " +
                              ".code-block-actions button[title='Kaldır'], " +
                              ".code-block-actions button:last-child")
            );
            
            // Butonun görünür olduğundan emin ol
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", deleteCodeButton);
            Thread.sleep(500);
            
            deleteCodeButton.click();
            Thread.sleep(2000);

            // 8. Kod bloğunun silindiğini kontrol et
            try {
                driver.findElement(By.cssSelector("div.code-block-container"));
                fail("Case 14a: Kod bloğu silinmedi");
            } catch (org.openqa.selenium.NoSuchElementException e) {
                assertTrue(true, "Case 14a: Kod bloğu başarıyla silindi");
                System.out.println("Case 14a: Kod bloğu başarıyla silindi");
            }

            // 9. Tekrar yayınla (kod bloğu silindikten sonra)
            Thread.sleep(1000);
            
            WebElement publishButtonAfterDelete = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".story-header-actions .publish-button:not(.delete-button), " +
                                  "header .publish-button:not(.delete-button), " +
                                  "button.publish-button:not(.delete-button)")
                )
            );
            
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", publishButtonAfterDelete);
            Thread.sleep(500);
            
            try {
                publishButtonAfterDelete.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", publishButtonAfterDelete);
            }
            
            Thread.sleep(5000);
            
            // Alert'leri kontrol et ve kabul et
            try {
                org.openqa.selenium.Alert alert = driver.switchTo().alert();
                alert.accept();
                Thread.sleep(2000);
            } catch (Exception alertEx) {
                // Alert yoksa devam et
            }
            
            waitForPageLoad();
            Thread.sleep(3000);

            // 10. API üzerinden kontrol et - kod bloğu silinmiş olmalı
            String apiContentAfterDelete = getStoryContentViaApi(storyId);
                            assertTrue(
                apiContentAfterDelete == null || !apiContentAfterDelete.contains("[CODE:"),
                "Case 14a: Kod bloğu API'de silinmedi. İçerik: " + 
                (apiContentAfterDelete != null ? apiContentAfterDelete.substring(0, Math.min(200, apiContentAfterDelete.length())) : "null")
                            );
            System.out.println("Case 14a: API kontrolü başarılı - kod bloğu silinmiş");

            System.out.println("Case 14a: Kod bloğu ekleme ve silme testi başarıyla tamamlandı");

        } catch (Exception e) {
            System.err.println("Case 14a: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 14a: Test başarısız - " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Case 14b: Yazar liste bloğu ekleyip öğesini düzenleyebilmeli")
    public void case14b_AddAndEditListBlock() {
        try {
            // 1. WRITER olarak kayıt ol ve giriş yap
            String writerEmail = "writer_list_" + System.currentTimeMillis() + "@example.com";
            String writerUsername = "writer_list_" + System.currentTimeMillis();
            String writerPassword = "Test123456";

            boolean writerRegistered = registerWriter("Writer", "List", writerEmail, writerUsername, writerPassword);
            if (!writerRegistered) {
                fail("Case 14b: Writer kaydı başarısız");
                return;
            }

            // Writer zaten giriş yapmış durumda
            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains("/dashboard") && !currentUrl.contains("/yazar/") && !currentUrl.contains("/reader/")) {
                loginUser(writerEmail, writerPassword);
            }

            // 2. Basit story oluştur
            String storyTitle = "List Test Story " + System.currentTimeMillis();
            String storyContent = "Bu bir test story'sidir.";
            String storySlug = createStory(writerEmail, writerPassword, storyTitle, storyContent);

            if (storySlug == null) {
                fail("Case 14b: Story oluşturulamadı");
                return;
            }

            // Story ID'yi al (retry logic ile)
            Long storyId = getStoryIdByTitle(storyTitle, writerEmail);
            if (storyId == null) {
                storyId = getLatestStoryIdByUserEmail(writerEmail);
            }

            if (storyId == null) {
                fail("Case 14b: Story ID alınamadı");
                return;
            }

            // Writer'a tekrar giriş yap
            loginUser(writerEmail, writerPassword);

            // 3. Düzenleme sayfasına git
            driver.get(BASE_URL + "/yazar/haber-duzenle/" + storyId);
            waitForPageLoad();
            Thread.sleep(2000);

            // 4. Liste bloğu ekle (Case4f'deki mantığı kullan - boş text bloğu bul ve hover yap)
            // Önce tüm textarea'ları bul
            java.util.List<WebElement> allTextBlocks = driver.findElements(
                By.cssSelector("textarea.block-textarea")
            );
            
            // Boş text bloğu bul (içeriği boş olan)
            WebElement emptyTextBlock = null;
            for (WebElement block : allTextBlocks) {
                String content = block.getAttribute("value");
                if (content == null || content.trim().isEmpty()) {
                    emptyTextBlock = block;
                    break;
                }
            }
            
            // Eğer boş text bloğu yoksa, Enter tuşuna basarak yeni bir boş text bloğu oluştur
            if (emptyTextBlock == null && !allTextBlocks.isEmpty()) {
                WebElement firstTextBlock = allTextBlocks.get(0);
                firstTextBlock.click();
                Thread.sleep(500);
                // Cursor'ı sona taşı
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].setSelectionRange(arguments[0].value.length, arguments[0].value.length);", 
                    firstTextBlock);
                Thread.sleep(500);
                // Enter tuşuna bas (yeni text bloğu oluştur)
                firstTextBlock.sendKeys(Keys.ENTER);
                Thread.sleep(2000); // Yeni bloğun oluşması için bekle
                
                // Yeni oluşan boş text bloğunu bul
                allTextBlocks = driver.findElements(By.cssSelector("textarea.block-textarea"));
                for (WebElement block : allTextBlocks) {
                    String content = block.getAttribute("value");
                    if (content == null || content.trim().isEmpty()) {
                        emptyTextBlock = block;
                        break;
                    }
                }
            }
            
            if (emptyTextBlock == null) {
                fail("Case 14b: Boş text bloğu bulunamadı");
                return;
            }

            // Boş text bloğuna hover yap (artı butonunu görünür yapmak için)
            Actions actions = new Actions(driver);
            actions.moveToElement(emptyTextBlock).perform();
            Thread.sleep(1000);
            
            // JavaScript ile hover event'ini tetikle (React'ın hover state'ini güncellemek için)
            ((JavascriptExecutor) driver).executeScript(
                "var event = new MouseEvent('mouseenter', { bubbles: true, cancelable: true }); " +
                "arguments[0].dispatchEvent(event);", emptyTextBlock);
            Thread.sleep(500);

            // + butonuna tıkla
            WebElement addButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".block-add-button.visible, .editor-block .block-add-button.visible")
                )
            );
            addButton.click();
            Thread.sleep(1000);

            // Liste butonuna tıkla (6. buton)
            WebElement listMenuButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".block-add-menu button[title='Liste'], .block-add-menu button:nth-child(6)")
                )
            );
            listMenuButton.click();
            Thread.sleep(2000);

            // Liste öğelerini gir
            WebElement firstListItem = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".block-list input, .list-block input, li input[type='text'], input[data-list-item-index='0']")
                )
            );

            // İlk liste öğesi
            firstListItem.click();
            Thread.sleep(200);
            firstListItem.clear();
            firstListItem.sendKeys("İlk liste öğesi");
            Thread.sleep(500);
            firstListItem.sendKeys(Keys.ENTER);
            Thread.sleep(1000);

            // İkinci liste öğesi
            java.util.List<WebElement> listItems = driver.findElements(
                By.cssSelector(".block-list input, .list-block input, li input[type='text'], input[data-list-item-index]")
            );
            assertTrue(listItems.size() >= 2, "İkinci liste öğesi oluşmalı");

            WebElement secondListItem = listItems.get(1);
            secondListItem.click();
            Thread.sleep(200);
            secondListItem.clear();
            secondListItem.sendKeys("İkinci liste öğesi");
            Thread.sleep(1000);

            // 5. İlk liste öğesini düzenle
            firstListItem.clear();
            firstListItem.sendKeys("Düzenlenmiş liste öğesi");
            Thread.sleep(1000);

            String updatedText = firstListItem.getAttribute("value");
            assertTrue(
                updatedText.contains("Düzenlenmiş"),
                "Case 14b: Liste öğesi düzenlenemedi. Bulunan: " + updatedText
            );

            // 6. Kaydet ve yayınla
            publishStory();

            // 7. API üzerinden kontrol et
            String apiContentAfterList = getStoryContentViaApi(storyId);
                            assertTrue(
                apiContentAfterList != null && (apiContentAfterList.contains("Düzenlenmiş") || apiContentAfterList.contains("İkinci") || apiContentAfterList.contains("-") || apiContentAfterList.contains("1.")),
                "Case 14b: Liste bloğu API'ye kaydedilmedi"
                            );

            System.out.println("Case 14b: Liste bloğu ekleme ve düzenleme testi başarıyla tamamlandı");

        } catch (Exception e) {
            System.err.println("Case 14b: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 14b: Test başarısız - " + e.getMessage());
        }
    }
}
