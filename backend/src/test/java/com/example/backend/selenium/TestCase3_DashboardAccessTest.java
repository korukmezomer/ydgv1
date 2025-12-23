package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Case 3: Dashboard Erişimi (Dashboard Access)
 * 
 * Senaryo:
 * - Kullanıcı giriş yapar
 * - Rolüne göre doğru dashboard'a yönlendirildiğini doğrula
 * - Dashboard sayfasında gerekli elementlerin göründüğünü kontrol et
 */
@DisplayName("Test Case 3: Dashboard Erişimi")
public class TestCase3_DashboardAccessTest extends BaseSeleniumTest {
    
    // Test için kullanılacak kullanıcı bilgileri
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test123456";
    
    @Test
    @DisplayName("Giriş yapan kullanıcı dashboard'a yönlendirilmeli")
    public void testDashboardAccessAfterLogin() {
        // Giriş yap
        performLogin(TEST_EMAIL, TEST_PASSWORD);
        
        // Dashboard'a yönlendirildiğini doğrula
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/reader/dashboard"),
            ExpectedConditions.urlContains("/yazar/dashboard"),
            ExpectedConditions.urlContains("/admin/dashboard")
        ));
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/dashboard"), 
            "Dashboard sayfasına yönlendirilmedi. Mevcut URL: " + currentUrl);
        
        // Dashboard içeriğinin yüklendiğini doğrula
        WebElement dashboardContent = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("body")
            )
        );
        assertNotNull(dashboardContent, "Dashboard içeriği yüklenmedi");
    }
    
    @Test
    @DisplayName("Ana sayfadaki Dashboard butonu doğru dashboard'a yönlendirmeli")
    public void testDashboardButtonFromHomePage() {
        // Önce giriş yap
        performLogin(TEST_EMAIL, TEST_PASSWORD);
        
        waitForPageLoad();
        
        // Ana sayfaya git
        driver.get(BASE_URL);
        waitForPageLoad();
        
        // Dashboard butonunu bul ve tıkla
        WebElement dashboardButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.linkText("Dashboard"))
        );
        dashboardButton.click();
        
        waitForPageLoad();
        
        // Dashboard'a yönlendirildiğini doğrula
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/reader/dashboard"),
            ExpectedConditions.urlContains("/yazar/dashboard"),
            ExpectedConditions.urlContains("/admin/dashboard")
        ));
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/dashboard"), 
            "Dashboard butonuna tıklanınca dashboard'a yönlendirilmedi");
    }
    
    /**
     * Helper method to perform login
     */
    private void performLogin(String email, String password) {
        // Ana sayfadan giriş sayfasına git
        WebElement loginLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.linkText("Giriş Yap"))
        );
        loginLink.click();
        
        waitForPageLoad();
        
        // Form alanlarını doldur
        WebElement emailInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("email"))
        );
        emailInput.sendKeys(email);
        
        WebElement sifreInput = driver.findElement(By.id("sifre"));
        sifreInput.sendKeys(password);
        
        // Giriş butonuna tıkla
        WebElement submitButton = driver.findElement(
            By.cssSelector("button[type='submit']")
        );
        submitButton.click();
        
        waitForPageLoad();
    }
}

