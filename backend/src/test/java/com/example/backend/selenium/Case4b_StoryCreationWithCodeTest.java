package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 4b: Story Oluşturma - Kod Bloğu Ekleme
 * 
 * Öncelik: YÜKSEK
 * 
 * Use Case: WRITER rolündeki kullanıcı kod bloğu içeren story oluşturabilmeli
 * Senaryo:
 * - WRITER olarak giriş yap
 * - Yeni story oluştur sayfasına git
 * - Başlık gir
 * - Kod bloğu ekle
 * - Kod içeriği gir
 * - Story'yi kaydet
 * - Story'nin kod bloğu ile birlikte oluşturulduğunu doğrula
 */
@DisplayName("Case 4b: Story Oluşturma - Kod Bloğu")
public class Case4b_StoryCreationWithCodeTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 4b: WRITER kod bloğu ile story oluşturabilmeli")
    public void case4b_StoryCreationWithCode() {
        try {
            // Önce WRITER rolünde kullanıcı kaydı yap
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
            
            // Frontend'de kayıt sonrası otomatik login yapılıyor
            Thread.sleep(3000);
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/yazar/dashboard"),
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlToBe(BASE_URL + "/")
            ));
            Thread.sleep(2000);
            
            // Story oluştur sayfasına git (/reader/new-story)
            driver.get(BASE_URL + "/reader/new-story");
            waitForPageLoad();
            Thread.sleep(2000);
            
            // Başlık alanını bul ve doldur
            WebElement titleInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input.story-title-input, input[placeholder*='Başlık'], input[placeholder*='başlık']")
                )
            );
            String storyTitle = "Test Story - Kod Bloğu " + randomSuffix;
            titleInput.sendKeys(storyTitle);
            
            // İlk text bloğunu bul (BOŞ BIRAK - artı butonu sadece boş text bloğunda görünür)
            Thread.sleep(1000);
            WebElement firstTextBlock = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.block-textarea, .editor-blocks textarea, textarea[placeholder*='Hikayenizi']")
                )
            );
            
            // Text bloğunu BOŞ BIRAK (içerik yazma - artı butonu sadece boş text bloğunda görünür)
            // Frontend: showPlusButton = block.type === 'text' && block.content === '' && (isHovered || isMenuOpen)
            
            // Text bloğuna hover yap (artı butonunu görünür yapmak için)
            Actions actions = new Actions(driver);
            actions.moveToElement(firstTextBlock).perform();
            Thread.sleep(1000);
            
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
            String codeContent = "function hello() {\n    console.log('Hello World');\n    return true;\n}";
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
            Thread.sleep(1000);
            
            // Story'yi yayınla (Frontend'de "Kaydet" yok, sadece "Yayınla" var)
            Thread.sleep(1000);
            WebElement publishButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".publish-button, button.publish-button")
                )
            );
            publishButton.click();
            
            // Story'nin kaydedildiğini doğrula
            Thread.sleep(3000);
            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/dashboard") || 
                currentUrl.contains("/yazar") ||
                currentUrl.contains("/story") ||
                currentUrl.contains("/reader") ||
                currentUrl.contains("/haberler"),
                "Case 4b: Story kaydedildikten sonra yönlendirme yapılmadı. URL: " + currentUrl
            );
            
            // Story'nin kod bloğu ile birlikte kaydedildiğini kontrol et
            if (currentUrl.contains("/story") || currentUrl.contains("/reader") || currentUrl.contains("/haberler")) {
                WebElement storyContent = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".story-content, .content, article")
                    )
                );
                String savedContent = storyContent.getText();
                assertTrue(
                    savedContent.contains("console.log") || savedContent.contains("function hello"),
                    "Case 4b: Kod bloğu doğru kaydedilmemiş. İçerik: " + savedContent.substring(0, Math.min(200, savedContent.length()))
                );
            }
            
            System.out.println("Case 4b: Story oluşturma (kod bloğu) testi başarılı");
            
        } catch (Exception e) {
            System.out.println("Case 4b: Story oluşturma (kod bloğu) testi - " + e.getMessage());
            e.printStackTrace();
            fail("Case 4b: Test başarısız oldu: " + e.getMessage());
        }
    }
    
    /**
     * Senaryo 2: Birden fazla blok ekleme (kod ekledikten sonra alt satırda başka bloklar)
     * - Başlık gir
     * - Kod bloğu ekle
     * - Kod bloğu onaylandıktan sonra otomatik olarak yeni text bloğu oluşur
     * - Yeni text bloğuna hover yap
     * - Artı butonuna tıkla
     * - Menüden farklı bloklar ekle (resim, yazı, kod gibi)
     */
    @Test
    @DisplayName("Case 4b: Kod ekledikten sonra alt satırda başka bloklar eklenebilmeli")
    public void case4b_MultipleBlocksAfterCode() {
        try {
            // 1. WRITER olarak kayıt ol
            driver.get(BASE_URL + "/register");
            waitForPageLoad();
            
            Random random = new Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String email = "writer" + randomSuffix + "@example.com";
            String username = "writer" + randomSuffix;
            
            WebElement firstNameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
            );
            firstNameInput.sendKeys("Writer");
            driver.findElement(By.id("lastName")).sendKeys("Test");
            driver.findElement(By.id("email")).sendKeys(email);
            driver.findElement(By.id("username")).sendKeys(username);
            driver.findElement(By.id("password")).sendKeys("Test123456");
            
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
            
            // 3. Başlık gir
            WebElement titleInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input.story-title-input, input[placeholder*='Başlık'], input[placeholder*='başlık']")
                )
            );
            String storyTitle = "Test Story - Çoklu Blok " + randomSuffix;
            titleInput.sendKeys(storyTitle);
            
            // 4. İlk text bloğunu bul (BOŞ BIRAK)
            Thread.sleep(1000);
            WebElement firstTextBlock = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.block-textarea, .editor-blocks textarea, textarea[placeholder*='Hikayenizi']")
                )
            );
            
            // 5. Text bloğuna hover yap ve kod bloğu ekle
            Actions actions = new Actions(driver);
            actions.moveToElement(firstTextBlock).perform();
            Thread.sleep(1000);
            
            WebElement addButton1 = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".block-add-button.visible, .editor-block .block-add-button.visible")
                )
            );
            addButton1.click();
            Thread.sleep(1000);
            
            // Kod butonuna tıkla
            WebElement codeMenuButton1 = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".block-add-menu button[title='Kod'], .block-add-menu button:nth-child(4)")
                )
            );
            codeMenuButton1.click();
            Thread.sleep(1000);
            
            // Kod içeriğini gir
            WebElement codeBlock1 = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.code-editor-inline-textarea, .code-editor-inline textarea")
                )
            );
            String codeContent1 = "const firstCode = 'Hello';";
            codeBlock1.clear();
            codeBlock1.sendKeys(codeContent1);
            
            // Kod bloğunu onayla (otomatik olarak yeni text bloğu oluşur)
            Thread.sleep(1000);
            WebElement confirmButton1 = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".code-editor-btn.confirm, button.code-editor-btn[title='Onayla']")
                )
            );
            confirmButton1.click();
            Thread.sleep(3000); // Yeni text bloğunun oluşmasını bekle (frontend 10ms timeout kullanıyor ama DOM güncellemesi için daha fazla bekle)
            
            // 6. Yeni oluşan text bloğunu bul (kod bloğundan sonra otomatik oluşur)
            // Kod bloğu onaylandıktan sonra yeni text bloğu oluşur, bekle
            WebElement secondTextBlock = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.block-textarea")
                )
            );
            // Tüm textarea'ları bul ve son boş olanı al (kod bloğundan sonra oluşan)
            java.util.List<WebElement> allTextAreas = driver.findElements(
                By.cssSelector("textarea.block-textarea")
            );
            // Son textarea'yı al (kod bloğundan sonra oluşan yeni text bloğu)
            secondTextBlock = allTextAreas.get(allTextAreas.size() - 1);
            
            // 7. İkinci text bloğuna hover yap ve başka blok ekle (örneğin yazı)
            actions.moveToElement(secondTextBlock).perform();
            Thread.sleep(1000);
            
            WebElement addButton2 = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".block-add-button.visible, .editor-block .block-add-button.visible")
                )
            );
            addButton2.click();
            Thread.sleep(1000);
            
            // Başlık butonuna tıkla (2. buton)
            WebElement headingMenuButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".block-add-menu button[title='Başlık'], .block-add-menu button:nth-child(2)")
                )
            );
            headingMenuButton.click();
            Thread.sleep(1000);
            
            // Başlık içeriğini gir
            WebElement headingInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input.block-heading, .block-heading")
                )
            );
            headingInput.sendKeys("Alt Başlık");
            Thread.sleep(500);
            
            // 8. Başlıktan sonra Enter'a bas (yeni text bloğu oluşur)
            headingInput.sendKeys(org.openqa.selenium.Keys.ENTER);
            Thread.sleep(1000);
            
            // 9. Üçüncü text bloğunu bul ve içerik ekle (başlıktan sonra oluşan)
            Thread.sleep(2000); // Başlıktan sonra yeni text bloğunun oluşmasını bekle
            java.util.List<WebElement> textBlocksAfterHeading = driver.findElements(
                By.cssSelector("textarea.block-textarea")
            );
            assertTrue(textBlocksAfterHeading.size() >= 2, "Başlıktan sonra yeni text bloğu oluşmalı");
            
            // Son textarea'yı al (başlıktan sonra oluşan yeni text bloğu)
            WebElement thirdTextBlock = textBlocksAfterHeading.get(textBlocksAfterHeading.size() - 1);
            thirdTextBlock.click(); // Focus yap
            Thread.sleep(500);
            thirdTextBlock.sendKeys("Bu bir test içeriğidir. Kod bloğundan sonra başlık ve yazı eklendi.");
            Thread.sleep(500);
            
            // 10. Text bloğu dolu olduğu için artı butonu görünmez
            // Cursor'ı sona al ve Enter'a basarak yeni boş text bloğu oluştur
            thirdTextBlock.sendKeys(org.openqa.selenium.Keys.END); // Cursor'ı sona al
            Thread.sleep(200);
            thirdTextBlock.sendKeys(org.openqa.selenium.Keys.ENTER); // Enter'a bas (yeni text bloğu oluşur)
            Thread.sleep(2000); // Yeni text bloğunun oluşmasını bekle
            
            // Yeni oluşan boş text bloğunu bul
            java.util.List<WebElement> allTextBlocks = driver.findElements(
                By.cssSelector("textarea.block-textarea")
            );
            assertTrue(allTextBlocks.size() >= 3, "Enter'a basıldıktan sonra yeni text bloğu oluşmalı");
            // Son textarea'yı al (yeni oluşan boş text bloğu)
            WebElement fourthTextBlock = allTextBlocks.get(allTextBlocks.size() - 1);
            
            // Boş olduğunu doğrula
            String fourthTextContent = fourthTextBlock.getAttribute("value");
            assertTrue(fourthTextContent == null || fourthTextContent.isEmpty(), 
                "Yeni oluşan text bloğu boş olmalı");
            
            // Boş text bloğuna focus yap ve hover yap (artı butonunu görünür yapmak için)
            fourthTextBlock.click(); // Focus yap
            Thread.sleep(500);
            actions.moveToElement(fourthTextBlock).perform();
            Thread.sleep(1500); // Hover sonrası artı butonunun görünür olmasını bekle
            
            // Artı butonunu bul (boş text bloğunda görünür olmalı)
            WebElement addButton3 = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".block-add-button.visible, .editor-block .block-add-button.visible")
                )
            );
            addButton3.click();
            Thread.sleep(1000);
            
            // Tekrar kod butonuna tıkla
            WebElement codeMenuButton2 = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".block-add-menu button[title='Kod'], .block-add-menu button:nth-child(4)")
                )
            );
            codeMenuButton2.click();
            Thread.sleep(1000);
            
            // İkinci kod içeriğini gir
            WebElement codeBlock2 = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.code-editor-inline-textarea, .code-editor-inline textarea")
                )
            );
            String codeContent2 = "const secondCode = 'World';";
            codeBlock2.clear();
            codeBlock2.sendKeys(codeContent2);
            
            // İkinci kod bloğunu onayla
            Thread.sleep(1000);
            WebElement confirmButton2 = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".code-editor-btn.confirm, button.code-editor-btn[title='Onayla']")
                )
            );
            confirmButton2.click();
            Thread.sleep(1000);
            
            // 11. Story'yi yayınla
            WebElement publishButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".publish-button, button.publish-button")
                )
            );
            publishButton.click();
            
            // 12. Story'nin kaydedildiğini doğrula
            Thread.sleep(3000);
            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/dashboard") || 
                currentUrl.contains("/yazar") ||
                currentUrl.contains("/story") ||
                currentUrl.contains("/reader") ||
                currentUrl.contains("/haberler"),
                "Case 4b Multiple: Story kaydedildikten sonra yönlendirme yapılmadı. URL: " + currentUrl
            );
            
            System.out.println("Case 4b Multiple: Birden fazla blok ekleme testi başarılı");
            
        } catch (Exception e) {
            System.out.println("Case 4b Multiple: " + e.getMessage());
            e.printStackTrace();
            fail("Case 4b Multiple: Test başarısız oldu: " + e.getMessage());
        }
    }
}

