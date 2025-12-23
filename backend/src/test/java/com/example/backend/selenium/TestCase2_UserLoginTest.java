package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Case 2: Kullanıcı Girişi (User Login)
 * 
 * Senaryo:
 * - Ana sayfadan giriş sayfasına git
 * - Email ve şifre alanlarını doldur
 * - Giriş butonuna tıkla
 * - Başarılı giriş sonrası dashboard'a yönlendirildiğini doğrula
 * 
 * Not: Bu test için önceden kayıtlı bir kullanıcı gereklidir
 */
@DisplayName("Test Case 2: Kullanıcı Girişi")
public class TestCase2_UserLoginTest extends BaseSeleniumTest {
    
    // Test için kullanılacak kullanıcı bilgileri
    // Her test için yeni bir kullanıcı oluşturulacak
    private String testEmail;
    private String testPassword = "Test123456";
    
    @Test
    @DisplayName("Mevcut kullanıcı ile giriş yapılabilmeli")
    public void testUserLogin() {
        // Önce bir kullanıcı oluştur
        createTestUser();
        
        // Ana sayfadan giriş sayfasına git
        driver.get(BASE_URL + "/login");
        waitForPageLoad();
        
        // URL'in /login olduğunu doğrula
        assertTrue(driver.getCurrentUrl().contains("/login"), 
            "Giriş sayfasına yönlendirilmedi");
        
        // Form alanlarını bul ve doldur
        WebElement emailInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("email"))
        );
        emailInput.clear();
        emailInput.sendKeys(testEmail);
        
        WebElement sifreInput = driver.findElement(By.id("sifre"));
        sifreInput.clear();
        sifreInput.sendKeys(testPassword);
        
        // Giriş butonuna tıkla
        WebElement submitButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")
            )
        );
        assertTrue(submitButton.isEnabled(), 
            "Giriş butonu aktif olmalı");
        submitButton.click();
        
        // API çağrısının tamamlanmasını bekle
        try {
            Thread.sleep(2000); // API çağrısı için bekle
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Hata mesajı var mı kontrol et
        try {
            WebElement errorElement = driver.findElement(By.cssSelector(".auth-error"));
            if (errorElement.isDisplayed() && !errorElement.getText().isEmpty()) {
                String errorMessage = errorElement.getText();
                fail("Giriş işlemi başarısız oldu. Hata mesajı: " + errorMessage);
            }
        } catch (Exception e) {
            // Hata mesajı yok, devam et
        }
        
        // Başarılı giriş sonrası dashboard'a yönlendirildiğini doğrula
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/reader/dashboard"),
                ExpectedConditions.urlContains("/yazar/dashboard"),
                ExpectedConditions.urlContains("/admin/dashboard")
            ));
            
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("/dashboard"),
                "Giriş sonrası dashboard'a yönlendirilmedi. Mevcut URL: " + currentUrl);
            
            // Dashboard sayfasında olduğumuzu doğrula
            WebElement dashboardContent = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("body")
                )
            );
            assertNotNull(dashboardContent, "Dashboard sayfası yüklenmedi");
            
        } catch (org.openqa.selenium.TimeoutException e) {
            String currentUrl = driver.getCurrentUrl();
            String errorMessage = "Bilinmeyen hata";
            try {
                WebElement errorElement = driver.findElement(By.cssSelector(".auth-error"));
                if (errorElement.isDisplayed()) {
                    errorMessage = errorElement.getText();
                }
            } catch (Exception ex) {
                // Hata mesajı bulunamadı
            }
            
            fail("Giriş işlemi timeout oldu. " +
                 "Mevcut URL: " + currentUrl + ". " +
                 "Hata mesajı: " + errorMessage + ". " +
                 "Kullanıcı: " + testEmail);
        }
    }
    
    /**
     * Test için kullanıcı oluştur
     */
    private void createTestUser() {
        java.util.Random random = new java.util.Random();
        String randomSuffix = String.valueOf(random.nextInt(100000));
        testEmail = "testlogin" + randomSuffix + "@example.com";
        String kullaniciAdi = "testlogin" + randomSuffix;
        
        // Kayıt sayfasına git
        driver.get(BASE_URL + "/register");
        waitForPageLoad();
        
        // Form alanlarını doldur
        WebElement adInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("ad"))
        );
        adInput.sendKeys("Test");
        
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
            Thread.sleep(3000); // API çağrısı için bekle
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Dashboard'a yönlendirildiğini kontrol et
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/reader/dashboard"),
                ExpectedConditions.urlContains("/yazar/dashboard"),
                ExpectedConditions.urlContains("/admin/dashboard")
            ));
        } catch (Exception e) {
            // Kayıt başarısız olabilir, devam et
        }
        
        // Çıkış yap (eğer giriş yapıldıysa)
        try {
            driver.get(BASE_URL + "/login");
            waitForPageLoad();
        } catch (Exception e) {
            // Hata yok say
        }
    }
    
    @Test
    @DisplayName("Yanlış şifre ile giriş yapılamamalı")
    public void testLoginWithWrongPassword() {
        // Önce bir kullanıcı oluştur
        createTestUser();
        
        // Giriş sayfasına git
        driver.get(BASE_URL + "/login");
        waitForPageLoad();
        
        // Form alanlarını doldur (yanlış şifre)
        WebElement emailInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("email"))
        );
        emailInput.clear();
        emailInput.sendKeys(testEmail);
        
        WebElement sifreInput = driver.findElement(By.id("sifre"));
        sifreInput.clear();
        sifreInput.sendKeys("WrongPassword123");
        
        // Giriş butonuna tıkla
        WebElement submitButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")
            )
        );
        submitButton.click();
        
        // API çağrısının tamamlanmasını bekle
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Hata mesajının göründüğünü doğrula
        WebElement errorMessage = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".auth-error")
            )
        );
        assertTrue(errorMessage.isDisplayed(), "Hata mesajı görünmüyor");
        assertFalse(errorMessage.getText().isEmpty(), 
            "Hata mesajı boş olmamalı");
        
        // Hata mesajının doğru olduğunu kontrol et
        String errorText = errorMessage.getText();
        assertTrue(errorText.contains("Email") || errorText.contains("şifre") || 
                   errorText.contains("hatalı") || errorText.contains("yanlış"),
            "Hata mesajı beklenen formatta değil: " + errorText);
        
        // Hala login sayfasında olduğumuzu doğrula
        assertTrue(driver.getCurrentUrl().contains("/login"), 
            "Yanlış şifre ile giriş yapılmamalı, login sayfasında kalınmalı");
    }
}

