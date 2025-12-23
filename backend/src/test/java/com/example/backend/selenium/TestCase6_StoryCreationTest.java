package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Case 6: Story Oluşturma (Yazar için)
 * 
 * Senaryo:
 * - Yazar olarak giriş yap
 * - Yeni story oluştur sayfasına git
 * - Form alanlarını doldur (başlık, içerik, özet, kategori)
 * - Story'yi kaydet
 * - Story'nin başarıyla oluşturulduğunu doğrula
 */
@DisplayName("Test Case 6: Story Oluşturma")
public class TestCase6_StoryCreationTest extends BaseSeleniumTest {
    
    private String testEmail;
    private String testPassword = "Test123456";
    
    @Test
    @DisplayName("Yazar olarak yeni story oluşturulabilmeli")
    public void testStoryCreation() {
        // Önce yazar kullanıcısı oluştur ve giriş yap
        createWriterUserAndLogin();
        
        // Yeni story oluştur sayfasına git
        driver.get(BASE_URL + "/yazar/yeni-yazi");
        waitForPageLoad();
        
        // URL'in doğru olduğunu doğrula
        assertTrue(driver.getCurrentUrl().contains("/yeni-yazi") || 
                   driver.getCurrentUrl().contains("/yazar"),
            "Story oluşturma sayfasına yönlendirilmedi");
        
        // Form alanlarını bul ve doldur
        Random random = new Random();
        String randomSuffix = String.valueOf(random.nextInt(100000));
        String baslik = "Test Story " + randomSuffix;
        String icerik = "Bu bir test story içeriğidir. " + randomSuffix;
        String ozet = "Test story özeti";
        
        // Başlık alanı
        WebElement baslikInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.id("baslik") // veya By.name("baslik") veya By.cssSelector("input[name='baslik']")
            )
        );
        baslikInput.clear();
        baslikInput.sendKeys(baslik);
        
        // İçerik alanı (textarea olabilir)
        try {
            WebElement icerikInput = driver.findElement(By.id("icerik"));
            if (icerikInput == null) {
                icerikInput = driver.findElement(By.name("icerik"));
            }
            icerikInput.clear();
            icerikInput.sendKeys(icerik);
        } catch (Exception e) {
            // İçerik alanı farklı bir selector ile bulunabilir
            WebElement icerikInput = driver.findElement(By.cssSelector("textarea[name='icerik'], textarea[id='icerik']"));
            icerikInput.clear();
            icerikInput.sendKeys(icerik);
        }
        
        // Özet alanı
        try {
            WebElement ozetInput = driver.findElement(By.id("ozet"));
            ozetInput.clear();
            ozetInput.sendKeys(ozet);
        } catch (Exception e) {
            // Özet alanı opsiyonel olabilir
        }
        
        // Kaydet butonuna tıkla
        WebElement submitButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit'], button:contains('Kaydet'), button:contains('Yayınla')")
            )
        );
        assertTrue(submitButton.isEnabled(), "Kaydet butonu aktif olmalı");
        submitButton.click();
        
        // İşlemin tamamlanmasını bekle
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Başarı mesajını veya story detay sayfasına yönlendirmeyi kontrol et
        try {
            // Hata mesajı var mı kontrol et
            try {
                WebElement errorElement = driver.findElement(By.cssSelector(".error, .alert-danger"));
                if (errorElement.isDisplayed() && !errorElement.getText().isEmpty()) {
                    fail("Story oluşturma başarısız: " + errorElement.getText());
                }
            } catch (Exception e) {
                // Hata mesajı yok, devam et
            }
            
            // Başarı mesajı veya yönlendirme kontrolü
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/yazar/yazilar"),
                ExpectedConditions.urlContains("/yazar/dashboard"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".success, .alert-success"))
            ));
            
            // Story'nin oluşturulduğunu doğrula
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("/yazar") || currentUrl.contains("/yazi"),
                "Story oluşturulduktan sonra doğru sayfaya yönlendirilmedi: " + currentUrl);
            
        } catch (org.openqa.selenium.TimeoutException e) {
            fail("Story oluşturma işlemi timeout oldu. Mevcut URL: " + driver.getCurrentUrl());
        }
    }
    
    /**
     * Yazar kullanıcısı oluştur ve giriş yap
     */
    private void createWriterUserAndLogin() {
        Random random = new Random();
        String randomSuffix = String.valueOf(random.nextInt(100000));
        testEmail = "writer" + randomSuffix + "@example.com";
        String kullaniciAdi = "writer" + randomSuffix;
        
        // Kayıt sayfasına git
        driver.get(BASE_URL + "/register");
        waitForPageLoad();
        
        // Form alanlarını doldur
        WebElement adInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("ad"))
        );
        adInput.sendKeys("Writer");
        
        WebElement soyadInput = driver.findElement(By.id("soyad"));
        soyadInput.sendKeys("User");
        
        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys(testEmail);
        
        WebElement kullaniciAdiInput = driver.findElement(By.id("kullaniciAdi"));
        kullaniciAdiInput.sendKeys(kullaniciAdi);
        
        WebElement sifreInput = driver.findElement(By.id("sifre"));
        sifreInput.sendKeys(testPassword);
        
        // Rol seçimi - WRITER
        try {
            WebElement rolSelect = driver.findElement(By.id("rolAdi"));
            if (rolSelect.getTagName().equals("select")) {
                org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(rolSelect);
                select.selectByValue("WRITER");
            }
        } catch (Exception e) {
            // Rol seçimi yapılamazsa devam et
        }
        
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Eğer dashboard'a yönlendirildiyse, çıkış yap
        try {
            if (driver.getCurrentUrl().contains("/dashboard")) {
                // Çıkış yap
                driver.get(BASE_URL + "/logout");
                waitForPageLoad();
            }
        } catch (Exception e) {
            // Hata yok say
        }
        
        // Giriş sayfasına git ve giriş yap
        driver.get(BASE_URL + "/login");
        waitForPageLoad();
        
        WebElement emailLoginInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("email"))
        );
        emailLoginInput.clear();
        emailLoginInput.sendKeys(testEmail);
        
        WebElement sifreLoginInput = driver.findElement(By.id("sifre"));
        sifreLoginInput.clear();
        sifreLoginInput.sendKeys(testPassword);
        
        WebElement loginButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")
            )
        );
        loginButton.click();
        
        // Giriş işleminin tamamlanmasını bekle
        try {
            Thread.sleep(2000);
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlContains("/yazar")
            ));
        } catch (Exception e) {
            // Giriş başarısız olabilir, devam et
        }
    }
}

