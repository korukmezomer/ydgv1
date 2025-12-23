package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Case 4: İçerik Oluşturma (Content Creation)
 * 
 * Senaryo:
 * - Yazar olarak giriş yap
 * - "Yaz" butonuna tıkla veya /reader/new-story sayfasına git
 * - Başlık alanına başlık gir
 * - İçerik blokları ekle (metin, başlık, kod, resim vb.)
 * - İçeriği kaydet veya yayınla
 */
@DisplayName("Test Case 4: İçerik Oluşturma")
public class TestCase4_ContentCreationTest extends BaseSeleniumTest {
    
    // Test için kullanılacak kullanıcı bilgileri
    private String writerEmail;
    private String writerPassword = "Test123456";
    
    @Test
    @DisplayName("Yazar yeni yazı oluşturup yayınlayabilmeli")
    public void testCreateAndPublishArticle() {
        // Yazar kullanıcı oluştur ve giriş yap
        createWriterUserAndLogin();
        
        waitForPageLoad();
        
        // Yeni içerik sayfasına git
        driver.get(BASE_URL + "/reader/new-story");
        waitForPageLoad();
        
        // Yeni içerik sayfasında olduğumuzu doğrula
        assertTrue(driver.getCurrentUrl().contains("/reader/new-story"),
            "Yeni içerik sayfasına yönlendirilmedi. Mevcut URL: " + driver.getCurrentUrl());
        
        // Başlık alanını bul ve başlık gir
        WebElement titleInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".story-title-input, input[placeholder='Başlık']")
            )
        );
        String testTitle = "Selenium Test Yazısı - " + System.currentTimeMillis();
        titleInput.clear();
        titleInput.sendKeys(testTitle);
        
        waitForPageLoad();
        
        // İlk metin bloğunu bul ve içerik yaz
        WebElement firstTextBlock = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".block-textarea, textarea")
            )
        );
        firstTextBlock.click();
        firstTextBlock.clear();
        firstTextBlock.sendKeys("Bu bir Selenium test yazısıdır. ");
        firstTextBlock.sendKeys("Yazı oluşturma ve yayınlama testi başarıyla tamamlanmıştır.");
        
        waitForPageLoad();
        
        // Başlık ve içeriğin girildiğini doğrula
        assertFalse(titleInput.getAttribute("value").isEmpty(), 
            "Başlık girilmedi");
        String contentText = firstTextBlock.getAttribute("value") != null ? 
            firstTextBlock.getAttribute("value") : firstTextBlock.getText();
        assertFalse(contentText.isEmpty(), 
            "İçerik girilmedi");
        
        // Yayınla butonunu bul ve tıkla
        WebElement publishButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".publish-button")
            )
        );
        assertTrue(publishButton.isEnabled(), 
            "Yayınla butonu aktif olmalı (başlık girildi)");
        
        publishButton.click();
        
        // Yayınlama işleminin tamamlanmasını bekle
        try {
            Thread.sleep(3000); // API çağrısı için bekle
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Yayınlama sonrası haber detay sayfasına yönlendirildiğini doğrula
        try {
            wait.until(ExpectedConditions.urlContains("/haberler/"));
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("/haberler/"),
                "Yayınlama sonrası haber detay sayfasına yönlendirilmedi. Mevcut URL: " + currentUrl);
            
            // Haber detay sayfasında başlığın göründüğünü doğrula
            WebElement articleTitle = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("h1, .article-title, .haber-baslik")
                )
            );
            assertNotNull(articleTitle, "Haber başlığı görünmüyor");
            
        } catch (org.openqa.selenium.TimeoutException e) {
            // Yayınlama başarısız olabilir, hata mesajını kontrol et
            String currentUrl = driver.getCurrentUrl();
            fail("Yayınlama işlemi timeout oldu. " +
                 "Mevcut URL: " + currentUrl + ". " +
                 "Yayınlama butonu tıklandı ancak yönlendirme yapılmadı.");
        }
    }
    
    @Test
    @DisplayName("Yazar yeni içerik oluşturabilmeli")
    public void testContentCreation() {
        // Yazar kullanıcı oluştur ve giriş yap
        createWriterUserAndLogin();
        
        waitForPageLoad();
        
        // Yeni içerik sayfasına git
        driver.get(BASE_URL + "/reader/new-story");
        waitForPageLoad();
        
        // Yeni içerik sayfasında olduğumuzu doğrula
        assertTrue(driver.getCurrentUrl().contains("/reader/new-story"),
            "Yeni içerik sayfasına yönlendirilmedi");
        
        // Başlık alanını bul ve başlık gir
        WebElement titleInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".story-title-input, input[placeholder='Başlık']")
            )
        );
        String testTitle = "Selenium Test İçeriği - " + System.currentTimeMillis();
        titleInput.clear();
        titleInput.sendKeys(testTitle);
        
        waitForPageLoad();
        
        // İçerik alanını bul (textarea)
        WebElement contentArea = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".block-textarea, textarea")
            )
        );
        
        // İçerik yaz
        contentArea.click();
        contentArea.clear();
        contentArea.sendKeys("Bu bir Selenium test içeriğidir. ");
        contentArea.sendKeys("İçerik oluşturma testi başarıyla tamamlandı.");
        
        waitForPageLoad();
        
        // Başlık ve içeriğin girildiğini doğrula
        assertFalse(titleInput.getAttribute("value").isEmpty(), 
            "Başlık girilmedi");
        String contentText = contentArea.getAttribute("value") != null ? 
            contentArea.getAttribute("value") : contentArea.getText();
        assertFalse(contentText.isEmpty(),
            "İçerik girilmedi");
        
        // Yayınla butonunun göründüğünü doğrula
        WebElement publishButton = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".publish-button")
            )
        );
        assertNotNull(publishButton, "Yayınla butonu bulunamadı");
        assertTrue(publishButton.isEnabled(), 
            "Yayınla butonu aktif olmalı (başlık girildi)");
    }
    
    @Test
    @DisplayName("Yazar içerik sayfasına erişebilmeli")
    public void testAccessToNewStoryPage() {
        // Yazar kullanıcı oluştur ve giriş yap
        createWriterUserAndLogin();
        
        waitForPageLoad();
        
        // Direkt olarak yeni içerik sayfasına git
        driver.get(BASE_URL + "/reader/new-story");
        waitForPageLoad();
        
        // Sayfanın yüklendiğini doğrula
        WebElement pageContent = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("body")
            )
        );
        assertNotNull(pageContent, "Yeni içerik sayfası yüklenmedi");
        
        // Başlık alanının göründüğünü doğrula
        WebElement titleInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".story-title-input, input[placeholder='Başlık']")
            )
        );
        assertNotNull(titleInput, "Başlık alanı bulunamadı");
    }
    
    /**
     * Helper method to create writer user and login
     */
    private void createWriterUserAndLogin() {
        // Önce kullanıcı oluştur (WRITER rolü ile)
        java.util.Random random = new java.util.Random();
        String randomSuffix = String.valueOf(random.nextInt(100000));
        writerEmail = "writer" + randomSuffix + "@example.com";
        String kullaniciAdi = "writer" + randomSuffix;
        
        // Kayıt sayfasına git
        driver.get(BASE_URL + "/register");
        waitForPageLoad();
        
        // Form alanlarını doldur
        WebElement adInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("ad"))
        );
        adInput.sendKeys("Test");
        
        WebElement soyadInput = driver.findElement(By.id("soyad"));
        soyadInput.sendKeys("Writer");
        
        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys(writerEmail);
        
        WebElement kullaniciAdiInput = driver.findElement(By.id("kullaniciAdi"));
        kullaniciAdiInput.sendKeys(kullaniciAdi);
        
        WebElement sifreInput = driver.findElement(By.id("sifre"));
        sifreInput.sendKeys(writerPassword);
        
        // Rol seçimi - WRITER seç
        WebElement rolSelect = driver.findElement(By.id("rolAdi"));
        org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(rolSelect);
        select.selectByValue("WRITER");
        
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
            // Kayıt başarısız olabilir, hata mesajını kontrol et
            try {
                WebElement errorElement = driver.findElement(By.cssSelector(".auth-error"));
                if (errorElement.isDisplayed()) {
                    String errorMessage = errorElement.getText();
                    fail("Yazar kullanıcı oluşturulamadı. Hata mesajı: " + errorMessage);
                }
            } catch (Exception ex) {
                fail("Yazar kullanıcı oluşturulamadı ve hata mesajı bulunamadı");
            }
        }
    }
}

