package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Case 7: Yorum Yapma
 * 
 * Senaryo:
 * - Kullanıcı olarak giriş yap
 * - Bir story sayfasına git
 * - Yorum yaz
 * - Yorumun başarıyla eklendiğini doğrula
 */
@DisplayName("Test Case 7: Yorum Yapma")
public class TestCase7_CommentTest extends BaseSeleniumTest {
    
    private String testEmail;
    private String testPassword = "Test123456";
    
    @Test
    @DisplayName("Kullanıcı story'ye yorum yapabilmeli")
    public void testAddComment() {
        // Önce kullanıcı oluştur ve giriş yap
        createUserAndLogin();
        
        // Ana sayfaya git ve bir story bul
        driver.get(BASE_URL);
        waitForPageLoad();
        
        // Story linkini bul ve tıkla
        try {
            WebElement storyLink = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector("a[href*='/haberler/'], .story-card a, .story-title a")
                )
            );
            storyLink.click();
            waitForPageLoad();
        } catch (Exception e) {
            // Story bulunamazsa, direkt bir story URL'ine git
            driver.get(BASE_URL + "/haberler/test-story");
            waitForPageLoad();
        }
        
        // Yorum alanını bul
        WebElement commentInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("textarea[name='comment'], textarea[id='comment'], textarea[placeholder*='yorum']")
            )
        );
        
        Random random = new Random();
        String commentText = "Bu bir test yorumudur. " + random.nextInt(10000);
        
        commentInput.clear();
        commentInput.sendKeys(commentText);
        
        // Yorum gönder butonuna tıkla
        WebElement submitButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']:contains('Gönder'), button:contains('Yorum Yap')")
            )
        );
        submitButton.click();
        
        // Yorumun eklenmesini bekle
        try {
            Thread.sleep(2000);
            
            // Yorumun eklendiğini doğrula
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(), '" + commentText.substring(0, 20) + "')]")
            ));
            
            // Yorum listesinde yorumumuzun olduğunu kontrol et
            WebElement commentElement = driver.findElement(
                By.xpath("//*[contains(text(), '" + commentText.substring(0, 20) + "')]")
            );
            assertNotNull(commentElement, "Yorum sayfada görünmüyor");
            
        } catch (Exception e) {
            // Hata mesajı kontrolü
            try {
                WebElement errorElement = driver.findElement(By.cssSelector(".error, .alert-danger"));
                if (errorElement.isDisplayed()) {
                    fail("Yorum ekleme başarısız: " + errorElement.getText());
                }
            } catch (Exception ex) {
                fail("Yorum eklenemedi ve hata mesajı da görünmüyor: " + e.getMessage());
            }
        }
    }
    
    /**
     * Kullanıcı oluştur ve giriş yap
     */
    private void createUserAndLogin() {
        Random random = new Random();
        String randomSuffix = String.valueOf(random.nextInt(100000));
        testEmail = "commenter" + randomSuffix + "@example.com";
        String kullaniciAdi = "commenter" + randomSuffix;
        
        // Kayıt sayfasına git
        driver.get(BASE_URL + "/register");
        waitForPageLoad();
        
        // Form alanlarını doldur
        WebElement adInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("ad"))
        );
        adInput.sendKeys("Commenter");
        
        WebElement soyadInput = driver.findElement(By.id("soyad"));
        soyadInput.sendKeys("User");
        
        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys(testEmail);
        
        WebElement kullaniciAdiInput = driver.findElement(By.id("kullaniciAdi"));
        kullaniciAdiInput.sendKeys(kullaniciAdi);
        
        WebElement sifreInput = driver.findElement(By.id("sifre"));
        sifreInput.sendKeys(testPassword);
        
        // Kayıt butonuna tıkla
        WebElement submitButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")
            )
        );
        submitButton.click();
        
        // Kayıt işleminin tamamlanmasını bekle
        try {
            Thread.sleep(3000);
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlContains("/reader")
            ));
        } catch (Exception e) {
            // Giriş yapılmış olabilir, devam et
        }
    }
}

