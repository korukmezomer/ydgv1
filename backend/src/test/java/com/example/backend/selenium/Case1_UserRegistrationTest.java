package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 1: Kullanıcı Kaydı (User Registration)
 * 
 * Use Case: Yeni kullanıcı sisteme kayıt olabilmeli
 * Senaryo:
 * - Ana sayfadan kayıt sayfasına git
 * - Form alanlarını doldur (ad, soyad, email, kullanıcı adı, şifre)
 * - Kayıt butonuna tıkla
 * - Başarılı kayıt sonrası dashboard'a yönlendirildiğini doğrula
 */
@DisplayName("Case 1: Kullanıcı Kaydı")
public class Case1_UserRegistrationTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 1: Yeni kullanıcı kaydı başarılı olmalı")
    public void case1_UserRegistration() {
        // Ana sayfadan kayıt sayfasına git
        WebElement registerLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.linkText("Başla"))
        );
        registerLink.click();
        
        waitForPageLoad();
        
        // URL'in /register olduğunu doğrula
        assertTrue(driver.getCurrentUrl().contains("/register"), 
            "Case 1: Kayıt sayfasına yönlendirilmedi");
        
        // Form alanlarını bul ve doldur
        Random random = new Random();
        String randomSuffix = String.valueOf(random.nextInt(10000));
        String email = "testuser" + randomSuffix + "@example.com";
        String kullaniciAdi = "testuser" + randomSuffix;
        String sifre = "Test123456";
        
        // Ad alanı
        WebElement adInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
        );
        adInput.sendKeys("Test");
        
        // Soyad alanı
        WebElement soyadInput = driver.findElement(By.id("lastName"));
        soyadInput.sendKeys("User");
        
        // Email alanı
        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys(email);
        
        // Kullanıcı adı alanı
        WebElement kullaniciAdiInput = driver.findElement(By.id("username"));
        kullaniciAdiInput.sendKeys(kullaniciAdi);
        
        // Şifre alanı
        WebElement sifreInput = driver.findElement(By.id("password"));
        sifreInput.sendKeys(sifre);
        
        // Kayıt butonuna tıkla
        WebElement submitButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")
            )
        );
        assertTrue(submitButton.isEnabled(), 
            "Case 1: Kayıt butonu aktif olmalı");
        
        // Form elementini bul
        WebElement form = driver.findElement(By.tagName("form"));
        safeSubmitForm(submitButton, form);
        
        // API çağrısının tamamlanmasını bekle
        try {
            Thread.sleep(3000);
            
            String currentUrl = driver.getCurrentUrl();
            
            // Eğer login sayfasına yönlendirildiyse, otomatik giriş yap
            if (currentUrl.contains("/login")) {
                // Login formunu doldur
                WebElement loginEmailInput = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.id("email"))
                );
                loginEmailInput.clear();
                loginEmailInput.sendKeys(email);
                
                WebElement loginPasswordInput = driver.findElement(By.id("password"));
                loginPasswordInput.clear();
                loginPasswordInput.sendKeys(sifre);
                
                // Giriş butonuna tıkla
                WebElement loginSubmitButton = wait.until(
                    ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[type='submit']")
                    )
                );
                WebElement loginForm = driver.findElement(By.tagName("form"));
                safeSubmitForm(loginSubmitButton, loginForm);
                
                // Giriş işleminin tamamlanmasını bekle
                Thread.sleep(3000);
            }
            
            // Dashboard'a yönlendirilmeyi bekle
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/reader/dashboard"),
                ExpectedConditions.urlContains("/yazar/dashboard"),
                ExpectedConditions.urlContains("/admin/dashboard"),
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlToBe(BASE_URL + "/")
            ));
            
            currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/dashboard") || 
                currentUrl.equals(BASE_URL + "/") ||
                currentUrl.equals(BASE_URL + "/reader/dashboard") ||
                currentUrl.equals(BASE_URL + "/yazar/dashboard") ||
                currentUrl.equals(BASE_URL + "/admin/dashboard"),
                "Case 1: Kayıt sonrası dashboard'a yönlendirilmedi. Mevcut URL: " + currentUrl
            );
            
        } catch (Exception e) {
            fail("Case 1: Kayıt işlemi başarısız oldu: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Case 1 Negative: Geçersiz email ile kayıt yapılamamalı")
    public void case1_Negative_InvalidEmail() {
        driver.get(BASE_URL + "/register");
        waitForPageLoad();
        
        // Geçersiz email formatı
        WebElement emailInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("email"))
        );
        emailInput.sendKeys("gecersiz-email");
        
        driver.findElement(By.id("firstName")).sendKeys("Test");
        driver.findElement(By.id("lastName")).sendKeys("User");
        driver.findElement(By.id("username")).sendKeys("testuser");
        driver.findElement(By.id("password")).sendKeys("Test123456");
        
        WebElement submitButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
        );
        WebElement form = driver.findElement(By.tagName("form"));
        safeSubmitForm(submitButton, form);
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Hata mesajı veya form validasyonu kontrolü
        try {
            WebElement errorElement = driver.findElement(
                By.cssSelector(".error, .text-red-500, [role='alert']")
            );
            assertTrue(errorElement.isDisplayed() || driver.getCurrentUrl().contains("/register"),
                "Case 1 Negative: Geçersiz email ile kayıt yapılmamalı");
        } catch (Exception e) {
            // Form validasyonu HTML5 ile yapılıyorsa sayfa değişmeyebilir
            assertTrue(driver.getCurrentUrl().contains("/register"),
                "Case 1 Negative: Geçersiz email ile kayıt sayfasında kalınmalı");
        }
    }
    
    @Test
    @DisplayName("Case 1 Negative: Eksik alanlar ile kayıt yapılamamalı")
    public void case1_Negative_MissingFields() {
        driver.get(BASE_URL + "/register");
        waitForPageLoad();
        
        // Sadece email gir, diğer alanları boş bırak
        WebElement emailInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("email"))
        );
        emailInput.sendKeys("test@example.com");
        
        WebElement submitButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
        );
        
        // Submit butonu disabled olabilir veya form validasyonu çalışabilir
        try {
            if (submitButton.isEnabled()) {
                submitButton.click();
                Thread.sleep(2000);
                // Form validasyonu varsa sayfa değişmemeli
                assertTrue(driver.getCurrentUrl().contains("/register"),
                    "Case 1 Negative: Eksik alanlar ile kayıt yapılmamalı");
            } else {
                assertTrue(true, "Case 1 Negative: Submit butonu disabled (beklenen)");
            }
        } catch (Exception e) {
            assertTrue(driver.getCurrentUrl().contains("/register"),
                "Case 1 Negative: Eksik alanlar ile kayıt sayfasında kalınmalı");
        }
    }
    
    @Test
    @DisplayName("Case 1 Negative: Zaten var olan email ile kayıt yapılamamalı")
    public void case1_Negative_DuplicateEmail() {
        // Önce bir kullanıcı kaydet
        driver.get(BASE_URL + "/register");
        waitForPageLoad();
        
        Random random = new Random();
        String randomSuffix = String.valueOf(random.nextInt(10000));
        String email = "duplicate" + randomSuffix + "@example.com";
        
        WebElement firstNameInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
        );
        firstNameInput.sendKeys("First");
        driver.findElement(By.id("lastName")).sendKeys("User");
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("username")).sendKeys("firstuser" + randomSuffix);
        driver.findElement(By.id("password")).sendKeys("Test123456");
        
        WebElement submitButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
        );
        WebElement form = driver.findElement(By.tagName("form"));
        safeSubmitForm(submitButton, form);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Aynı email ile tekrar kayıt dene
        driver.get(BASE_URL + "/register");
        waitForPageLoad();
        
        firstNameInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
        );
        firstNameInput.sendKeys("Second");
        driver.findElement(By.id("lastName")).sendKeys("User");
        driver.findElement(By.id("email")).sendKeys(email); // Aynı email
        driver.findElement(By.id("username")).sendKeys("seconduser" + randomSuffix);
        driver.findElement(By.id("password")).sendKeys("Test123456");
        
        submitButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
        );
        submitButton.click();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Hata mesajı kontrolü
        try {
            WebElement errorElement = driver.findElement(
                By.cssSelector(".error, .text-red-500, [role='alert'], .alert-danger")
            );
            assertTrue(errorElement.isDisplayed() || driver.getCurrentUrl().contains("/register"),
                "Case 1 Negative: Duplicate email ile kayıt yapılmamalı");
        } catch (Exception e) {
            // Hata mesajı görünmüyorsa sayfa değişmemeli
            assertTrue(driver.getCurrentUrl().contains("/register"),
                "Case 1 Negative: Duplicate email ile kayıt sayfasında kalınmalı");
        }
    }
}

