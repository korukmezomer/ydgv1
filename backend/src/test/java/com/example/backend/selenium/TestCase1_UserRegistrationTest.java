package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Case 1: Kullanıcı Kaydı (User Registration)
 * 
 * Senaryo:
 * - Ana sayfadan kayıt sayfasına git
 * - Form alanlarını doldur (ad, soyad, email, kullanıcı adı, şifre, rol)
 * - Kayıt butonuna tıkla
 * - Başarılı kayıt sonrası dashboard'a yönlendirildiğini doğrula
 */
@DisplayName("Test Case 1: Kullanıcı Kaydı")
public class TestCase1_UserRegistrationTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Yeni kullanıcı kaydı başarılı olmalı")
    public void testUserRegistration() {
        // Ana sayfadan kayıt sayfasına git
        WebElement registerLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.linkText("Başla"))
        );
        registerLink.click();
        
        waitForPageLoad();
        
        // URL'in /register olduğunu doğrula
        assertTrue(driver.getCurrentUrl().contains("/register"), 
            "Kayıt sayfasına yönlendirilmedi");
        
        // Form alanlarını bul ve doldur
        Random random = new Random();
        String randomSuffix = String.valueOf(random.nextInt(10000));
        String email = "testuser" + randomSuffix + "@example.com";
        String kullaniciAdi = "testuser" + randomSuffix;
        String sifre = "Test123456";
        
        // Ad alanı
        WebElement adInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("ad"))
        );
        adInput.sendKeys("Test");
        
        // Soyad alanı
        WebElement soyadInput = driver.findElement(By.id("soyad"));
        soyadInput.sendKeys("User");
        
        // Email alanı
        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys(email);
        
        // Kullanıcı adı alanı
        WebElement kullaniciAdiInput = driver.findElement(By.id("kullaniciAdi"));
        kullaniciAdiInput.sendKeys(kullaniciAdi);
        
        // Şifre alanı
        WebElement sifreInput = driver.findElement(By.id("sifre"));
        sifreInput.sendKeys(sifre);
        
        // Rol seçimi (USER varsayılan, değiştirmiyoruz)
        WebElement rolSelect = driver.findElement(By.id("rolAdi"));
        assertEquals("USER", rolSelect.getAttribute("value"), 
            "Varsayılan rol USER olmalı");
        
        // Kayıt butonuna tıkla
        WebElement submitButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")
            )
        );
        assertTrue(submitButton.isEnabled(), 
            "Kayıt butonu aktif olmalı");
        submitButton.click();
        
        // Loading durumunu bekle (buton metni değişir)
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.textToBePresentInElement(submitButton, "Kayıt yapılıyor..."),
                ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(submitButton, "Kayıt ol"))
            ));
        } catch (Exception e) {
            // Loading durumu görünmeyebilir, devam et
        }
        
        // API çağrısının tamamlanmasını bekle (daha uzun timeout)
        try {
            // Önce hata mesajı olup olmadığını kontrol et
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
                    fail("Kayıt işlemi başarısız oldu. Hata mesajı: " + errorMessage);
                }
            } catch (Exception e) {
                // Hata mesajı yok, devam et
            }
            
            // Dashboard'a yönlendirilmeyi bekle (daha uzun timeout)
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/reader/dashboard"),
                ExpectedConditions.urlContains("/yazar/dashboard"),
                ExpectedConditions.urlContains("/admin/dashboard")
            ));
            
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("/dashboard"),
                "Kayıt sonrası dashboard'a yönlendirilmedi. Mevcut URL: " + currentUrl);
            
        } catch (org.openqa.selenium.TimeoutException e) {
            // Timeout durumunda daha detaylı hata mesajı
            String currentUrl = driver.getCurrentUrl();
            
            // Hata mesajını bul
            String errorMessage = "Bilinmeyen hata";
            try {
                WebElement errorElement = driver.findElement(By.cssSelector(".auth-error"));
                if (errorElement.isDisplayed()) {
                    errorMessage = errorElement.getText();
                }
            } catch (Exception ex) {
                // Hata mesajı bulunamadı
            }
            
            fail("Kayıt işlemi timeout oldu. " +
                 "Mevcut URL: " + currentUrl + ". " +
                 "Hata mesajı: " + errorMessage + ". " +
                 "Sayfa hala /register sayfasında. " +
                 "Backend ve frontend servislerinin çalıştığından emin olun.");
        }
        
        // Dashboard sayfasında olduğumuzu doğrula
        WebElement dashboardContent = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("body")
            )
        );
        assertNotNull(dashboardContent, "Dashboard sayfası yüklenmedi");
    }
}

