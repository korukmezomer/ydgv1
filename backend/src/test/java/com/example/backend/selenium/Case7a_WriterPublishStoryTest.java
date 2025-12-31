package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 7a: Writer Story Yayınlama Testi
 * 
 * Senaryo:
 * - Writer olarak kayıt ol
 * - Story oluştur
 * - Story'yi yayınla (YAYIN_BEKLIYOR durumuna geçer)
 */
@DisplayName("Case 7a: Writer Story Yayınlama")
public class Case7a_WriterPublishStoryTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 7a: Writer story oluşturup yayınlayabilmeli")
    public void case7a_WriterPublishStory() {
        try {
            // 1. Writer kullanıcısı oluştur
            java.util.Random random = new java.util.Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String writerEmail = "writer_publish_" + randomSuffix + "@example.com";
            String writerPassword = "Test123456";
            
            driver.get(BASE_URL + "/register");
            waitForPageLoad();
            
            WebElement firstNameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
            );
            firstNameInput.sendKeys("Writer");
            driver.findElement(By.id("lastName")).sendKeys("Publish");
            driver.findElement(By.id("email")).sendKeys(writerEmail);
            driver.findElement(By.id("username")).sendKeys("writer_publish_" + randomSuffix);
            driver.findElement(By.id("password")).sendKeys(writerPassword);
            
            // Role seçimi - WRITER
            try {
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
                        // JavaScript ile değer set et
                        ((org.openqa.selenium.JavascriptExecutor) driver)
                            .executeScript("arguments[0].value = 'WRITER';", roleSelectElement);
                    }
                }
            } catch (Exception e) {
                // Role select yoksa devam et
                System.out.println("Role select bulunamadı: " + e.getMessage());
            }
            
            WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
            );
            submitButton.click();
            
            Thread.sleep(3000);
            
            // Writer olarak zaten giriş yapılmış durumda (kayıt sonrası dashboard'a yönlendirildi)
            
            // 2. Story oluştur ve yayınla
            String storyTitle = "Yayınlama Test Story " + System.currentTimeMillis();
            String storyContent = "Bu bir yayınlama test story'sidir.";
            String storySlug = createStory(writerEmail, writerPassword, storyTitle, storyContent);
            
            assertNotNull(storySlug, "Case 7a: Story oluşturulamadı");
            System.out.println("Case 7a: Story başarıyla oluşturuldu ve yayınlandı. Slug: " + storySlug);
            
        } catch (Exception e) {
            System.out.println("Case 7a: " + e.getMessage());
            e.printStackTrace();
            fail("Case 7a: Test başarısız - " + e.getMessage());
        }
    }
}

