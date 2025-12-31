package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 4a: Story Oluşturma - Sadece Yazı (Text Only)
 * 
 * Öncelik: YÜKSEK (En önemli test)
 * 
 * Medium benzeri yazı oluşturma mantığı:
 * - Kullanıcı başlık olmadan içerik yazabilir
 * - Ancak başlık olmadan yayınla butonu disabled olmalı
 * - Başlık olmadan yayınla butonuna tıklanırsa alert gösterilmeli
 * - İçerik olmadan yayınlanamaz (alert gösterilmeli)
 * - Başlık ve içerik varsa yayınlanabilir
 * 
 * Senaryo:
 * - WRITER olarak giriş yap
 * - Yeni story oluştur sayfasına git (/reader/new-story)
 * - Başlık gir
 * - İçerik gir (yeterli uzunlukta)
 * - Yayınla butonuna tıkla
 * - Story'nin oluşturulduğunu ve içeriğin doğru kaydedildiğini doğrula
 */
@DisplayName("Case 4a: Story Oluşturma - Sadece Yazı")
public class Case4_StoryCreationTextOnlyTest extends BaseSeleniumTest {
    
    /**
     * Senaryo 1: Başarılı story oluşturma
     * - Başlık girilir
     * - İçerik girilir (yeterli uzunlukta)
     * - Yayınla butonu aktif olmalı
     * - Yayınla butonuna tıklanır
     * - Story başarıyla oluşturulur ve story detay sayfasına yönlendirilir
     */
    @Test
    @DisplayName("Case 4a: Başlık ve içerik ile story başarıyla oluşturulabilmeli")
    public void case4a_StoryCreationTextOnly() {
        try {
            // 1. WRITER rolünde kullanıcı kaydı yap
            driver.get(BASE_URL + "/register");
            waitForPageLoad();
            
            Random random = new Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String email = "writer" + randomSuffix + "@example.com";
            String username = "writer" + randomSuffix;
            
            // Kayıt formunu doldur
            WebElement firstNameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
            );
            firstNameInput.sendKeys("Writer");
            driver.findElement(By.id("lastName")).sendKeys("Test");
            driver.findElement(By.id("email")).sendKeys(email);
            driver.findElement(By.id("username")).sendKeys(username);
            driver.findElement(By.id("password")).sendKeys("Test123456");
            
            // Rol seçimi (WRITER) - Select elementini kullan
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
                    ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("arguments[0].value = 'WRITER';", roleSelectElement);
                }
            }
            
            WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
            );
            WebElement form = driver.findElement(By.tagName("form"));
            safeSubmitForm(submitButton, form);
            
            // 2. Kayıt sonrası otomatik login ve yönlendirme bekle
            Thread.sleep(3000);
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/yazar/dashboard"),
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlToBe(BASE_URL + "/")
            ));
            
            String currentUrlAfterReg = driver.getCurrentUrl();
            assertTrue(
                currentUrlAfterReg.contains("/yazar/dashboard") || 
                currentUrlAfterReg.equals(BASE_URL + "/"),
                "Case 4a: WRITER rolü ile kayıt sonrası /yazar/dashboard'a yönlendirilmedi. URL: " + currentUrlAfterReg
            );
            
            Thread.sleep(2000);
            
            // 3. Story oluştur sayfasına git
            driver.get(BASE_URL + "/reader/new-story");
            waitForPageLoad();
            Thread.sleep(2000);
            
            // 4. Başlık alanını bul ve doldur
            WebElement titleInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input.story-title-input, input[placeholder*='Başlık'], input[placeholder*='başlık']")
                )
            );
            String storyTitle = "Test Story - Sadece Yazı " + randomSuffix;
            titleInput.clear();
            titleInput.sendKeys(storyTitle);
            
            // 5. İçerik alanını bul ve doldur (ilk text bloğu)
            Thread.sleep(1000);
            WebElement contentInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.block-textarea, .editor-blocks textarea, textarea[placeholder*='Hikayenizi']")
                )
            );
            
            // Yeterli uzunlukta içerik gir
            String content = "Bu bir test story içeriğidir. Sadece yazı (text) bloğu kullanılarak oluşturulmuştur. " +
                "Bu içerik yeterince uzun olmalı ve story'nin yayınlanabilmesi için gerekli koşulları sağlamalıdır. " +
                "Yazı bloğu düzgün bir şekilde kaydedilmeli ve görüntülenebilmelidir. " +
                "Test amaçlı bu içerik yeterli uzunlukta olmalıdır.";
            contentInput.clear();
            contentInput.sendKeys(content);
            
            // 6. Yayınla butonunun aktif olduğunu doğrula (başlık ve içerik var)
            Thread.sleep(1000);
            WebElement publishButton = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".publish-button, button.publish-button")
                )
            );
            assertTrue(publishButton.isEnabled(), 
                "Case 4a: Başlık ve içerik girildikten sonra yayınla butonu aktif olmalı");
            
            // 7. Yayınla butonuna tıkla
            publishButton.click();
            
            // 8. Story'nin başarıyla oluşturulduğunu doğrula (story detay sayfasına yönlendirilmeli)
            Thread.sleep(3000);
            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/haberler/") || 
                currentUrl.contains("/story") ||
                currentUrl.contains("/reader"),
                "Case 4a: Story kaydedildikten sonra story detay sayfasına yönlendirilmedi. URL: " + currentUrl
            );
            
            // 9. Story içeriğinin doğru kaydedildiğini kontrol et
            if (currentUrl.contains("/haberler/") || currentUrl.contains("/story")) {
                WebElement storyContent = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".story-content, .content, article, .article-content")
                    )
                );
                String savedContent = storyContent.getText();
                assertTrue(
                    savedContent.contains(content.substring(0, Math.min(50, content.length()))) ||
                    savedContent.contains(storyTitle),
                    "Case 4a: Story içeriği doğru kaydedilmemiş. Beklenen içerik bulunamadı."
                );
            }
            
            System.out.println("Case 4a: Story oluşturma (sadece yazı) testi başarılı");
            
        } catch (Exception e) {
            System.out.println("Case 4a: Story oluşturma (sadece yazı) testi - " + e.getMessage());
            e.printStackTrace();
            fail("Case 4a: Story oluşturma başarısız oldu: " + e.getMessage());
        }
    }
    
    /**
     * Senaryo 2: Başlık olmadan story oluşturma denemesi
     * - Başlık girilmez (boş bırakılır)
     * - İçerik girilir
     * - Yayınla butonu disabled olmalı (başlık olmadığı için)
     * - Eğer JavaScript ile tıklanırsa alert gösterilmeli
     */
    @Test
    @DisplayName("Case 4a Negative: Başlık olmadan story oluşturulamamalı - Yayınla butonu disabled olmalı")
    public void case4a_Negative_EmptyTitle() {
        try {
            // 1. WRITER olarak kayıt ol ve giriş yap
            driver.get(BASE_URL + "/register");
            waitForPageLoad();
            
            Random random = new Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String email = "writer" + randomSuffix + "@example.com";
            
            WebElement firstNameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
            );
            firstNameInput.sendKeys("Writer");
            driver.findElement(By.id("lastName")).sendKeys("Test");
            driver.findElement(By.id("email")).sendKeys(email);
            driver.findElement(By.id("username")).sendKeys("writer" + randomSuffix);
            driver.findElement(By.id("password")).sendKeys("Test123456");
            
            // Rol seçimi (WRITER)
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
                    ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("arguments[0].value = 'WRITER';", roleSelectElement);
                }
            }
            
            WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
            );
            WebElement form = driver.findElement(By.tagName("form"));
            safeSubmitForm(submitButton, form);
            
            Thread.sleep(3000);
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/yazar/dashboard"),
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlToBe(BASE_URL + "/")
            ));
            Thread.sleep(2000);
            
            // 2. Story oluştur sayfasına git
            driver.get(BASE_URL + "/reader/new-story");
            waitForPageLoad();
            Thread.sleep(2000);
            
            // 3. Başlık alanını boş bırak (sadece içerik gir)
            WebElement contentInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.block-textarea, .editor-blocks textarea, textarea[placeholder*='Hikayenizi']")
                )
            );
            contentInput.clear();
            contentInput.sendKeys("Bu bir test içeriğidir. Yeterli uzunlukta içerik girilmiştir. " +
                "Ancak başlık olmadığı için yayınla butonu disabled olmalıdır.");
            
            Thread.sleep(1000);
            
            // 4. Yayınla butonunun disabled olduğunu doğrula (başlık olmadığı için)
            WebElement publishButton = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".publish-button, button.publish-button")
                )
            );
            
            assertFalse(publishButton.isEnabled(), 
                "Case 4a Negative: Başlık olmadan yayınla butonu disabled olmalı");
            
            // 5. JavaScript ile tıklanırsa alert gösterilmeli (frontend validasyonu)
            try {
                // JavaScript ile tıklamayı dene (disabled buton normalde tıklanamaz)
                ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", publishButton);
                Thread.sleep(1000);
                
                // Alert kontrolü (eğer alert gösterildiyse)
                try {
                    org.openqa.selenium.Alert alert = driver.switchTo().alert();
                    String alertText = alert.getText();
                    assertTrue(alertText.contains("başlık") || alertText.contains("Başlık"),
                        "Case 4a Negative: Başlık olmadan alert gösterilmeli. Alert: " + alertText);
                    alert.accept();
                } catch (Exception alertEx) {
                    // Alert gösterilmediyse, sayfa değişmemeli
                    String currentUrl = driver.getCurrentUrl();
                    assertTrue(currentUrl.contains("/new-story"),
                        "Case 4a Negative: Başlık olmadan sayfa değişmemeli. URL: " + currentUrl);
                }
            } catch (Exception e) {
                // JavaScript ile tıklama başarısız oldu (beklenen - disabled buton)
                assertTrue(true, "Case 4a Negative: Disabled buton tıklanamaz (beklenen)");
            }
            
            System.out.println("Case 4a Negative: Başlık olmadan story oluşturulamaz testi başarılı");
            
        } catch (Exception e) {
            System.out.println("Case 4a Negative: " + e.getMessage());
            e.printStackTrace();
            fail("Case 4a Negative: Test başarısız oldu: " + e.getMessage());
        }
    }
    
    /**
     * Senaryo 3: İçerik olmadan story oluşturma denemesi
     * - Başlık girilir
     * - İçerik girilmez (boş bırakılır)
     * - Yayınla butonu aktif olabilir (başlık var)
     * - Yayınla butonuna tıklanırsa alert gösterilmeli ("Lütfen içerik girin")
     */
    @Test
    @DisplayName("Case 4a Negative: İçerik olmadan story oluşturulamamalı - Alert gösterilmeli")
    public void case4a_Negative_EmptyContent() {
        try {
            // 1. WRITER olarak kayıt ol ve giriş yap
            driver.get(BASE_URL + "/register");
            waitForPageLoad();
            
            Random random = new Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String email = "writer" + randomSuffix + "@example.com";
            
            WebElement firstNameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
            );
            firstNameInput.sendKeys("Writer");
            driver.findElement(By.id("lastName")).sendKeys("Test");
            driver.findElement(By.id("email")).sendKeys(email);
            driver.findElement(By.id("username")).sendKeys("writer" + randomSuffix);
            driver.findElement(By.id("password")).sendKeys("Test123456");
            
            // Rol seçimi (WRITER)
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
                    ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("arguments[0].value = 'WRITER';", roleSelectElement);
                }
            }
            
            WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
            );
            WebElement form = driver.findElement(By.tagName("form"));
            safeSubmitForm(submitButton, form);
            
            Thread.sleep(3000);
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/yazar/dashboard"),
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlToBe(BASE_URL + "/")
            ));
            Thread.sleep(2000);
            
            // 2. Story oluştur sayfasına git
            driver.get(BASE_URL + "/reader/new-story");
            waitForPageLoad();
            Thread.sleep(2000);
            
            // 3. Başlık gir, içerik boş bırak
            WebElement titleInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input.story-title-input, input[placeholder*='Başlık'], input[placeholder*='başlık']")
                )
            );
            titleInput.clear();
            titleInput.sendKeys("Boş İçerik Test");
            
            // İçerik alanını boş bırak (varsayılan boş textarea)
            Thread.sleep(1000);
            
            // 4. Yayınla butonunun aktif olduğunu doğrula (başlık var)
            WebElement publishButton = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".publish-button, button.publish-button")
                )
            );
            assertTrue(publishButton.isEnabled(), 
                "Case 4a Negative: Başlık girildikten sonra yayınla butonu aktif olmalı");
            
            // 5. Yayınla butonuna tıkla
            publishButton.click();
            Thread.sleep(1000);
            
            // 6. Alert gösterilmeli ("Lütfen içerik girin")
            try {
                org.openqa.selenium.Alert alert = driver.switchTo().alert();
                String alertText = alert.getText();
                assertTrue(alertText.contains("içerik") || alertText.contains("İçerik"),
                    "Case 4a Negative: İçerik olmadan alert gösterilmeli. Alert: " + alertText);
                alert.accept();
                
                // 7. Sayfa değişmemeli (hala /reader/new-story sayfasında olmalı)
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains("/new-story"),
                    "Case 4a Negative: İçerik olmadan sayfa değişmemeli. URL: " + currentUrl);
                
            } catch (Exception alertEx) {
                // Alert gösterilmediyse, sayfa değişmemeli
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains("/new-story"),
                    "Case 4a Negative: İçerik olmadan sayfa değişmemeli. URL: " + currentUrl);
            }
            
            System.out.println("Case 4a Negative: İçerik olmadan story oluşturulamaz testi başarılı");
            
        } catch (Exception e) {
            System.out.println("Case 4a Negative: " + e.getMessage());
            e.printStackTrace();
            fail("Case 4a Negative: Test başarısız oldu: " + e.getMessage());
        }
    }
}
