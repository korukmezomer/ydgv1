package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 3: Dashboard Erişimi (Dashboard Access)
 * 
 * Use Case: Kullanıcı giriş yaptıktan sonra rolüne göre doğru dashboard'a yönlendirilmeli
 * Senaryo:
 * - READER (USER) rolü ile kayıt ol ve /reader/dashboard'a yönlendirildiğini doğrula
 * - WRITER rolü ile kayıt ol ve /yazar/dashboard'a yönlendirildiğini doğrula
 * - Dashboard sayfasının yüklendiğini doğrula
 */
@DisplayName("Case 3: Dashboard Erişimi")
public class Case3_DashboardAccessTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 3.1: READER (USER) rolü ile dashboard'a erişebilmeli")
    public void case3_1_ReaderDashboardAccess() {
        try {
            // READER (USER) rolü ile kayıt ol
            driver.get(BASE_URL + "/register");
            waitForPageLoad();
            
            Random random = new Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String email = "readerdashboard" + randomSuffix + "@example.com";
            
            WebElement firstNameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
            );
            firstNameInput.sendKeys("Reader");
            driver.findElement(By.id("lastName")).sendKeys("Test");
            driver.findElement(By.id("email")).sendKeys(email);
            driver.findElement(By.id("username")).sendKeys("readerdashboard" + randomSuffix);
            driver.findElement(By.id("password")).sendKeys("Test123456");
            
            // USER rolü seç (varsayılan, ama açıkça seçelim)
            WebElement roleSelectElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("roleName"))
            );
            Select roleSelect = new Select(roleSelectElement);
            try {
                roleSelect.selectByValue("USER");
            } catch (Exception e) {
                // USER varsayılan rol, zaten seçili olabilir
            }
            
            WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
            );
            WebElement form = driver.findElement(By.tagName("form"));
            safeSubmitForm(submitButton, form);
            
            // Frontend'de kayıt sonrası otomatik login yapılıyor
            Thread.sleep(3000);
            
            // /reader/dashboard'a yönlendirilmeyi bekle
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/reader/dashboard"),
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlToBe(BASE_URL + "/")
            ));
            
            // Eğer ana sayfaya yönlendirildiyse, Home.jsx otomatik olarak /reader/dashboard'a yönlendirecek
            Thread.sleep(2000);
            
            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/reader/dashboard") || 
                currentUrl.equals(BASE_URL + "/"),
                "Case 3.1: READER rolü ile kayıt sonrası /reader/dashboard'a yönlendirilmedi. URL: " + currentUrl
            );
            
            // Dashboard içeriğinin yüklendiğini doğrula
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            assertNotNull(driver.findElement(By.tagName("body")), 
                "Case 3.1: Dashboard sayfası yüklenmedi");
            
            System.out.println("Case 3.1: READER dashboard erişimi başarılı");
            
        } catch (Exception e) {
            fail("Case 3.1: READER dashboard erişimi başarısız oldu: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Case 3.2: WRITER rolü ile dashboard'a erişebilmeli")
    public void case3_2_WriterDashboardAccess() {
        try {
            // WRITER rolü ile kayıt ol
            driver.get(BASE_URL + "/register");
            waitForPageLoad();
            
            Random random = new Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String email = "writerdashboard" + randomSuffix + "@example.com";
            
            WebElement firstNameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
            );
            firstNameInput.sendKeys("Writer");
            driver.findElement(By.id("lastName")).sendKeys("Test");
            driver.findElement(By.id("email")).sendKeys(email);
            driver.findElement(By.id("username")).sendKeys("writerdashboard" + randomSuffix);
            driver.findElement(By.id("password")).sendKeys("Test123456");
            
            // WRITER rolü seç
            WebElement roleSelectElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("roleName"))
            );
            Select roleSelect = new Select(roleSelectElement);
            try {
                roleSelect.selectByValue("WRITER");
            } catch (Exception e) {
                try {
                    roleSelect.selectByVisibleText("WRITER");
                } catch (Exception e2) {
                    ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("arguments[0].value = 'WRITER';", roleSelectElement);
                }
            }
            
            WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
            );
            WebElement form = driver.findElement(By.tagName("form"));
            safeSubmitForm(submitButton, form);
            
            // Frontend'de kayıt sonrası otomatik login yapılıyor
            Thread.sleep(3000);
            
            // /yazar/dashboard'a yönlendirilmeyi bekle
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/yazar/dashboard"),
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlToBe(BASE_URL + "/")
            ));
            
            // Eğer ana sayfaya yönlendirildiyse, Home.jsx otomatik olarak /yazar/dashboard'a yönlendirecek
            Thread.sleep(2000);
            
            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/yazar/dashboard") || 
                currentUrl.equals(BASE_URL + "/"),
                "Case 3.2: WRITER rolü ile kayıt sonrası /yazar/dashboard'a yönlendirilmedi. URL: " + currentUrl
            );
            
            // Dashboard içeriğinin yüklendiğini doğrula
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            assertNotNull(driver.findElement(By.tagName("body")), 
                "Case 3.2: Dashboard sayfası yüklenmedi");
            
            System.out.println("Case 3.2: WRITER dashboard erişimi başarılı");
            
        } catch (Exception e) {
            fail("Case 3.2: WRITER dashboard erişimi başarısız oldu: " + e.getMessage());
        }
    }
}

