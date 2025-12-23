package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.sql.Driver;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("test case1: ben kullanıcı kayıdı ")
public class TestCase1_UserRegistrationTestben extends BaseSeleniumTest {

    @Test
    @DisplayName("yeni kullanıcı kayıdı başarılımı ")
    public void testUserRegistration() {
        WebElement registerLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Başla"))
        );
        registerLink.click();

        waitForPageLoad();

        assertTrue(driver.getCurrentUrl().contains("/register"),"kayıt sayfasına yönledirilmedi");

        Random random =new Random();
        String randomSuffix = String.valueOf(random.nextInt(10000));
        String email = "testuser" + randomSuffix + "@example.com";
        String KullaniciAdi = "testuser" + randomSuffix;
        String sifre = "Test123456";

        WebElement adInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("ad"))
        );
        adInput.sendKeys("Test");

        WebElement soyadInput = driver.findElement(By.id("soyad"));
        soyadInput.sendKeys("User");

        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys(email);

        WebElement kullaniciAdiInput = driver.findElement(By.id("kullaniciAdi"));
        kullaniciAdiInput.sendKeys(KullaniciAdi);


        WebElement sifreInput = driver.findElement(By.id("sifre"));
        sifreInput.sendKeys(sifre);


        WebElement rolSelect = driver.findElement(By.id("rolAdi"));
        assertEquals("USER", rolSelect.getAttribute("value"),
                "Varsayılan rol USER olmalı");


        WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[type='submit']")
                ));

        assertTrue(submitButton.isEnabled(),
                "Kayıt butonu aktif olmalı");
        submitButton.click();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.textToBePresentInElement(submitButton,"kayıt yapılıyor..."),
                ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(submitButton, "Kayıt ol"))

        ));




        try {
            try {
                Thread.sleep(2000);
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
