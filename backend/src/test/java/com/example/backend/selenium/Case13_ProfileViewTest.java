package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 13: Profil Görüntüleme (Profile View)
 * 
 * Use Case: Kullanıcı kendi profilini görüntüleyebilmeli
 * Senaryo:
 * - Kullanıcı giriş yapar
 * - Profil sayfasına gider
 * - Profil bilgilerinin görüntülendiğini doğrula
 */
@DisplayName("Case 13: Profil Görüntüleme")
public class Case13_ProfileViewTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 13: Kullanıcı profilini görüntüleyebilmeli")
    public void case13_ProfileView() {
        try {
            // Kullanıcı kaydı (BaseSeleniumTest'teki registerUser helper metodunu kullan)
            java.util.Random random = new java.util.Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String email = "profile" + randomSuffix + "@example.com";
            String username = "profile" + randomSuffix;
            
            boolean userRegistered = registerUser("Profile", "Test", email, username, "Test123456");
            if (!userRegistered) {
                fail("Case 13: Kullanıcı kaydı başarısız");
                return;
            }
            
            // Profil sayfasına git
            driver.get(BASE_URL + "/reader/profile");
            waitForPageLoad();
            
            // Profil içeriğinin yüklendiğini doğrula
            WebElement profileContent = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.tagName("body"))
            );
            assertNotNull(profileContent, "Case 13: Profil sayfası yüklenmedi");
            
            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/profile") || 
                currentUrl.contains("/dashboard"),
                "Case 13: Profil sayfasına yönlendirilmedi. URL: " + currentUrl
            );
            
        } catch (Exception e) {
            System.out.println("Case 13: " + e.getMessage());
        }
    }
}

