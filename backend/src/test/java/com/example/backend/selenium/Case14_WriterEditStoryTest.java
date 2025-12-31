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

            // Story ID'yi al
            Long storyId = getStoryIdByTitle(originalTitle);
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

            // 6. Yayınla butonuna bas (kaydet ve yayınla)
            publishStory();

            // 7. Veritabanından güncellenmiş story'yi kontrol et
            try (java.sql.Connection conn = getTestDatabaseConnection()) {
                String sql = "SELECT baslik, icerik FROM stories WHERE id = ?";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, storyId);
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String dbTitle = rs.getString("baslik");
                            String dbContent = rs.getString("icerik");
                            
                            assertTrue(
                                dbTitle != null && dbTitle.contains("Güncellenmiş"),
                                "Case 14: Başlık veritabanında güncellenmedi. Beklenen: " + updatedTitle + ", Bulunan: " + dbTitle
                            );
                            
                            assertTrue(
                                dbContent != null && dbContent.contains("Güncellenmiş içerik"),
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

            Long storyId = getStoryIdByTitle(storyTitle);
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

            // 4. Kod bloğu ekle (Case4g'deki mantığı kullan)
            WebElement textBlock = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.block-textarea")
                )
            );

            // Text bloğuna hover yap
            Actions actions = new Actions(driver);
            actions.moveToElement(textBlock).perform();
            Thread.sleep(1000);

            // + butonuna tıkla
            WebElement addButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".block-add-button.visible, .editor-block .block-add-button.visible")
                )
            );
            addButton.click();
            Thread.sleep(1000);

            // Kod butonuna tıkla
            WebElement codeMenuButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".block-add-menu button[title='Kod'], .block-add-menu button:nth-child(4)")
                )
            );
            codeMenuButton.click();
            Thread.sleep(1000);

            // Kod bloğunu doldur
            WebElement codeBlock = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.code-editor-inline-textarea, .code-editor-inline textarea")
                )
            );
            codeBlock.clear();
            codeBlock.sendKeys("console.log('test');");
            Thread.sleep(1000);

            // Onayla butonuna tıkla
            WebElement confirmButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".code-editor-btn.confirm, button.code-editor-btn[title='Onayla']")
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

            // 6. Kod bloğunu sil
            WebElement deleteCodeButton = codeBlockContainer.findElement(
                By.cssSelector("button.code-block-edit-btn[title='Kaldır'], button[title='Kaldır']")
            );
            deleteCodeButton.click();
            Thread.sleep(2000);

            // 7. Kod bloğunun silindiğini kontrol et
            try {
                driver.findElement(By.cssSelector("div.code-block-container"));
                fail("Case 14a: Kod bloğu silinmedi");
            } catch (org.openqa.selenium.NoSuchElementException e) {
                assertTrue(true, "Case 14a: Kod bloğu başarıyla silindi");
            }

            // 8. Kaydet ve yayınla
            publishStory();

            // 9. Veritabanından kontrol et
            try (java.sql.Connection conn = getTestDatabaseConnection()) {
                String sql = "SELECT icerik FROM stories WHERE id = ?";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, storyId);
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String dbContent = rs.getString("icerik");
                            assertTrue(
                                dbContent == null || !dbContent.contains("[CODE:"),
                                "Case 14a: Kod bloğu veritabanından silinmedi"
                            );
                        }
                    }
                }
            } catch (java.sql.SQLException e) {
                System.err.println("Case 14a: Veritabanı kontrolü hatası: " + e.getMessage());
            }

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

            Long storyId = getStoryIdByTitle(storyTitle);
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

            // 4. Liste bloğu ekle (Case4f'deki mantığı kullan)
            WebElement firstTextBlock = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.block-textarea")
                )
            );

            // Text bloğuna hover yap
            Actions actions = new Actions(driver);
            actions.moveToElement(firstTextBlock).perform();
            Thread.sleep(1000);

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

            // 7. Veritabanından kontrol et
            try (java.sql.Connection conn = getTestDatabaseConnection()) {
                String sql = "SELECT icerik FROM stories WHERE id = ?";
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, storyId);
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String dbContent = rs.getString("icerik");
                            assertTrue(
                                dbContent != null && (dbContent.contains("Düzenlenmiş") || dbContent.contains("İkinci") || dbContent.contains("-") || dbContent.contains("1.")),
                                "Case 14b: Liste bloğu veritabanına kaydedilmedi"
                            );
                        }
                    }
                }
            } catch (java.sql.SQLException e) {
                System.err.println("Case 14b: Veritabanı kontrolü hatası: " + e.getMessage());
            }

            System.out.println("Case 14b: Liste bloğu ekleme ve düzenleme testi başarıyla tamamlandı");

        } catch (Exception e) {
            System.err.println("Case 14b: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 14b: Test başarısız - " + e.getMessage());
        }
    }
}
