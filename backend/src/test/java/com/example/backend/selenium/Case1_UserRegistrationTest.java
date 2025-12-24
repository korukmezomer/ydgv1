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
        submitButton.click();
        
        // API çağrısının tamamlanmasını bekle
        try {
            Thread.sleep(3000);
            
            // Dashboard'a yönlendirilmeyi bekle
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/reader/dashboard"),
                ExpectedConditions.urlContains("/yazar/dashboard"),
                ExpectedConditions.urlContains("/admin/dashboard"),
                ExpectedConditions.urlContains("/dashboard")
            ));
            
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("/dashboard") || currentUrl.equals(BASE_URL + "/"),
                "Case 1: Kayıt sonrası dashboard'a yönlendirilmedi. Mevcut URL: " + currentUrl);
            
        } catch (Exception e) {
            fail("Case 1: Kayıt işlemi başarısız oldu: " + e.getMessage());
        }
    }
}

