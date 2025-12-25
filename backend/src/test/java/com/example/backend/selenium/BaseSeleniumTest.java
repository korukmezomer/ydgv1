package com.example.backend.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Base class for Selenium tests
 * Provides common setup and teardown methods
 */
public abstract class BaseSeleniumTest {
    
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected static final String BASE_URL = "http://localhost:5173";
    protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30); // Daha uzun timeout
    
    @BeforeEach
    public void setUp() {
        // Setup ChromeDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        
        // CI/CD ortamı için headless mod kontrolü
        String headless = System.getProperty("selenium.headless", "false");
        if ("true".equalsIgnoreCase(headless) || System.getenv("CI") != null) {
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
        } else {
            options.addArguments("--start-maximized");
        }
        
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-extensions");
        options.addArguments("--window-size=1920,1080");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        
        // Navigate to base URL
        driver.get(BASE_URL);
    }
    
    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    /**
     * Helper method to wait for page to load
     */
    protected void waitForPageLoad() {
        try {
            Thread.sleep(1000); // Wait for React to render
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Güvenilir buton tıklama metodu
     * Önce normal click dener, başarısız olursa JavaScript executor kullanır
     */
    protected void safeClick(WebElement element) {
        try {
            // Önce butonun görünür ve tıklanabilir olduğundan emin ol
            wait.until(ExpectedConditions.elementToBeClickable(element));
            
            // Scroll to element
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
            Thread.sleep(200);
            
            // Önce normal click dene
            try {
                element.click();
            } catch (Exception e) {
                // Normal click başarısız olursa JavaScript executor kullan
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            }
        } catch (Exception e) {
            // Son çare olarak JavaScript executor kullan
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }
    
    /**
     * Form submit butonuna güvenilir şekilde tıkla
     * Önce buton tıklama dener, başarısız olursa Enter tuşu ile submit yapar
     */
    protected void safeSubmitForm(WebElement submitButton, WebElement formElement) {
        try {
            // Önce butonun görünür ve tıklanabilir olduğundan emin ol
            wait.until(ExpectedConditions.elementToBeClickable(submitButton));
            
            // Scroll to button
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitButton);
            Thread.sleep(200);
            
            // Buton tıklanabilir mi kontrol et
            if (submitButton.isEnabled() && submitButton.isDisplayed()) {
                try {
                    // Önce normal click dene
                    submitButton.click();
                } catch (Exception e) {
                    // Normal click başarısız olursa JavaScript executor kullan
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);
                }
            } else {
                // Buton tıklanabilir değilse, form elementine Enter tuşu gönder
                if (formElement != null) {
                    formElement.sendKeys(Keys.ENTER);
                } else {
                    // Form element bulunamazsa, aktif elemente Enter gönder
                    new Actions(driver).sendKeys(Keys.ENTER).perform();
                }
            }
        } catch (Exception e) {
            // Son çare: JavaScript ile form submit
            if (formElement != null) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].submit();", formElement);
            } else {
                // Form bulunamazsa Enter tuşu gönder
                new Actions(driver).sendKeys(Keys.ENTER).perform();
            }
        }
    }
    
    /**
     * Submit butonunu bul ve güvenilir şekilde tıkla
     */
    protected void clickSubmitButton() {
        try {
            WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    org.openqa.selenium.By.cssSelector("button[type='submit']")
                )
            );
            safeClick(submitButton);
        } catch (Exception e) {
            // Submit butonu bulunamazsa, form'a Enter tuşu gönder
            try {
                WebElement form = driver.findElement(org.openqa.selenium.By.tagName("form"));
                if (form != null) {
                    form.sendKeys(Keys.ENTER);
                }
            } catch (Exception ex) {
                // Form da bulunamazsa, aktif elemente Enter gönder
                new Actions(driver).sendKeys(Keys.ENTER).perform();
            }
        }
    }
}

