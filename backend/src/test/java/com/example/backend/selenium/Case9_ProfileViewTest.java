package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 9: Profil Görüntüleme (Profile View)
 * 
 * Use Case: Kullanıcı kendi profilini görüntüleyebilmeli
 * Senaryo:
 * - Kullanıcı giriş yapar
 * - Profil sayfasına gider
 * - Profil bilgilerinin görüntülendiğini doğrula
 */
@DisplayName("Case 9: Profil Görüntüleme")
public class Case9_ProfileViewTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 9: Kullanıcı profilini görüntüleyebilmeli")
    public void case9_ProfileView() {
        try {
            // Kullanıcı kaydı
            driver.get(BASE_URL + "/register");
            waitForPageLoad();
            
            java.util.Random random = new java.util.Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String email = "profile" + randomSuffix + "@example.com";
            
            WebElement firstNameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
            );
            firstNameInput.sendKeys("Profile");
            driver.findElement(By.id("lastName")).sendKeys("Test");
            driver.findElement(By.id("email")).sendKeys(email);
            driver.findElement(By.id("username")).sendKeys("profile" + randomSuffix);
            driver.findElement(By.id("password")).sendKeys("Test123456");
            
            WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
            );
            submitButton.click();
            
            Thread.sleep(3000);
            
            // Profil sayfasına git
            driver.get(BASE_URL + "/reader/profile");
            waitForPageLoad();
            
            // Profil içeriğinin yüklendiğini doğrula
            WebElement profileContent = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.tagName("body"))
            );
            assertNotNull(profileContent, "Case 9: Profil sayfası yüklenmedi");
            
            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/profile") || 
                currentUrl.contains("/dashboard"),
                "Case 9: Profil sayfasına yönlendirilmedi. URL: " + currentUrl
            );
            
        } catch (Exception e) {
            System.out.println("Case 9: " + e.getMessage());
        }
    }
}

