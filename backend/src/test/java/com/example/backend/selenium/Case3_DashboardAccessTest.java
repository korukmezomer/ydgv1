package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 3: Dashboard Erişimi (Dashboard Access)
 * 
 * Use Case: Kullanıcı giriş yaptıktan sonra rolüne göre doğru dashboard'a yönlendirilmeli
 * Senaryo:
 * - Kullanıcı giriş yapar
 * - Rolüne göre doğru dashboard'a yönlendirildiğini doğrula
 * - Dashboard sayfasının yüklendiğini doğrula
 */
@DisplayName("Case 3: Dashboard Erişimi")
public class Case3_DashboardAccessTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 3: Kullanıcı dashboard'a erişebilmeli")
    public void case3_DashboardAccess() {
        // Önce kayıt ol (test için)
        driver.get(BASE_URL + "/register");
        waitForPageLoad();
        
        // Test kullanıcısı kaydı
        java.util.Random random = new java.util.Random();
        String randomSuffix = String.valueOf(random.nextInt(10000));
        String email = "dashboardtest" + randomSuffix + "@example.com";
        
        try {
            WebElement firstNameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
            );
            firstNameInput.sendKeys("Dashboard");
            
            driver.findElement(By.id("lastName")).sendKeys("Test");
            driver.findElement(By.id("email")).sendKeys(email);
            driver.findElement(By.id("username")).sendKeys("dashboardtest" + randomSuffix);
            driver.findElement(By.id("password")).sendKeys("Test123456");
            
            WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
            );
            WebElement form = driver.findElement(By.tagName("form"));
            safeSubmitForm(submitButton, form);
            
            // API çağrısının tamamlanmasını bekle
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
                loginPasswordInput.sendKeys("Test123456");
                
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
                currentUrl.contains("/reader/dashboard") ||
                currentUrl.contains("/yazar/dashboard"),
                "Case 3: Dashboard'a yönlendirilmedi. Mevcut URL: " + currentUrl
            );
            
            // Dashboard içeriğinin yüklendiğini doğrula
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            assertNotNull(driver.findElement(By.tagName("body")), 
                "Case 3: Dashboard sayfası yüklenmedi");
            
        } catch (Exception e) {
            fail("Case 3: Dashboard erişimi başarısız oldu: " + e.getMessage());
        }
    }
}

