package com.example.backend.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Base class for Selenium tests
 * Provides common setup and teardown methods
 */
public abstract class BaseSeleniumTest {
    
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected static final String BASE_URL = "http://localhost:5173";
    protected static final String BACKEND_URL = "http://localhost:8080";
    protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30); // Daha uzun timeout
    
    // Test veritabanı bağlantı bilgileri
    private static final String TEST_DB_URL = System.getProperty("test.db.url", "jdbc:postgresql://localhost:5433/yazilimdogrulama_test");
    private static final String TEST_DB_USER = System.getProperty("test.db.user", "postgres");
    private static final String TEST_DB_PASSWORD = System.getProperty("test.db.password", "postgres");
    
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
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
        
        // CDP uyarılarını azaltmak için
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-logging");
        options.addArguments("--log-level=3"); // Sadece fatal hataları göster
        
        // System property ile Selenium log seviyesini ayarla
        System.setProperty("webdriver.chrome.silentOutput", "true");
        System.setProperty("org.openqa.selenium.chrome.driver.silent", "true");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        
        // Önce localStorage ve cookies'i temizle (önceki oturumları temizlemek için)
        driver.get(BASE_URL);
        try {
            Thread.sleep(500); // Sayfanın yüklenmesini bekle
            ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
            ((JavascriptExecutor) driver).executeScript("window.sessionStorage.clear();");
            driver.manage().deleteAllCookies();
            // Sayfayı yeniden yükle
            driver.navigate().refresh();
            Thread.sleep(500);
        } catch (Exception e) {
            // Temizleme başarısız olursa devam et
            System.out.println("LocalStorage/Cookie temizleme hatası: " + e.getMessage());
        }
        
        // Ana sayfaya git ve oturum kontrolü yap
        driver.get(BASE_URL + "/");
        waitForPageLoad();
        
        // Eğer dashboard'a yönlendirildiyse, logout yap
        try {
            Thread.sleep(2000); // Sayfanın yüklenmesini bekle (Home.jsx useEffect dashboard'a yönlendirebilir)
            String currentUrl = driver.getCurrentUrl();
            
            // Dashboard'da mıyız kontrol et
            if (currentUrl.contains("/dashboard") || currentUrl.contains("/reader/dashboard") || 
                currentUrl.contains("/yazar/dashboard") || currentUrl.contains("/admin/dashboard")) {
                // Logout yap - ProfileDropdown'dan logout yap
                logout();
                
                // Tekrar ana sayfaya git ve kontrol et
                driver.get(BASE_URL + "/");
                waitForPageLoad();
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            // Hata olursa devam et
            System.out.println("Oturum kontrolü hatası: " + e.getMessage());
        }
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
    
    /**
     * Test veritabanına bağlantı oluştur
     */
    protected Connection getTestDatabaseConnection() throws SQLException {
        return DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
    }
    
    /**
     * Test veritabanında admin kullanıcısı oluştur veya mevcut olanı kullan
     * Bu method test veritabanına direkt JDBC ile bağlanır
     */
    protected AdminCredentials ensureAdminUserExists() {
        String adminEmail = System.getProperty("test.admin.email", "admin@test.com");
        String adminPassword = System.getProperty("test.admin.password", "admin123");
        String adminUsername = System.getProperty("test.admin.username", "admin");
        
        try (Connection conn = getTestDatabaseConnection()) {
            // Önce admin kullanıcısının var olup olmadığını kontrol et
            String checkUserSql = "SELECT id FROM kullanicilar WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkUserSql)) {
                stmt.setString(1, adminEmail);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Kullanıcı zaten var, admin rolünü kontrol et
                        Long userId = rs.getLong("id");
                        if (hasAdminRole(conn, userId)) {
                            return new AdminCredentials(adminEmail, adminPassword);
                        } else {
                            // Kullanıcı var ama admin rolü yok, ekle
                            addAdminRole(conn, userId);
                            return new AdminCredentials(adminEmail, adminPassword);
                        }
                    }
                }
            }
            
            // Kullanıcı yok, oluştur
            // Önce ADMIN rolünün ID'sini al
            Long adminRoleId = getRoleId(conn, "ADMIN");
            if (adminRoleId == null) {
                // ADMIN rolü yok, oluştur
                adminRoleId = createRole(conn, "ADMIN", "Yönetici - Tüm yetkilere sahip");
            }
            
            // Şifreyi encode et
            String encodedPassword = passwordEncoder.encode(adminPassword);
            
            // Kullanıcıyı oluştur
            String insertUserSql = "INSERT INTO kullanicilar (email, sifre, ad, soyad, kullanici_adi, is_active, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
            Long userId;
            try (PreparedStatement stmt = conn.prepareStatement(insertUserSql)) {
                stmt.setString(1, adminEmail);
                stmt.setString(2, encodedPassword);
                stmt.setString(3, "Admin");
                stmt.setString(4, "User");
                stmt.setString(5, adminUsername);
                stmt.setBoolean(6, true);
                LocalDateTime now = LocalDateTime.now();
                stmt.setObject(7, now);
                stmt.setObject(8, now);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getLong("id");
                    } else {
                        throw new SQLException("Kullanıcı oluşturulamadı");
                    }
                }
            }
            
            // Admin rolünü kullanıcıya ekle
            String insertUserRoleSql = "INSERT INTO kullanici_roller (kullanici_id, rol_id) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertUserRoleSql)) {
                stmt.setLong(1, userId);
                stmt.setLong(2, adminRoleId);
                stmt.executeUpdate();
            }
            
            System.out.println("Admin kullanıcısı test veritabanında oluşturuldu: " + adminEmail);
            return new AdminCredentials(adminEmail, adminPassword);
            
        } catch (SQLException e) {
            System.err.println("Admin kullanıcısı oluşturulurken hata: " + e.getMessage());
            e.printStackTrace();
            // Hata durumunda varsayılan değerleri döndür
            return new AdminCredentials(adminEmail, adminPassword);
        }
    }
    
    /**
     * Kullanıcının admin rolüne sahip olup olmadığını kontrol et
     */
    private boolean hasAdminRole(Connection conn, Long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM kullanici_roller ur " +
                     "JOIN roller r ON ur.rol_id = r.id " +
                     "WHERE ur.kullanici_id = ? AND r.rol_adi = 'ADMIN' AND r.is_active = true";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Kullanıcıya admin rolü ekle
     */
    private void addAdminRole(Connection conn, Long userId) throws SQLException {
        Long adminRoleId = getRoleId(conn, "ADMIN");
        if (adminRoleId == null) {
            adminRoleId = createRole(conn, "ADMIN", "Yönetici - Tüm yetkilere sahip");
        }
        
        String sql = "INSERT INTO kullanici_roller (kullanici_id, rol_id) VALUES (?, ?) " +
                     "ON CONFLICT DO NOTHING";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, adminRoleId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Rol ID'sini al
     */
    private Long getRoleId(Connection conn, String roleName) throws SQLException {
        String sql = "SELECT id FROM roller WHERE rol_adi = ? AND is_active = true";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roleName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }
        return null;
    }
    
    /**
     * Rol oluştur
     */
    private Long createRole(Connection conn, String roleName, String description) throws SQLException {
        String sql = "INSERT INTO roller (rol_adi, aciklama, is_active, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roleName);
            stmt.setString(2, description);
            stmt.setBoolean(3, true);
            LocalDateTime now = LocalDateTime.now();
            stmt.setObject(4, now);
            stmt.setObject(5, now);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }
        throw new SQLException("Rol oluşturulamadı: " + roleName);
    }
    
    /**
     * Test için admin kullanıcısı oluştur veya mevcut admin kullanıcısını kullan
     * Not: Gerçek uygulamada admin kullanıcıları manuel olarak oluşturulmalı
     * Test için: Önceden oluşturulmuş bir admin kullanıcısı kullan veya
     * Backend'de admin oluşturma endpoint'i kullan
     */
    
    /**
     * Kullanıcı girişi yap
     */
    protected void loginUser(String email, String password) {
        driver.get(BASE_URL + "/login");
        waitForPageLoad();
        
        // Eğer zaten dashboard'daysa önce logout yap
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("/dashboard")) {
            logout();
            driver.get(BASE_URL + "/login");
            waitForPageLoad();
        }
        
        WebElement emailInput = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("email"))
        );
        emailInput.sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        
        WebElement form = driver.findElement(By.cssSelector("form"));
        WebElement submitButton = form.findElement(By.cssSelector("button[type='submit']"));
        safeSubmitForm(submitButton, form);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Kayıt sonrası dashboard'a yönlendirilme durumunu handle et
     * Eğer dashboard'daysa logout yap
     */
    protected void handlePostRegistrationRedirect() {
        try {
            Thread.sleep(2000); // Sayfanın yüklenmesini bekle
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("/dashboard") || currentUrl.contains("/reader/dashboard") || 
                currentUrl.contains("/yazar/dashboard") || currentUrl.contains("/admin/dashboard")) {
                logout();
            }
        } catch (Exception e) {
            System.out.println("Post-registration redirect kontrolü hatası: " + e.getMessage());
        }
    }
    
    /**
     * Kullanıcı kaydı yap (READER rolü ile)
     * @param firstName Ad
     * @param lastName Soyad
     * @param email Email
     * @param username Kullanıcı adı
     * @param password Şifre
     * @return Kayıt başarılı ise true
     */
    protected boolean registerUser(String firstName, String lastName, String email, String username, String password) {
        try {
            driver.get(BASE_URL + "/register");
            waitForPageLoad();
            
            WebElement firstNameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
            );
            firstNameInput.sendKeys(firstName);
            driver.findElement(By.id("lastName")).sendKeys(lastName);
            driver.findElement(By.id("email")).sendKeys(email);
            driver.findElement(By.id("username")).sendKeys(username);
            driver.findElement(By.id("password")).sendKeys(password);
            
            // Role seçimi - READER (varsayılan, seçmeye gerek yok)
            
            WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
            );
            WebElement form = driver.findElement(By.tagName("form"));
            safeSubmitForm(submitButton, form);
            Thread.sleep(3000);
            
            // Kayıt başarılı kontrolü
            String currentUrl = driver.getCurrentUrl();
            return currentUrl.contains("/dashboard") || currentUrl.contains("/reader/dashboard") || 
                   currentUrl.equals(BASE_URL + "/") || !currentUrl.contains("/register");
        } catch (Exception e) {
            System.err.println("Kullanıcı kaydı hatası: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Writer kaydı yap (WRITER rolü ile)
     * @param firstName Ad
     * @param lastName Soyad
     * @param email Email
     * @param username Kullanıcı adı
     * @param password Şifre
     * @return Kayıt başarılı ise true
     */
    protected boolean registerWriter(String firstName, String lastName, String email, String username, String password) {
        try {
            driver.get(BASE_URL + "/register");
            waitForPageLoad();
            Thread.sleep(2000);
            
            // Form alanlarını temizle ve doldur
            WebElement firstNameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
            );
            firstNameInput.clear();
            firstNameInput.sendKeys(firstName);
            Thread.sleep(500);
            
            WebElement lastNameInput = driver.findElement(By.id("lastName"));
            lastNameInput.clear();
            lastNameInput.sendKeys(lastName);
            Thread.sleep(500);
            
            WebElement emailInput = driver.findElement(By.id("email"));
            emailInput.clear();
            emailInput.sendKeys(email);
            Thread.sleep(500);
            
            WebElement usernameInput = driver.findElement(By.id("username"));
            usernameInput.clear();
            usernameInput.sendKeys(username);
            Thread.sleep(500);
            
            WebElement passwordInput = driver.findElement(By.id("password"));
            passwordInput.clear();
            passwordInput.sendKeys(password);
            Thread.sleep(500);
            
            // Role seçimi - WRITER
            try {
                WebElement roleSelectElement = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.id("roleName"))
                );
                org.openqa.selenium.support.ui.Select roleSelect = new org.openqa.selenium.support.ui.Select(roleSelectElement);
                try {
                    roleSelect.selectByValue("WRITER");
                    Thread.sleep(500);
                } catch (Exception e) {
                    try {
                        roleSelect.selectByVisibleText("WRITER");
                        Thread.sleep(500);
                    } catch (Exception e2) {
                        ((JavascriptExecutor) driver)
                            .executeScript("arguments[0].value = 'WRITER';", roleSelectElement);
                        Thread.sleep(500);
                    }
                }
            } catch (Exception e) {
                System.out.println("Role select bulunamadı veya seçilemedi: " + e.getMessage());
                // Role seçimi başarısız olsa bile devam et
            }
            
            // Hata mesajlarını kontrol et
            try {
                java.util.List<WebElement> errorMessages = driver.findElements(
                    By.cssSelector(".error, .text-danger, [role='alert']")
                );
                for (WebElement error : errorMessages) {
                    if (error.isDisplayed()) {
                        String errorText = error.getText();
                        if (errorText.contains("zaten") || errorText.contains("already") || 
                            errorText.contains("kullanılıyor") || errorText.contains("exists")) {
                            System.out.println("Writer kaydı hatası: " + errorText);
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                // Hata mesajı kontrolü başarısız olsa bile devam et
            }
            
            WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
            );
            WebElement form = driver.findElement(By.tagName("form"));
            safeSubmitForm(submitButton, form);
            Thread.sleep(5000); // Kayıt işlemi için daha uzun bekle
            
            // Kayıt başarılı kontrolü
            String currentUrl = driver.getCurrentUrl();
            System.out.println("Writer kaydı sonrası URL: " + currentUrl);
            
            // Hata mesajlarını tekrar kontrol et
            try {
                java.util.List<WebElement> errorMessages = driver.findElements(
                    By.cssSelector(".error, .text-danger, [role='alert']")
                );
                for (WebElement error : errorMessages) {
                    if (error.isDisplayed()) {
                        String errorText = error.getText();
                        System.out.println("Writer kaydı sonrası hata mesajı: " + errorText);
                        if (errorText.contains("zaten") || errorText.contains("already") || 
                            errorText.contains("kullanılıyor") || errorText.contains("exists")) {
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                // Hata mesajı kontrolü başarısız olsa bile devam et
            }
            
            boolean success = currentUrl.contains("/dashboard") || currentUrl.contains("/yazar/dashboard") || 
                   currentUrl.equals(BASE_URL + "/") || !currentUrl.contains("/register");
            
            if (!success) {
                System.out.println("Writer kaydı başarısız görünüyor. URL: " + currentUrl);
            }
            
            return success;
        } catch (Exception e) {
            System.err.println("Writer kaydı hatası: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Logout yap (dashboard'dan çıkış)
     * Medium temasında logout ProfileDropdown içinde
     */
    protected void logout() {
        try {
            String currentUrl = driver.getCurrentUrl();
            
            // Eğer dashboard veya authenticated sayfadaysa ProfileDropdown'dan logout yap
            if (currentUrl.contains("/dashboard") || currentUrl.contains("/reader/") || 
                currentUrl.contains("/yazar/") || currentUrl.contains("/admin/")) {
                try {
                    // ProfileDropdown trigger'ı bul ve aç (profile-avatar veya profile-dropdown-trigger)
                    WebElement profileTrigger = wait.until(
                        ExpectedConditions.elementToBeClickable(
                            By.cssSelector(".profile-dropdown-trigger, .profile-avatar, button.profile-dropdown-trigger")
                        )
                    );
                    profileTrigger.click();
                    Thread.sleep(1000);
                    
                    // "Çıkış yap" butonunu bul ve tıkla (dropdown-signout class'ı)
                    WebElement logoutButton = wait.until(
                        ExpectedConditions.elementToBeClickable(
                            By.cssSelector(".dropdown-signout, button.dropdown-signout")
                        )
                    );
                    logoutButton.click();
                    Thread.sleep(2000);
                } catch (Exception e1) {
                    // ProfileDropdown bulunamadıysa veya açılamadıysa direkt logout endpoint'ine git
                    try {
                        driver.get(BASE_URL + "/logout");
                        Thread.sleep(2000);
                    } catch (Exception e2) {
                        // Logout sayfası yoksa JavaScript ile temizle
                        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
                        ((JavascriptExecutor) driver).executeScript("window.sessionStorage.clear();");
                        driver.manage().deleteAllCookies();
                    }
                }
            } else {
                // Dashboard'da değilse direkt temizle
                ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
                ((JavascriptExecutor) driver).executeScript("window.sessionStorage.clear();");
                driver.manage().deleteAllCookies();
            }
        } catch (Exception e) {
            // Hata olursa localStorage ve cookies'i temizle
            try {
                ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
                ((JavascriptExecutor) driver).executeScript("window.sessionStorage.clear();");
                driver.manage().deleteAllCookies();
            } catch (Exception e2) {
                System.out.println("Logout hatası: " + e2.getMessage());
            }
        }
    }
    
    /**
     * URL'den story slug'ını al
     */
    protected String getStorySlugFromUrl() {
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("/haberler/")) {
            String slug = currentUrl.substring(currentUrl.indexOf("/haberler/") + "/haberler/".length());
            // Query string varsa kaldır
            if (slug.contains("?")) {
                slug = slug.substring(0, slug.indexOf("?"));
            }
            return slug;
        }
        return null;
    }
    
    /**
     * Story ID'yi slug'dan al (veritabanından)
     */
    protected Long getStoryIdFromSlug(String slug) {
        try (Connection conn = getTestDatabaseConnection()) {
            String sql = "SELECT id FROM stories WHERE slug = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, slug);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("id");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Story ID alınamadı: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Email'den kullanıcı ID'sini al
     */
    protected Long getUserIdByEmail(String email) {
        try (Connection conn = getTestDatabaseConnection()) {
            String sql = "SELECT id FROM kullanicilar WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("id");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Kullanıcı ID alınamadı: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Kullanıcının en son oluşturduğu story ID'sini al
     */
    protected Long getLatestStoryIdByUserEmail(String userEmail) {
        try (Connection conn = getTestDatabaseConnection()) {
            String sql = "SELECT s.id FROM stories s " +
                         "JOIN kullanicilar k ON s.kullanici_id = k.id " +
                         "WHERE k.email = ? " +
                         "ORDER BY s.created_at DESC " +
                         "LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userEmail);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("id");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Kullanıcının en son story ID'si alınamadı: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Story'yi yayınla (publish button'a tıkla)
     * Alert'leri otomatik kabul eder
     */
    protected void publishStory() throws Exception {
        // Alert ve confirm'i override et
        ((JavascriptExecutor) driver).executeScript(
            "window.alert = function(text) { " +
            "  console.log('Alert: ' + text); " +
            "  return true; " +
            "};"
        );
        ((JavascriptExecutor) driver).executeScript(
            "window.confirm = function(text) { " +
            "  console.log('Confirm: ' + text); " +
            "  return true; " +
            "};"
        );
        
        Thread.sleep(1000);
        WebElement publishButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".publish-button, button.publish-button")
            )
        );
        publishButton.click();
        
        Thread.sleep(3000); // Yayınlama işlemi için bekle
        
        // Alert'leri kontrol et ve kabul et
        try {
            org.openqa.selenium.Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            System.out.println("Publish sonrası alert: " + alertText);
            alert.accept();
            Thread.sleep(2000);
        } catch (Exception alertEx) {
            // Alert yoksa devam et
        }
        
        waitForPageLoad();
        Thread.sleep(2000);
    }
    
    /**
     * Story başlığından story ID'sini al
     */
    protected Long getStoryIdByTitle(String title) {
        // Önce URL'den ID'yi almaya çalış (eğer story oluşturulduktan sonra URL'de ID varsa)
        try {
            String currentUrl = driver.getCurrentUrl();
            // URL formatı: /haberler/{slug} veya /yazar/haber-duzenle/{id} veya /haberler/{id}
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("/(?:haberler|yazar/haber-duzenle)/(\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(currentUrl);
            if (matcher.find()) {
                Long idFromUrl = Long.parseLong(matcher.group(1));
                System.out.println("Story ID URL'den alındı: " + idFromUrl);
                return idFromUrl;
            }
        } catch (Exception e) {
            // URL'den alınamazsa devam et
        }
        
        // Veritabanından almayı dene
        try (Connection conn = getTestDatabaseConnection()) {
            String sql = "SELECT id FROM stories WHERE baslik = ? ORDER BY created_at DESC LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, title);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Long id = rs.getLong("id");
                        System.out.println("Story ID veritabanından alındı: " + id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Story ID başlıktan alınamadı: " + e.getMessage());
            // Eğer tablo yoksa, kullanıcının en son story'sini almayı dene
            if (e.getMessage().contains("does not exist") || e.getMessage().contains("relation")) {
                System.out.println("Stories tablosu bulunamadı, alternatif yöntem deneniyor...");
            }
        }
        
        // Son çare: Kullanıcının en son story'sini al (eğer email biliniyorsa)
        return null;
    }
    
    /**
     * Veritabanı üzerinden story onayla
     */
    protected void approveStoryViaBackend(Long storyId, Long adminId) {
        try (Connection conn = getTestDatabaseConnection()) {
            String sql = "UPDATE stories SET durum = 'YAYINLANDI', yayinlanma_tarihi = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, LocalDateTime.now());
                stmt.setLong(2, storyId);
                int updated = stmt.executeUpdate();
                if (updated > 0) {
                    System.out.println("Story veritabanından onaylandı: " + storyId);
                } else {
                    System.out.println("Story bulunamadı veya zaten onaylı: " + storyId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Veritabanından story onaylanamadı: " + e.getMessage());
        }
    }
    
    /**
     * Story oluştur ve yayınla (admin onayı yapılmaz)
     * @return Story slug'ı
     */
    protected String createStory(String writerEmail, String writerPassword, String storyTitle, String storyContent) {
        try {
            // Writer zaten giriş yapmış durumda olmalı (kayıt sonrası dashboard'a yönlendirildi)
            // Eğer giriş yapılmamışsa giriş yap
            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains("/dashboard") && !currentUrl.contains("/yazar/") && !currentUrl.contains("/reader/")) {
                loginUser(writerEmail, writerPassword);
            }
            
            // Story oluştur
            driver.get(BASE_URL + "/reader/new-story");
            waitForPageLoad();
            Thread.sleep(2000);
            
            // Başlık gir
            WebElement titleInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input.story-title-input, input[placeholder*='Başlık']")
                )
            );
            titleInput.sendKeys(storyTitle);
            Thread.sleep(1000);
            
            // İçerik gir
            WebElement contentTextarea = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.block-textarea")
                )
            );
            contentTextarea.sendKeys(storyContent);
            Thread.sleep(1000);
            
            // Yayınla
            WebElement publishButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".publish-button, button.publish-button")
                )
            );
            publishButton.click();
            Thread.sleep(5000);
            
            // Story slug'ını al (yayınlandıktan sonra URL'den veya veritabanından)
            String storySlug = getStorySlugFromUrl();
            if (storySlug == null) {
                // URL'den alınamazsa veritabanından al
                Long storyId = getStoryIdByTitle(storyTitle);
                if (storyId != null) {
                    try (Connection conn = getTestDatabaseConnection()) {
                        String sql = "SELECT slug FROM stories WHERE id = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setLong(1, storyId);
                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                    storySlug = rs.getString("slug");
                                }
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Story slug veritabanından alınamadı: " + e.getMessage());
                    }
                }
                
                // Hala bulunamazsa title'dan oluştur
                if (storySlug == null) {
                    storySlug = storyTitle.toLowerCase()
                        .replaceAll("[^a-z0-9\\s-]", "")
                        .replaceAll("\\s+", "-")
                        .replaceAll("-+", "-");
                }
            }
            
            // Writer'dan logout yap (admin onayı için hazırlık)
            try {
                driver.get(BASE_URL + "/logout");
                Thread.sleep(2000);
            } catch (Exception e) {
                // Logout sayfası yoksa veya hata varsa devam et
                System.out.println("Logout yapılamadı, devam ediliyor: " + e.getMessage());
            }
            
            return storySlug;
            
        } catch (Exception e) {
            System.err.println("Story oluşturma hatası: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Admin olarak giriş yap ve story'yi onayla
     * @param storyTitle Story başlığı (onay bekleyen story'yi bulmak için)
     * @return Story slug'ı (onaylandıktan sonra)
     */
    protected String approveStoryAsAdmin(String storyTitle) {
        try {
            // Logout
            try {
                driver.get(BASE_URL + "/logout");
                Thread.sleep(2000);
            } catch (Exception e) {
                // Logout sayfası yoksa devam et
            }
            
            // Admin credentials al
            AdminCredentials adminCreds = ensureAdminUserExists();
            
            // Admin olarak giriş yap
            loginUser(adminCreds.getEmail(), adminCreds.getPassword());
            
            // Admin dashboard'a git
            driver.get(BASE_URL + "/admin/dashboard");
            waitForPageLoad();
            Thread.sleep(3000);
            
            // Story'yi bul ve onayla
            try {
                WebElement storyRow = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[contains(text(), '" + storyTitle + "')]")
                    )
                );
                
                // Onayla butonunu bul ve tıkla
                WebElement approveButton = storyRow.findElement(
                    By.xpath(".//button[contains(text(), 'Onayla') or contains(text(), 'onayla')]")
                );
                approveButton.click();
                
                Thread.sleep(1000);
                try {
                    driver.switchTo().alert().accept();
                } catch (Exception e) {
                    // Alert yoksa devam et
                }
                
                Thread.sleep(3000);
                
                // Story slug'ını al (onaylandıktan sonra)
                // Story ID'yi bul ve slug'ı al
                Long storyId = getStoryIdByTitle(storyTitle);
                if (storyId != null) {
                    try (Connection conn = getTestDatabaseConnection()) {
                        String sql = "SELECT slug FROM stories WHERE id = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setLong(1, storyId);
                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                    return rs.getString("slug");
                                }
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Story slug veritabanından alınamadı: " + e.getMessage());
                    }
                }
                
                // Slug bulunamazsa title'dan oluştur
                String storySlug = storyTitle.toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")
                    .replaceAll("\\s+", "-")
                    .replaceAll("-+", "-");
                return storySlug;
                
            } catch (Exception e) {
                System.err.println("Story admin dashboard'da bulunamadı: " + e.getMessage());
                // Backend API ile onaylamayı dene
                Long storyId = getStoryIdByTitle(storyTitle);
                if (storyId != null) {
                    approveStoryViaBackend(storyId, null);
                    // Slug'ı veritabanından al
                    try (Connection conn = getTestDatabaseConnection()) {
                        String sql = "SELECT slug FROM stories WHERE id = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setLong(1, storyId);
                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                    return rs.getString("slug");
                                }
                            }
                        }
                    } catch (SQLException ex) {
                        System.err.println("Story slug veritabanından alınamadı: " + ex.getMessage());
                    }
                }
                
                // Slug bulunamazsa title'dan oluştur
                return storyTitle.toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")
                    .replaceAll("\\s+", "-")
                    .replaceAll("-+", "-");
            }
            
        } catch (Exception e) {
            System.err.println("Admin onaylama hatası: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Story oluştur, yayınla ve admin onayı yap (eski metod - geriye dönük uyumluluk için)
     * @return Story slug'ı
     */
    protected String createStoryAndApprove(String writerEmail, String writerPassword, String storyTitle, String storyContent) {
        try {
            // Writer olarak giriş yap
            loginUser(writerEmail, writerPassword);
            
            // Story oluştur
            driver.get(BASE_URL + "/reader/new-story");
            waitForPageLoad();
            Thread.sleep(2000);
            
            // Başlık gir
            WebElement titleInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input.story-title-input, input[placeholder*='Başlık']")
                )
            );
            titleInput.sendKeys(storyTitle);
            Thread.sleep(1000);
            
            // İçerik gir
            WebElement contentTextarea = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("textarea.block-textarea")
                )
            );
            contentTextarea.sendKeys(storyContent);
            Thread.sleep(1000);
            
            // Yayınla
            WebElement publishButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".publish-button, button.publish-button")
                )
            );
            publishButton.click();
            Thread.sleep(5000);
            
            // Story'nin oluşturulmasını bekle ve ID'yi al
            Long storyId = null;
            int retryCount = 0;
            while (storyId == null && retryCount < 10) {
                try {
                    Thread.sleep(1000);
                    // Önce başlıktan dene
                    storyId = getStoryIdByTitle(storyTitle);
                    if (storyId == null) {
                        // Başlıktan bulunamazsa kullanıcının en son story'sini al
                        storyId = getLatestStoryIdByUserEmail(writerEmail);
                    }
                    retryCount++;
                } catch (Exception e) {
                    retryCount++;
                }
            }
            
            if (storyId == null) {
                System.err.println("Story ID alınamadı, admin onayı yapılamayacak");
                // Slug'ı title'dan oluştur ve döndür
                String storySlug = storyTitle.toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")
                    .replaceAll("\\s+", "-")
                    .replaceAll("-+", "-");
                return storySlug;
            }
            
            // Story ID bulundu, admin onayı yap
            System.out.println("Story ID bulundu: " + storyId + ", admin onayı yapılıyor...");
            
            // Admin credentials al
            AdminCredentials adminCreds = ensureAdminUserExists();
            
            // Veritabanı üzerinden onayla
            try {
                Long adminId = getUserIdByEmail(adminCreds.getEmail());
                if (adminId != null) {
                    approveStoryViaBackend(storyId, adminId);
                    System.out.println("Story veritabanından onaylandı: " + storyId);
                } else {
                    System.err.println("Admin kullanıcı ID bulunamadı");
                }
            } catch (Exception e) {
                System.err.println("Veritabanı ile story onaylanamadı, UI üzerinden denenecek: " + e.getMessage());
                
                // Veritabanı yöntemi başarısız olursa UI üzerinden dene
                try {
                    // Logout
                    try {
                        driver.get(BASE_URL + "/logout");
                        Thread.sleep(2000);
                    } catch (Exception ex) {
                        // Logout sayfası yoksa devam et
                    }
                    
                    // Admin olarak giriş yap
                    loginUser(adminCreds.getEmail(), adminCreds.getPassword());
                    
                    // Admin dashboard'a git
                    driver.get(BASE_URL + "/admin/dashboard");
                    waitForPageLoad();
                    Thread.sleep(3000);
                    
                    // Story'yi bul ve onayla
                    WebElement storyRow = wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//*[contains(text(), '" + storyTitle + "')]")
                        )
                    );
                    
                    // Onayla butonunu bul ve tıkla
                    WebElement approveButton = storyRow.findElement(
                        By.xpath(".//button[contains(text(), 'Onayla') or contains(text(), 'onayla')]")
                    );
                    approveButton.click();
                    
                    Thread.sleep(1000);
                    try {
                        driver.switchTo().alert().accept();
                    } catch (Exception ex) {
                        // Alert yoksa devam et
                    }
                    
                    Thread.sleep(3000);
                    System.out.println("Story UI üzerinden onaylandı: " + storyId);
                } catch (Exception ex) {
                    System.err.println("Story UI üzerinden de onaylanamadı: " + ex.getMessage());
                }
            }
            
            // Story slug'ını al (onaylandıktan sonra)
            String storySlug = getStorySlugFromUrl();
            if (storySlug == null) {
                // URL'den alınamazsa veritabanından al
                try (Connection conn = getTestDatabaseConnection()) {
                    String sql = "SELECT slug FROM stories WHERE id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setLong(1, storyId);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                storySlug = rs.getString("slug");
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Story slug veritabanından alınamadı: " + e.getMessage());
                }
                
                // Hala bulunamazsa title'dan oluştur
                if (storySlug == null) {
                    storySlug = storyTitle.toLowerCase()
                        .replaceAll("[^a-z0-9\\s-]", "")
                        .replaceAll("\\s+", "-")
                        .replaceAll("-+", "-");
                }
            }
            
            return storySlug;
            
        } catch (Exception e) {
            System.err.println("Story oluşturma ve onaylama hatası: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Admin credentials için inner class
     */
    protected static class AdminCredentials {
        private final String email;
        private final String password;
        
        public AdminCredentials(String email, String password) {
            this.email = email;
            this.password = password;
        }
        
        public String getEmail() {
            return email;
        }
        
        public String getPassword() {
            return password;
        }
    }
}

