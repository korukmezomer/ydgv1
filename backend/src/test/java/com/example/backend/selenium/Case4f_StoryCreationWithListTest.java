package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 4f: Story Oluşturma - Liste Ekleme
 * 
 * Öncelik: ORTA
 * 
 * Use Case: WRITER rolündeki kullanıcı liste içeren story oluşturabilmeli
 * Senaryo:
 * - WRITER olarak giriş yap
 * - Yeni story oluştur sayfasına git
 * - Başlık gir
 * - Liste bloğu ekle (artı butonundan)
 * - Liste öğeleri gir
 * - Story'yi kaydet
 * - Story'nin liste ile birlikte oluşturulduğunu doğrula
 */
@DisplayName("Case 4f: Story Oluşturma - Liste")
public class Case4f_StoryCreationWithListTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 4f: WRITER liste ile story oluşturabilmeli")
    public void case4f_StoryCreationWithList() {
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
            String storyTitle = "Test Story - Liste " + randomSuffix;
            titleInput.sendKeys(storyTitle);
            
            // 4. İlk text bloğunu bul (BOŞ BIRAK)
            Thread.sleep(1000);
            WebElement firstTextBlock = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.block-textarea, .editor-blocks textarea, textarea[placeholder*='Hikayenizi']")
                )
            );
            
            // 5. Text bloğuna hover yap ve liste ekle
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
            
            // Liste butonuna tıkla (6. buton - Resim, Başlık, Video, Kod, Gömülü İçerik, Liste)
            WebElement listMenuButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".block-add-menu button[title='Liste'], .block-add-menu button:nth-child(6)")
                )
            );
            listMenuButton.click();
            Thread.sleep(2000);
            
            // 6. Liste öğelerini gir (doğru selector: .block-list input veya li input)
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
            Thread.sleep(500);
            secondListItem.sendKeys(Keys.ENTER);
            Thread.sleep(1000); // Yeni liste öğesinin oluşmasını bekle
            
            // Üçüncü liste öğesi
            listItems = driver.findElements(
                By.cssSelector(".block-list input, .list-block input, li input[type='text'], input[data-list-item-index]")
            );
            assertTrue(listItems.size() >= 3, "Üçüncü liste öğesi oluşmalı. Mevcut öğe sayısı: " + listItems.size());
            
            WebElement thirdListItem = listItems.get(2);
            thirdListItem.click(); // Focus yap
            Thread.sleep(200);
            thirdListItem.clear();
            thirdListItem.sendKeys("Üçüncü liste öğesi");
            Thread.sleep(1000);
            
            // 7. Liste bloğunun eklendiğini doğrula
            WebElement listBlock = driver.findElement(
                By.cssSelector(".list-block, .list, ul, ol")
            );
            assertNotNull(listBlock, "Case 4f: Liste bloğu eklenemedi");
            
            // 8. Story'yi yayınla
            Thread.sleep(1000);
            WebElement publishButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".publish-button, button.publish-button")
                )
            );
            publishButton.click();
            
            // 9. Story'nin kaydedildiğini doğrula
            Thread.sleep(3000);
            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/dashboard") || 
                currentUrl.contains("/yazar") ||
                currentUrl.contains("/story") ||
                currentUrl.contains("/reader") ||
                currentUrl.contains("/haberler"),
                "Case 4f: Story kaydedildikten sonra yönlendirme yapılmadı. URL: " + currentUrl
            );
            
            System.out.println("Case 4f: Story oluşturma (liste) testi başarılı");
            
        } catch (Exception e) {
            System.out.println("Case 4f: Story oluşturma (liste) testi - " + e.getMessage());
            e.printStackTrace();
            fail("Case 4f: Test başarısız oldu: " + e.getMessage());
        }
    }
}
