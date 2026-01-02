package com.example.backend.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class for Selenium tests
 * Provides common setup and teardown methods
 */
public abstract class BaseSeleniumTest {
    
    protected static WebDriver driver;
    protected static WebDriverWait wait;
    // URL'leri environment variable veya system property'den al, yoksa localhost kullan
    protected static final String BASE_URL = System.getProperty("frontend.url", 
        System.getenv("FRONTEND_URL") != null ? System.getenv("FRONTEND_URL") : "http://localhost:5173");
    protected static final String BACKEND_URL = System.getProperty("backend.url",
        System.getenv("BACKEND_URL") != null ? System.getenv("BACKEND_URL") : "http://localhost:8080");
    protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15); // Optimize edilmiş timeout
    
    // Jenkins ortamında DB'ye erişim yok; DB erişimini kapatmak için USE_DB=false
    private static final boolean USE_DB = Boolean.parseBoolean(System.getProperty("test.use.db", "false"));
    // Veritabanı bağlantı bilgileri (local geliştirme için, USE_DB=true ise kullanılır)
    private static final String TEST_DB_URL = System.getProperty("test.db.url", 
        System.getenv("TEST_DB_URL") != null ? System.getenv("TEST_DB_URL") : "jdbc:postgresql://localhost:5433/yazilimdogrulama");
    private static final String TEST_DB_USER = System.getProperty("test.db.user",
        System.getenv("TEST_DB_USER") != null ? System.getenv("TEST_DB_USER") : "postgres");
    private static final String TEST_DB_PASSWORD = System.getProperty("test.db.password",
        System.getenv("TEST_DB_PASSWORD") != null ? System.getenv("TEST_DB_PASSWORD") : "postgres");
    
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // Spring Boot context'i bir kez başlat (tabloları oluşturmak için)
    private static volatile boolean databaseInitialized = false;
    private static volatile boolean frontendChecked = false;
    
    @BeforeAll
    static void initializeDatabase() {
        if (!databaseInitialized) {
            if (!USE_DB) {
                databaseInitialized = true;
                return;
            }
            synchronized (BaseSeleniumTest.class) {
                if (!databaseInitialized) {
                    ConfigurableApplicationContext springContext = null;
                    try {
                        System.out.println("🔧 Veritabanı kontrol ediliyor: " + TEST_DB_URL);
                        
                        // Önce tabloların var olup olmadığını kontrol et
                        boolean tablesExist = false;
                        try (Connection conn = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD)) {
                            try (PreparedStatement stmt = conn.prepareStatement(
                                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'kullanicilar')"
                            )) {
                                try (ResultSet rs = stmt.executeQuery()) {
                                    if (rs.next() && rs.getBoolean(1)) {
                                        tablesExist = true;
                                        System.out.println("✅ Veritabanında tablolar zaten mevcut");
                                    }
                                }
                            }
                        } catch (SQLException e) {
                            System.out.println("⚠️ Tablo kontrolü hatası: " + e.getMessage());
                        }
                        
                        // Tablolar varsa Spring context başlatma - gereksiz
                        if (tablesExist) {
                            System.out.println("✅ Veritabanı tabloları mevcut, Spring context başlatılmıyor");
                            databaseInitialized = true;
                        } else {
                            // Spring Boot'u başlat (tabloları oluşturmak için)
                            // Backend'in kullandığı veritabanına tabloları oluşturmak için
                            // application.properties'teki ayarları override et
                            System.setProperty("spring.datasource.url", TEST_DB_URL);
                            System.setProperty("spring.datasource.username", TEST_DB_USER);
                            System.setProperty("spring.datasource.password", TEST_DB_PASSWORD);
                            // Tablolar yoksa create kullan (backend'in kullandığı veritabanına tabloları oluştur)
                            System.setProperty("spring.jpa.hibernate.ddl-auto", "create");
                            System.setProperty("spring.jpa.show-sql", "false");
                            System.setProperty("server.port", "0"); // Random port
                            System.setProperty("spring.main.web-application-type", "none"); // Web server başlatma
                            
                            System.out.println("📥 Veritabanı tabloları oluşturuluyor...");
                            System.out.println("⚠️ NOT: Backend local'de çalışıyorsa, backend'i yeniden başlatmanız gerekebilir");
                            
                            // Spring Boot'u başlat
                            springContext = SpringApplication.run(
                                com.example.backend.BackendApplication.class,
                                new String[]{}
                            );
                            
                            // Context başlatıldıktan sonra tablolar oluşturulmuş olacak
                            System.out.println("✅ Veritabanı tabloları oluşturuldu");
                            System.out.println("⚠️ Backend local'de çalışıyorsa, backend'i yeniden başlatın");
                            databaseInitialized = true;
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Database initialization hatası: " + e.getMessage());
                        e.printStackTrace();
                        // Hata olsa bile devam et, belki tablolar zaten var
                    } finally {
                        // Context'i kapat (sadece tabloları oluşturmak için başlattık)
                        if (springContext != null) {
                            try {
                                springContext.close();
                            } catch (Exception e) {
                                // Ignore
                            }
                        }
                    }
                }
            }
        }
    }
    
    @BeforeAll
    public static void setUpOnce() {
        if (driver != null) {
            return;
        }
        // Setup ChromeDriver using WebDriverManager
        // ARM64 için doğru driver'ı indirmesini sağla
        String osArch = System.getProperty("os.arch", "");
        if (osArch.contains("aarch64") || osArch.contains("arm64")) {
            // ARM64 için container'da kurulu olan chromedriver'ı kullan
            // WebDriverManager yanlış mimari için driver indiriyor (linux64 yerine linux-arm64)
            String[] systemDriverPaths = {
                "/usr/bin/chromedriver",  // Container'da kurulu ARM64 driver
                "/usr/bin/chromium-driver",
                "/usr/local/bin/chromedriver"
            };
            
            String driverPath = null;
            for (String path : systemDriverPaths) {
                java.io.File driverFile = new java.io.File(path);
                if (driverFile.exists() && driverFile.canExecute()) {
                    driverPath = path;
                    System.out.println("✅ Container'da kurulu ARM64 ChromeDriver bulundu: " + path);
                    break;
                }
            }
            
            // System driver bulunamazsa WebDriverManager'ı dene (fallback)
            if (driverPath == null) {
                System.out.println("⚠️ System driver bulunamadı, WebDriverManager deneniyor...");
                try {
                    WebDriverManager.chromedriver()
                        .driverVersion("143.0.7499.169")
                        .setup();
                    
                    String wdmPath = System.getProperty("webdriver.chrome.driver");
                    if (wdmPath != null && new java.io.File(wdmPath).exists()) {
                        driverPath = wdmPath;
                        System.out.println("✅ WebDriverManager ile driver bulundu: " + wdmPath);
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ WebDriverManager hatası: " + e.getMessage());
                }
            }
            
            if (driverPath == null) {
                throw new RuntimeException("ChromeDriver bulunamadı. Container'da /usr/bin/chromedriver kurulu olmalı.");
            }
            
            System.setProperty("webdriver.chrome.driver", driverPath);
            System.out.println("📥 ARM64 ChromeDriver yapılandırması tamamlandı: " + driverPath);
        } else {
            try {
                WebDriverManager.chromedriver().setup();
            } catch (Exception e) {
                // Internet erişimi yoksa cache'den kullan
                System.out.println("⚠️ ChromeDriver indirilemedi, cache'den kullanılıyor: " + e.getMessage());
                // Cache path'lerini dene
                String[] possibleCachePaths = {
                    "/root/.cache/selenium/chromedriver/linux64/chromedriver",
                    System.getProperty("user.home") + "/.cache/selenium/chromedriver/linux64/chromedriver"
                };
                
                boolean driverFound = false;
                for (String cachedDriverPath : possibleCachePaths) {
                    java.io.File driverFile = new java.io.File(cachedDriverPath);
                    if (driverFile.exists() && driverFile.canExecute()) {
                        System.setProperty("webdriver.chrome.driver", cachedDriverPath);
                        System.out.println("✅ Cache'den ChromeDriver path'i ayarlandı: " + cachedDriverPath);
                        driverFound = true;
                        break;
                    }
                }
                
                if (!driverFound) {
                    throw new RuntimeException("ChromeDriver bulunamadı ve indirilemedi.", e);
                }
            }
        }
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--disable-dev-shm-usage", "--no-sandbox", "--window-size=1280,720");
        
        // Şifre yöneticisini tamamen devre dışı bırak (test sırasında pop-up'ları önlemek için)
        options.addArguments("--disable-password-manager");
        options.addArguments("--disable-password-manager-reauthentication");
        options.addArguments("--disable-features=PasswordManager,PasswordCheck");
        options.addArguments("--disable-save-password-bubble");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        
        // Chrome preferences ile şifre yöneticisini tamamen devre dışı bırak
        java.util.Map<String, Object> prefs = new java.util.HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.default_content_setting_values.notifications", 2);
        prefs.put("profile.password_manager_leak_detection", false);
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("profile.content_settings.exceptions.automatic_downloads", new java.util.HashMap<>());
        options.setExperimentalOption("prefs", prefs);
        
        // Chrome'un otomatik şifre önerilerini devre dışı bırak
        options.setExperimentalOption("excludeSwitches", java.util.Arrays.asList("enable-automation", "enable-logging"));
        options.setExperimentalOption("useAutomationExtension", false);
        
        // PERFORMANS: Resimleri devre dışı bırak (test süresini %30-50 azaltır)
        options.addArguments("--blink-settings=imagesEnabled=false");
        options.addArguments("--disable-images");
        
        // CI/CD ortamı için headless mod kontrolü
        String headless = System.getProperty("selenium.headless", "false");
        if ("true".equalsIgnoreCase(headless) || System.getenv("CI") != null) {
            // Headless mod için gerekli tüm argümanlar (ARM64 uyumluluğu dahil)
            options.addArguments("--headless=new"); // Yeni headless mod (daha stabil)
            options.addArguments("--no-sandbox"); // Container'da gerekli
            options.addArguments("--disable-dev-shm-usage"); // /dev/shm sorunlarını önler
            options.addArguments("--disable-gpu"); // GPU gereksiz
            options.addArguments("--disable-software-rasterizer"); // ARM64 için
            options.addArguments("--disable-extensions"); // Extension'lar gereksiz
            options.addArguments("--disable-background-networking"); // Arka plan ağ trafiğini azalt
            options.addArguments("--disable-background-timer-throttling");
            options.addArguments("--disable-renderer-backgrounding");
            options.addArguments("--disable-backgrounding-occluded-windows");
            options.addArguments("--disable-breakpad"); // Crash reporting
            options.addArguments("--disable-client-side-phishing-detection");
            options.addArguments("--disable-crash-reporter");
            options.addArguments("--disable-default-apps");
            options.addArguments("--disable-hang-monitor");
            options.addArguments("--disable-popup-blocking");
            options.addArguments("--disable-prompt-on-repost");
            options.addArguments("--disable-sync");
            options.addArguments("--disable-translate");
            options.addArguments("--metrics-recording-only");
            options.addArguments("--no-first-run");
            options.addArguments("--safebrowsing-disable-auto-update");
            options.addArguments("--enable-automation");
            options.addArguments("--password-store=basic");
            options.addArguments("--use-mock-keychain"); // macOS için (ARM64'te de gerekli olabilir)
            options.addArguments("--single-process"); // ARM64 için daha stabil
            options.addArguments("--disable-features=TranslateUI");
            options.addArguments("--disable-ipc-flooding-protection");
            options.addArguments("--disable-setuid-sandbox"); // Container'da gerekli
            options.addArguments("--disable-seccomp-filter-sandbox"); // Container'da gerekli
            options.addArguments("--disable-background-timer-throttling");
            options.addArguments("--disable-backgrounding-occluded-windows");
            options.addArguments("--disable-renderer-backgrounding");
            options.addArguments("--disable-features=BlinkGenPropertyTrees");
            options.addArguments("--disable-features=IsolateOrigins,site-per-process");
            options.addArguments("--run-all-compositor-stages-before-draw");
            options.addArguments("--disable-threaded-animation");
            options.addArguments("--disable-threaded-scrolling");
            options.addArguments("--disable-in-process-stack-traces");
            options.addArguments("--disable-histogram-customizer");
            options.addArguments("--disable-gl-extensions");
            options.addArguments("--disable-composited-antialiasing");
            options.addArguments("--disable-canvas-aa");
            options.addArguments("--disable-2d-canvas-clip-aa");
            options.addArguments("--disable-gl-drawing-for-tests");
            // D-Bus hatalarını önle (container'da D-Bus yok)
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-software-rasterizer");
            options.addArguments("--disable-gpu-compositing");
            options.addArguments("--disable-background-networking");
            options.addArguments("--disable-default-apps");
            options.addArguments("--disable-sync");
            options.addArguments("--metrics-recording-only");
            options.addArguments("--no-first-run");
            options.addArguments("--safebrowsing-disable-auto-update");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-plugins");
            options.addArguments("--disable-plugins-discovery");
            options.addArguments("--disable-preconnect");
            options.addArguments("--disable-translate");
            options.addArguments("--disable-background-timer-throttling");
            options.addArguments("--disable-renderer-backgrounding");
            options.addArguments("--disable-backgrounding-occluded-windows");
            options.addArguments("--disable-breakpad");
            options.addArguments("--disable-client-side-phishing-detection");
            options.addArguments("--disable-crash-reporter");
            options.addArguments("--disable-hang-monitor");
            options.addArguments("--disable-popup-blocking");
            options.addArguments("--disable-prompt-on-repost");
            options.addArguments("--disable-domain-reliability");
            options.addArguments("--disable-component-update");
            options.addArguments("--disable-background-downloads");
            options.addArguments("--disable-add-to-shelf");
            options.addArguments("--disable-breakpad");
            options.addArguments("--disable-features=TranslateUI,BlinkGenPropertyTrees");
            // D-Bus hatalarını tamamen devre dışı bırak
            System.setProperty("DBUS_SESSION_BUS_ADDRESS", "");
            System.setProperty("CHROME_DEVEL_SANDBOX", "");
        } else {
            options.addArguments("--start-maximized");
        }
        
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--window-size=1920,1080");
        
        // CDP uyarılarını azaltmak için
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-logging");
        options.addArguments("--log-level=3"); // Sadece fatal hataları göster
        
        // ARM64 için özel ayarlar ve Chrome binary path'i
        // osArch zaten yukarıda tanımlı
        if (osArch.contains("aarch64") || osArch.contains("arm64")) {
            options.addArguments("--disable-software-rasterizer");
            options.addArguments("--disable-gpu-sandbox");
            options.addArguments("--disable-accelerated-2d-canvas");
        }
        
        // CI/CD ortamında Chrome binary path'ini belirle
        if ("true".equalsIgnoreCase(headless) || System.getenv("CI") != null) {
            // Container'da chromium genellikle bu path'lerden birinde olur
            String[] possiblePaths = {
                "/usr/bin/chromium",
                "/usr/bin/chromium-browser",
                "/usr/bin/google-chrome",
                "/usr/bin/google-chrome-stable"
            };
            
            // İlk bulunan path'i kullan
            for (String path : possiblePaths) {
                try {
                    java.io.File chromeFile = new java.io.File(path);
                    if (chromeFile.exists() && chromeFile.canExecute()) {
                        options.setBinary(path);
                        System.out.println("✅ Chrome binary bulundu: " + path);
                        break;
                    }
                } catch (Exception e) {
                    // Path kontrolü başarısız, devam et
                }
            }
        }
        
        // System property ile Selenium log seviyesini ayarla
        // Console logging'i etkinleştir (browser console loglarını yakalamak için)
        System.setProperty("webdriver.chrome.silentOutput", "false");
        System.setProperty("org.openqa.selenium.chrome.driver.silent", "false");
        
        // Browser console loglarını yakalamak için LoggingPreferences ekle
        org.openqa.selenium.logging.LoggingPreferences loggingPreferences = new org.openqa.selenium.logging.LoggingPreferences();
        loggingPreferences.enable(org.openqa.selenium.logging.LogType.BROWSER, java.util.logging.Level.ALL);
        loggingPreferences.enable(org.openqa.selenium.logging.LogType.PERFORMANCE, java.util.logging.Level.ALL);
        options.setCapability(org.openqa.selenium.chrome.ChromeOptions.LOGGING_PREFS, loggingPreferences);
        
        // Environment variable'ları ayarla (ChromeDriver başlatılmadan önce)
        if ("true".equalsIgnoreCase(headless) || System.getenv("CI") != null) {
            // System property'ler (bazı ChromeDriver versiyonları bunları okur)
            System.setProperty("DBUS_SESSION_BUS_ADDRESS", "");
            System.setProperty("CHROME_DEVEL_SANDBOX", "");
            System.setProperty("DISPLAY", "");
            System.setProperty("QT_QPA_PLATFORM", "offscreen");
        }
        
        // ChromeDriver'ı başlat (retry mekanizması ile)
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                driver = new ChromeDriver(options);
                break; // Başarılı, döngüden çık
            } catch (org.openqa.selenium.WebDriverException e) {
                retryCount++;
                if (retryCount < maxRetries) {
                    System.out.println("⚠️ ChromeDriver başlatılamadı, tekrar deneniyor (" + retryCount + "/" + maxRetries + "): " + e.getMessage());
                    try {
                        Thread.sleep(1000); // 1 saniye bekle
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    // Her denemede ek argümanlar ekle
                    if (retryCount == 2) {
                        options.addArguments("--disable-features=VizDisplayCompositor");
                        options.addArguments("--disable-software-rasterizer");
                    }
                } else {
                    throw new RuntimeException("ChromeDriver " + maxRetries + " denemede başlatılamadı. Son hata: " + e.getMessage(), e);
                }
            }
        }
        wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        
        // Chrome şifre yöneticisi uyarılarını otomatik kapat
        dismissPasswordManagerAlerts();
        
        // Frontend erişilebilirlik kontrolü (sadece bir kez)
        if (!frontendChecked) {
            synchronized (BaseSeleniumTest.class) {
                if (!frontendChecked) {
                    checkFrontendAccess();
                    frontendChecked = true;
                }
            }
        }
        
        // Önce localStorage ve cookies'i temizle (önceki oturumları temizlemek için)
        try {
            Thread.sleep(500); // Sayfanın yüklenmesini bekle
            String currentUrl = driver.getCurrentUrl();
            // data: URL'lerinde localStorage kullanılamaz, sadece normal URL'lerde temizle
            if (currentUrl != null && !currentUrl.startsWith("data:")) {
                try {
                    ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
                } catch (Exception e) {
                    // localStorage temizleme hatası (data: URL'lerinde olabilir) - sessizce devam et
                }
                try {
                    ((JavascriptExecutor) driver).executeScript("window.sessionStorage.clear();");
                } catch (Exception e) {
                    // sessionStorage temizleme hatası - sessizce devam et
                }
            }
            driver.manage().deleteAllCookies();
            // Sayfayı yeniden yükle
            if (currentUrl != null && !currentUrl.startsWith("data:")) {
                driver.navigate().refresh();
                Thread.sleep(500);
            }
        } catch (Exception e) {
            // Temizleme başarısız olursa devam et (hata mesajı gösterme)
        }
        
        // Ana sayfaya git ve oturum kontrolü yap
        driver.get(BASE_URL + "/");
        waitForPageLoad();
        
        // Eğer dashboard'a yönlendirildiyse, logout yap
        try {
            Thread.sleep(1000); // 2000 -> 1000
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
    
    @AfterAll
    public static void tearDownOnce() {
        if (driver != null) {
            driver.quit();
            driver = null;
            wait = null;
        }
    }
    
    /**
     * Frontend erişilebilirliğini kontrol et (sadece bir kez çalışır)
     */
    private static void checkFrontendAccess() {
        try {
            System.out.println("🔍 Frontend erişilebilirlik kontrolü: " + BASE_URL);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
            driver.get(BASE_URL);
            System.out.println("✅ Frontend erişilebilir: " + BASE_URL);
        } catch (org.openqa.selenium.TimeoutException e) {
            String errorMsg = "❌ Frontend'e erişilemiyor: " + BASE_URL + 
                "\nFrontend'in çalıştığından ve erişilebilir olduğundan emin olun." +
                "\nHata: " + e.getMessage();
            System.err.println(errorMsg);
            driver.quit();
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "❌ Frontend bağlantı hatası: " + BASE_URL + 
                "\nHata: " + e.getMessage();
            System.err.println(errorMsg);
            driver.quit();
            throw new RuntimeException(errorMsg, e);
        } finally {
            // Timeout'u normale döndür
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        }
    }
    
    /**
     * Helper method to wait for page to load
     */
    /**
     * Chrome şifre yöneticisi uyarılarını otomatik kapat
     */
    protected static void dismissPasswordManagerAlerts() {
        try {
            // Önce alert'leri kontrol et
            for (int i = 0; i < 5; i++) {
                try {
                    org.openqa.selenium.Alert alert = driver.switchTo().alert();
                    String alertText = alert.getText();
                    if (alertText != null && (alertText.contains("şifre") || alertText.contains("password") || 
                        alertText.contains("Şifre") || alertText.contains("Password"))) {
                        alert.accept();
                        System.out.println("🔒 Şifre yöneticisi uyarısı kapatıldı");
                        Thread.sleep(500);
                    } else {
                        alert.accept();
                        Thread.sleep(500);
                    }
                } catch (org.openqa.selenium.NoAlertPresentException e) {
                    break;
                }
            }
            
            // JavaScript ile Chrome'un şifre yöneticisi pop-up'ını kapat
            try {
                ((JavascriptExecutor) driver).executeScript(
                    "if (window.chrome && window.chrome.runtime) {" +
                    "  try { window.chrome.runtime.onConnect.removeListener(); } catch(e) {}" +
                    "}" +
                    "var alerts = document.querySelectorAll('[role=\"alert\"], [role=\"dialog\"], .password-manager-alert, [class*=\"password\"], [class*=\"Password\"]');" +
                    "alerts.forEach(function(alert) { " +
                    "  var text = alert.textContent || alert.innerText || ''; " +
                    "  if (text.includes('şifre') || text.includes('password') || text.includes('Şifre') || text.includes('Password')) { " +
                    "    var button = alert.querySelector('button, [role=\"button\"]'); " +
                    "    if (button) button.click(); " +
                    "    else alert.remove(); " +
                    "  } " +
                    "});"
                );
            } catch (Exception e) {
                // JavaScript hatası - devam et
            }
            
            // XPath ile şifre uyarısı butonlarını bul ve tıkla
            try {
                java.util.List<WebElement> passwordButtons = driver.findElements(
                    By.xpath("//button[contains(text(), 'Tamam') or contains(text(), 'OK') or contains(text(), 'Kapat') or contains(text(), 'Close')]")
                );
                for (WebElement button : passwordButtons) {
                    try {
                        String buttonText = button.getText().toLowerCase();
                        if (buttonText.contains("tamam") || buttonText.contains("ok") || 
                            buttonText.contains("kapat") || buttonText.contains("close")) {
                            safeClick(button);
                            Thread.sleep(500);
                        }
                    } catch (Exception e) {
                        // Buton tıklanamıyor, devam et
                    }
                }
            } catch (Exception e) {
                // XPath hatası - devam et
            }
        } catch (Exception e) {
            // Genel hata - sessizce devam et
        }
    }
    
    protected static void waitForPageLoad() {
        try {
            Thread.sleep(500); // 1000 -> 500 (Wait for React to render)
            // Sayfa yüklendikten sonra şifre yöneticisi uyarılarını kapat
            dismissPasswordManagerAlerts();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // Hata olsa bile devam et
        }
    }
    
    /**
     * Güvenilir buton tıklama metodu
     * Önce normal click dener, başarısız olursa JavaScript executor kullanır
     */
    protected static void safeClick(WebElement element) {
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
     * Backend'in oluşturduğu admin kullanıcısını kullan
     * Backend başlatıldığında DataInitializer otomatik olarak omer@gmail.com / 123456 oluşturuyor
     * Bu method bir kez kontrol eder, varsa güncelleme yapmaz
     */
    private static volatile boolean adminUserChecked = false;
    
    protected AdminCredentials ensureAdminUserExists() {
        String adminEmail = System.getProperty("test.admin.email", "omer@gmail.com");
        String adminPassword = System.getProperty("test.admin.password", "123456");
        
        // İlk çalışmada bir kez kontrol et, sonraki çalışmalarda kontrol etme
        if (adminUserChecked) {
            return new AdminCredentials(adminEmail, adminPassword);
        }
        
        synchronized (BaseSeleniumTest.class) {
            if (adminUserChecked) {
                return new AdminCredentials(adminEmail, adminPassword);
            }
            
            try (Connection conn = getTestDatabaseConnection()) {
                // Admin kullanıcısının var olup olmadığını kontrol et
                String checkUserSql = "SELECT id, sifre, is_active FROM kullanicilar WHERE email = ?";
                try (PreparedStatement stmt = conn.prepareStatement(checkUserSql)) {
                    stmt.setString(1, adminEmail);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            // Kullanıcı var
                            Long userId = rs.getLong("id");
                            String hashedPassword = rs.getString("sifre");
                            Boolean isActive = rs.getBoolean("is_active");
                            
                            // Şifre kontrolü - sadece bilgi amaçlı (güncelleme yapılmaz)
                            boolean passwordMatches = passwordEncoder.matches(adminPassword, hashedPassword);
                            System.out.println("🔍 Admin kullanıcısı bulundu:");
                            System.out.println("  - ID: " + userId);
                            System.out.println("  - Email: " + adminEmail);
                            System.out.println("  - is_active: " + isActive);
                            System.out.println("  - Şifre eşleşiyor: " + passwordMatches);
                            
                            if (!passwordMatches) {
                                System.out.println("⚠️ UYARI: Admin kullanıcısının şifresi eşleşmiyor!");
                                System.out.println("  - Beklenen şifre: " + adminPassword);
                                System.out.println("  - Veritabanındaki şifre farklı olabilir");
                            }
                            
                            if (!isActive) {
                                System.out.println("⚠️ UYARI: Admin kullanıcısı pasif durumda!");
                            }
                            
                            // Admin rolünü kontrol et (sadece bilgi amaçlı)
                            if (!hasAdminRole(conn, userId)) {
                                System.out.println("⚠️ UYARI: Admin kullanıcısının ADMIN rolü yok!");
                            }
                            
                            adminUserChecked = true;
                            System.out.println("✅ Admin kullanıcısı kullanıma hazır: " + adminEmail);
                            return new AdminCredentials(adminEmail, adminPassword);
                        }
                    }
                }
            
                // Kullanıcı bulunamadı - yine de credential'ları döndür (backend'in oluşturduğu kullanıcıyı kullan)
                adminUserChecked = true; // Bir daha kontrol etme
                return new AdminCredentials(adminEmail, adminPassword);
                
            } catch (SQLException e) {
                // Hata durumunda varsayılan değerleri döndür (backend'in oluşturduğu kullanıcıyı kullan)
                adminUserChecked = true; // Hata olsa bile bir daha kontrol etme
                return new AdminCredentials(adminEmail, adminPassword);
            }
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
     * Test için admin kullanıcısı oluştur veya mevcut admin kullanıcısını kullan
     * Not: Gerçek uygulamada admin kullanıcıları manuel olarak oluşturulmalı
     * Test için: Önceden oluşturulmuş bir admin kullanıcısı kullan veya
     * Backend'de admin oluşturma endpoint'i kullan
     */
    
    /**
     * Kullanıcı girişi yap
     */
    protected void loginUser(String email, String password) {
        try {
            driver.get(BASE_URL + "/login");
            waitForPageLoad();
            Thread.sleep(1000); // Sayfanın yüklenmesini bekle
            
            // Eğer zaten dashboard'daysa önce logout yap
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("/dashboard") || currentUrl.contains("/yazar/") || 
                currentUrl.contains("/admin/") || currentUrl.contains("/reader/")) {
                logout();
                driver.get(BASE_URL + "/login");
                waitForPageLoad();
                Thread.sleep(1000);
            }
            
            System.out.println("🔐 Login başlatılıyor - Email: " + email);
            
            // Email input'unu bul ve doldur
            WebElement emailInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("email"))
            );
            emailInput.clear();
            emailInput.sendKeys(email);
            // React onChange event'ini tetikle
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", emailInput);
            Thread.sleep(200);
            
            System.out.println("✉️ Email girildi: " + emailInput.getAttribute("value"));
            
            // Password input'unu bul ve doldur
            WebElement passwordInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("password"))
            );
            passwordInput.clear();
            passwordInput.sendKeys(password);
            // React onChange event'ini tetikle
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", passwordInput);
            Thread.sleep(200);
            
            System.out.println("🔑 Password girildi (uzunluk: " + password.length() + ")");
            
            // Form submit
            WebElement form = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("form"))
            );
            WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
            );
            
            // Butonun disabled olmadığından emin ol
            if (submitButton.getAttribute("disabled") != null) {
                System.out.println("⚠️ Login submit butonu disabled, form değerlerini kontrol ediyoruz...");
                Thread.sleep(500); // 2000 -> 500
            }
            
            System.out.println("🖱️ Login formu gönderiliyor...");
            
            // Frontend'de login çağrısını yakalayabilmek için bir flag ekle
            ((JavascriptExecutor) driver).executeScript(
                "window.lastLoginAttempt = { email: arguments[0], timestamp: Date.now(), status: 'pending' };",
                email
            );
            
            safeSubmitForm(submitButton, form);
            System.out.println("✅ Form gönderildi, backend response bekleniyor...");
            
            // API çağrısının tamamlanmasını ve dashboard'a yönlendirilmeyi bekle
            System.out.println("Login işlemi tamamlanıyor, dashboard yönlendirmesi bekleniyor...");
            int loginWaitCount = 0;
            boolean loginCompleted = false;
            boolean errorFound = false;
            
            while (loginWaitCount < 20 && !loginCompleted && !errorFound) {
                Thread.sleep(500);
                currentUrl = driver.getCurrentUrl(); // Mevcut değişkeni kullan
                
                // Browser console'dan hata kontrolü (her iterasyonda)
                if (loginWaitCount % 2 == 0) { // Her 1 saniyede bir kontrol et
                    try {
                        org.openqa.selenium.logging.LogEntries logEntries = driver.manage().logs().get(org.openqa.selenium.logging.LogType.BROWSER);
                        for (org.openqa.selenium.logging.LogEntry entry : logEntries) {
                            String message = entry.getMessage();
                            // Login API çağrısı ile ilgili hataları kontrol et
                            if (message.contains("/api/auth/giris") && 
                                (message.contains("401") || message.contains("403") || message.contains("400") || 
                                 message.contains("500") || message.contains("SEVERE") || message.contains("ERROR"))) {
                                System.err.println("❌ Login API hatası tespit edildi: " + message);
                                errorFound = true;
                            }
                        }
                    } catch (Exception logEx) {
                        // Ignore
                    }
                }
                
                // Dashboard'lardan birine yönlendirildi mi?
                if (currentUrl.contains("/dashboard") || currentUrl.contains("/admin/") || 
                    currentUrl.contains("/yazar/") || currentUrl.contains("/reader/")) {
                    loginCompleted = true;
                    System.out.println("✅ Login başarılı. Dashboard URL: " + currentUrl);
                } else if (currentUrl.contains("/login")) {
                    // Hala login sayfasındaysak hata olabilir
                    try {
                        WebElement errorElement = driver.findElement(By.cssSelector(".auth-error"));
                        if (errorElement.isDisplayed()) {
                            String errorText = errorElement.getText();
                            System.err.println("❌ Login UI hatası: " + errorText);
                            errorFound = true;
                        }
                    } catch (Exception e) {
                        // Hata mesajı yoksa devam et
                    }
                } else if (currentUrl.endsWith("/") || currentUrl.equals(BASE_URL)) {
                    // Home sayfasına yönlendirildiyse, biraz daha bekle (rol bazlı yönlendirme için)
                    // Home sayfası kullanıcının rolüne göre dashboard'a yönlendirir
                    if (loginWaitCount % 4 == 0) {
                        System.out.println("🏠 Home sayfasında, dashboard yönlendirmesi bekleniyor... (" + loginWaitCount/2 + "s)");
                    }
                }
                loginWaitCount++;
            }
            
            if (!loginCompleted) {
                String finalUrl = driver.getCurrentUrl();
                System.err.println("❌ Login işlemi " + (loginWaitCount/2) + " saniye içinde dashboard'a yönlendirmedi. Final URL: " + finalUrl);
                
                // JavaScript'te login attempt flag'ini kontrol et
                try {
                    Object loginAttemptObj = ((JavascriptExecutor) driver).executeScript(
                        "return window.lastLoginAttempt;"
                    );
                    if (loginAttemptObj != null) {
                        System.out.println("🔍 Login Attempt Flag: " + loginAttemptObj.toString());
                    } else {
                        System.out.println("⚠️ Login Attempt Flag bulunamadı (form submit olmamış olabilir)");
                    }
                } catch (Exception jsEx) {
                    System.out.println("⚠️ Login Attempt Flag kontrolü başarısız: " + jsEx.getMessage());
                }
                
                // localStorage'dan token'ı kontrol et
                try {
                    Object tokenObj = ((JavascriptExecutor) driver).executeScript(
                        "return localStorage.getItem('token');"
                    );
                    if (tokenObj != null) {
                        String token = tokenObj.toString();
                        System.out.println("✅ Token localStorage'da mevcut (uzunluk: " + token.length() + ")");
                        
                        // Token'ı decode et ve rolleri kontrol et
                        try {
                            Object rolesObj = ((JavascriptExecutor) driver).executeScript(
                                "const token = localStorage.getItem('token');" +
                                "if (!token) return null;" +
                                "try {" +
                                "  const base64Url = token.split('.')[1];" +
                                "  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');" +
                                "  const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''));" +
                                "  const decoded = JSON.parse(jsonPayload);" +
                                "  return { roller: decoded.roller, roles: decoded.roles, userId: decoded.userId, email: decoded.sub };" +
                                "} catch(e) { return 'decode_error: ' + e.message; }"
                            );
                            System.out.println("📊 Token içeriği: " + (rolesObj != null ? rolesObj.toString() : "null"));
                        } catch (Exception decodeEx) {
                            System.out.println("⚠️ Token decode hatası: " + decodeEx.getMessage());
                        }
                    } else {
                        System.out.println("❌ Token localStorage'da YOK (login başarısız veya token kaydedilmemiş)");
                    }
                } catch (Exception tokenEx) {
                    System.out.println("⚠️ Token kontrolü başarısız: " + tokenEx.getMessage());
                }
                
                // Hata mesajı var mı kontrol et
                try {
                    WebElement errorElement = driver.findElement(By.cssSelector(".auth-error"));
                    if (errorElement.isDisplayed()) {
                        String errorText = errorElement.getText();
                        System.err.println("❌ Login UI hatası: " + errorText);
                    }
                } catch (Exception e) {
                    // Hata mesajı yoksa devam et
                }
                
                // window.lastLoginError flag'ini kontrol et (frontend'den gelen detaylı hata bilgisi)
                try {
                    String errorInfo = (String) ((JavascriptExecutor) driver).executeScript(
                        "if (!window.lastLoginError) return null;" +
                        "try {" +
                        "  return JSON.stringify(window.lastLoginError, null, 2);" +
                        "} catch(e) {" +
                        "  return 'Error parsing: ' + e.message;" +
                        "}"
                    );
                    if (errorInfo != null && !errorInfo.equals("null")) {
                        System.err.println("🔴 Backend Login Hatası Detayları:");
                        System.err.println(errorInfo);
                    }
                } catch (Exception jsEx) {
                    // Ignore
                }
                
                // Browser console'u kontrol et
                System.out.println("🔍 Login hatası - Browser console logları:");
                try {
                    org.openqa.selenium.logging.LogEntries logEntries = driver.manage().logs().get(org.openqa.selenium.logging.LogType.BROWSER);
                    boolean hasLoginApiLogs = false;
                    for (org.openqa.selenium.logging.LogEntry entry : logEntries) {
                        String message = entry.getMessage();
                        // Login API ile ilgili tüm logları göster
                        if (message.contains("/api/auth/giris") || message.contains("auth") || 
                            message.contains("401") || message.contains("403") || 
                            message.contains("ERROR") || message.contains("SEVERE")) {
                            System.err.println("  🔴 " + entry.getLevel() + ": " + message);
                            hasLoginApiLogs = true;
                        }
                    }
                    if (!hasLoginApiLogs) {
                        System.out.println("  ℹ️ Login API ile ilgili log bulunamadı. Backend çalışmıyor olabilir.");
                    }
                } catch (Exception logEx) {
                    System.err.println("Browser console logları alınamadı: " + logEx.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Login hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Kayıt sonrası dashboard'a yönlendirilme durumunu handle et
     * Eğer dashboard'daysa logout yap
     */
    protected void handlePostRegistrationRedirect() {
        try {
            Thread.sleep(500); // Kısa bekleme
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
            Thread.sleep(1000); // Sayfanın yüklenmesini bekle
            
            // Form alanlarını doldur ve React onChange event'ini tetikle
            WebElement firstNameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
            );
            firstNameInput.clear();
            firstNameInput.sendKeys(firstName);
            // React onChange event'ini tetikle
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", firstNameInput);
            
            WebElement lastNameInput = driver.findElement(By.id("lastName"));
            lastNameInput.clear();
            lastNameInput.sendKeys(lastName);
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", lastNameInput);
            
            WebElement emailInput = driver.findElement(By.id("email"));
            emailInput.clear();
            emailInput.sendKeys(email);
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", emailInput);
            
            WebElement usernameInput = driver.findElement(By.id("username"));
            usernameInput.clear();
            usernameInput.sendKeys(username);
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", usernameInput);
            
            WebElement passwordInput = driver.findElement(By.id("password"));
            passwordInput.clear();
            passwordInput.sendKeys(password);
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", passwordInput);
            
            // Tüm input event'lerinin işlenmesi için kısa bir bekleme
            Thread.sleep(100);
            
            // Role seçimi - READER (varsayılan, seçmeye gerek yok)
            
            // Submit butonuna tıkla
            WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
            );
            
            // Butonun disabled olmadığından emin ol
            if (submitButton.getAttribute("disabled") != null) {
                System.out.println("Submit butonu disabled, form değerlerini kontrol ediyoruz...");
                Thread.sleep(500); // 2000 -> 500
            }
            
            WebElement form = driver.findElement(By.tagName("form"));
            safeSubmitForm(submitButton, form);
            
            // API çağrısının tamamlanmasını bekle
            Thread.sleep(1000); // 3000 -> 1000
            
            String currentUrl = driver.getCurrentUrl();
            System.out.println("Kayıt sonrası URL: " + currentUrl);
            
            // Hata mesajı kontrolü
            try {
                WebElement errorElement = driver.findElement(By.cssSelector(".auth-error, .error, [role='alert']"));
                if (errorElement.isDisplayed()) {
                    String errorText = errorElement.getText();
                    System.out.println("Kayıt hatası: " + errorText);
                    
                    // window.lastRegistrationError flag'ini kontrol et (frontend'den gelen detaylı hata bilgisi)
                    try {
                        String errorInfo = (String) ((JavascriptExecutor) driver).executeScript(
                            "if (!window.lastRegistrationError) return null;" +
                            "try {" +
                            "  return JSON.stringify(window.lastRegistrationError, null, 2);" +
                            "} catch(e) {" +
                            "  return 'Error parsing: ' + e.message;" +
                            "}"
                        );
                        if (errorInfo != null && !errorInfo.equals("null")) {
                            System.err.println("🔴 Backend Kayıt Hatası Detayları:");
                            System.err.println(errorInfo);
                        }
                    } catch (Exception jsEx) {
                        // Ignore
                    }
                    
                    return false;
                }
            } catch (Exception e) {
                // Hata mesajı yoksa devam et
            }
            
            // Eğer login sayfasına yönlendirildiyse, otomatik giriş yap (Case1'deki mantık)
            if (currentUrl.contains("/login")) {
                loginUser(email, password);
                Thread.sleep(500); // 2000 -> 500
                currentUrl = driver.getCurrentUrl();
            }
            
            // Kayıt başarılı kontrolü
            return currentUrl.contains("/dashboard") || currentUrl.contains("/reader/dashboard") || 
                   currentUrl.contains("/yazar/dashboard") || currentUrl.contains("/admin/dashboard") ||
                   currentUrl.equals(BASE_URL + "/") || !currentUrl.contains("/register");
        } catch (Exception e) {
            System.err.println("Kullanıcı kaydı hatası: " + e.getMessage());
            e.printStackTrace();
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
            Thread.sleep(1000); // Sayfanın yüklenmesini bekle
            
            // Form alanlarını doldur ve React onChange event'ini tetikle
            WebElement firstNameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("firstName"))
            );
            firstNameInput.clear();
            firstNameInput.sendKeys(firstName);
            // React onChange event'ini tetikle
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", firstNameInput);
            
            WebElement lastNameInput = driver.findElement(By.id("lastName"));
            lastNameInput.clear();
            lastNameInput.sendKeys(lastName);
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", lastNameInput);
            
            WebElement emailInput = driver.findElement(By.id("email"));
            emailInput.clear();
            emailInput.sendKeys(email);
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", emailInput);
            
            WebElement usernameInput = driver.findElement(By.id("username"));
            usernameInput.clear();
            usernameInput.sendKeys(username);
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", usernameInput);
            
            WebElement passwordInput = driver.findElement(By.id("password"));
            passwordInput.clear();
            passwordInput.sendKeys(password);
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", passwordInput);
            
            // Tüm input event'lerinin işlenmesi için kısa bir bekleme
            Thread.sleep(100);
            
            // Role seçimi - WRITER (Case4g'deki gibi basit yaklaşım)
            WebElement roleSelectElement = null;
            try {
                roleSelectElement = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.id("roleName"))
                );
                org.openqa.selenium.support.ui.Select roleSelect = new org.openqa.selenium.support.ui.Select(roleSelectElement);
                try {
                    roleSelect.selectByValue("WRITER");
                } catch (Exception e) {
                    try {
                        roleSelect.selectByVisibleText("WRITER");
                    } catch (Exception e2) {
                        ((JavascriptExecutor) driver)
                            .executeScript("arguments[0].value = 'WRITER'; arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", roleSelectElement);
                    }
                }
                // Change event'ini tetikle
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", roleSelectElement);
                Thread.sleep(100);
            } catch (Exception e) {
                System.out.println("Role select bulunamadı: " + e.getMessage());
                return false;
            }
            
            // Form değerlerini kontrol et (debug için)
            String firstNameValue = firstNameInput.getAttribute("value");
            String emailValue = emailInput.getAttribute("value");
            String roleValue = roleSelectElement != null ? roleSelectElement.getAttribute("value") : "null";
            System.out.println("Form değerleri - firstName: " + firstNameValue + ", email: " + emailValue + ", role: " + roleValue);
            
            // Submit butonuna tıkla (Case1 ve Case4g'deki gibi safeSubmitForm kullan)
            WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
            );
            
            // Butonun disabled olmadığından emin ol
            if (submitButton.getAttribute("disabled") != null) {
                System.out.println("Submit butonu disabled, form değerlerini kontrol ediyoruz...");
                Thread.sleep(500); // 2000 -> 500
            }
            
            WebElement form = driver.findElement(By.tagName("form"));
            safeSubmitForm(submitButton, form);
            
            // API çağrısının tamamlanmasını bekle (Case1'deki gibi)
            Thread.sleep(1000); // 3000 -> 1000
            
            String currentUrl = driver.getCurrentUrl();
            System.out.println("Kayıt sonrası URL: " + currentUrl);
            
            // Browser console loglarını yakala ve yazdır
            try {
                org.openqa.selenium.logging.LogEntries logEntries = driver.manage().logs().get(org.openqa.selenium.logging.LogType.BROWSER);
                System.out.println("📋 Browser Console Logları (registerWriter):");
                boolean hasErrors = false;
                for (org.openqa.selenium.logging.LogEntry entry : logEntries) {
                    String level = entry.getLevel().toString();
                    String message = entry.getMessage();
                    if (level.contains("SEVERE") || level.contains("ERROR")) {
                        System.out.println("🔴 Browser Console ERROR: " + message);
                        hasErrors = true;
                    } else if (level.contains("WARNING")) {
                        System.out.println("⚠️ Browser Console WARNING: " + message);
                    } else if (message.contains("API") || message.contains("api") || message.contains("Base URL") || message.contains("CORS") || message.contains("Network")) {
                        System.out.println("📡 Browser Console INFO (API/Network): " + message);
                    }
                }
                if (hasErrors) {
                    System.out.println("⚠️ Browser console'da hatalar var, kayıt başarısız olabilir");
                }
            } catch (Exception e) {
                System.out.println("Browser console logları alınamadı: " + e.getMessage());
            }
            
            // Hata mesajı kontrolü
            try {
                WebElement errorElement = driver.findElement(By.cssSelector(".auth-error, .error, [role='alert']"));
                if (errorElement.isDisplayed()) {
                    String errorText = errorElement.getText();
                    System.out.println("Kayıt hatası: " + errorText);
                    
                    // window.lastRegistrationError flag'ini kontrol et (frontend'den gelen detaylı hata bilgisi)
                    try {
                        String errorInfo = (String) ((JavascriptExecutor) driver).executeScript(
                            "if (!window.lastRegistrationError) return null;" +
                            "try {" +
                            "  return JSON.stringify(window.lastRegistrationError, null, 2);" +
                            "} catch(e) {" +
                            "  return 'Error parsing: ' + e.message;" +
                            "}"
                        );
                        if (errorInfo != null && !errorInfo.equals("null")) {
                            System.err.println("🔴 Backend Kayıt Hatası Detayları:");
                            System.err.println(errorInfo);
                        }
                    } catch (Exception jsEx) {
                        // Ignore
                    }
                    
                    // Hata mesajının detaylarını al
                    try {
                        String errorHtml = errorElement.getAttribute("innerHTML");
                        System.out.println("Hata mesajı HTML: " + errorHtml);
                    } catch (Exception e) {
                        // Ignore
                    }
                    return false;
                }
            } catch (Exception e) {
                // Hata mesajı yoksa devam et
            }
            
            // Eğer login sayfasına yönlendirildiyse, otomatik giriş yap (Case1'deki mantık)
            if (currentUrl.contains("/login")) {
                // Login formunu doldur
                WebElement loginEmailInput = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.id("email"))
                );
                loginEmailInput.clear();
                loginEmailInput.sendKeys(email);
                
                WebElement loginPasswordInput = driver.findElement(By.id("password"));
                loginPasswordInput.clear();
                loginPasswordInput.sendKeys(password);
                
                // Giriş butonuna tıkla
                WebElement loginSubmitButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
                );
                WebElement loginForm = driver.findElement(By.tagName("form"));
                safeSubmitForm(loginSubmitButton, loginForm);
                
                // Giriş işleminin tamamlanmasını bekle
                Thread.sleep(1000); // 3000 -> 1000
            }
            
            // Dashboard'a yönlendirilmeyi bekle (Case1'deki gibi)
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/reader/dashboard"),
                ExpectedConditions.urlContains("/yazar/dashboard"),
                ExpectedConditions.urlContains("/admin/dashboard"),
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlToBe(BASE_URL + "/")
            ));
            
            currentUrl = driver.getCurrentUrl();
            boolean success = currentUrl.contains("/dashboard") || 
                   currentUrl.equals(BASE_URL + "/") ||
                   currentUrl.equals(BASE_URL + "/reader/dashboard") ||
                   currentUrl.equals(BASE_URL + "/yazar/dashboard") ||
                   currentUrl.equals(BASE_URL + "/admin/dashboard");
            
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
    protected static void logout() {
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
                    Thread.sleep(500); // 2000 -> 500
                } catch (Exception e1) {
                    // ProfileDropdown bulunamadıysa veya açılamadıysa direkt logout endpoint'ine git
                    try {
                        driver.get(BASE_URL + "/logout");
                        Thread.sleep(500); // 2000 -> 500
                    } catch (Exception e2) {
                        // Logout sayfası yoksa JavaScript ile temizle
                        try {
                            String url = driver.getCurrentUrl();
                            if (url != null && !url.startsWith("data:")) {
                                ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
                                ((JavascriptExecutor) driver).executeScript("window.sessionStorage.clear();");
                            }
                        } catch (Exception e3) {
                            // localStorage temizleme hatası - sessizce devam et
                        }
                        driver.manage().deleteAllCookies();
                    }
                }
            } else {
                // Dashboard'da değilse direkt temizle
                try {
                    String url = driver.getCurrentUrl();
                    if (url != null && !url.startsWith("data:")) {
                        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
                        ((JavascriptExecutor) driver).executeScript("window.sessionStorage.clear();");
                    }
                } catch (Exception e) {
                    // localStorage temizleme hatası - sessizce devam et
                }
                driver.manage().deleteAllCookies();
            }
        } catch (Exception e) {
            // Hata olursa localStorage ve cookies'i temizle
            try {
                String url = driver.getCurrentUrl();
                if (url != null && !url.startsWith("data:")) {
                    ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
                    ((JavascriptExecutor) driver).executeScript("window.sessionStorage.clear();");
                }
            } catch (Exception e2) {
                // localStorage temizleme hatası - sessizce devam et
            }
            try {
                driver.manage().deleteAllCookies();
            } catch (Exception e2) {
                // Cookie temizleme hatası - sessizce devam et
            }
        }
    }
    
    /**
     * URL'den story slug'ını al
     * Alert varsa otomatik kabul eder
     */
    protected String getStorySlugFromUrl() {
        try {
            // Alert varsa kabul et
            try {
                org.openqa.selenium.Alert alert = driver.switchTo().alert();
                String alertText = alert.getText();
                System.out.println("Alert tespit edildi: " + alertText);
                alert.accept();
                Thread.sleep(1000);
            } catch (Exception e) {
                // Alert yoksa devam et
            }
            
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("/haberler/")) {
                String slug = currentUrl.substring(currentUrl.indexOf("/haberler/") + "/haberler/".length());
                // Query string varsa kaldır
                if (slug.contains("?")) {
                    slug = slug.substring(0, slug.indexOf("?"));
                }
                return slug;
            }
        } catch (Exception e) {
            System.err.println("URL'den slug alınamadı: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Story slug'ını backend API üzerinden ID ile al
     */
    protected String getStorySlugViaApi(Long storyId) {
        try {
            String url = BACKEND_URL + "/api/haberler/" + storyId;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(response.body());
                if (node.has("slug")) {
                    return node.get("slug").asText();
                }
            } else {
                System.out.println("API slug isteği (id) başarısız: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("API'den story slug alınamadı: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Admin token'ı API üzerinden al
     */
    private String getAdminToken() {
        try {
            AdminCredentials adminCreds = ensureAdminUserExists();
            String url = BACKEND_URL + "/api/auth/giris";
            String payload = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", adminCreds.getEmail(), adminCreds.getPassword());
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
            
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(response.body());
                if (node.has("token")) {
                    return node.get("token").asText();
                }
            } else {
                System.out.println("API admin login isteği başarısız: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Admin token alınamadı: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Story'yi API üzerinden onayla
     */
    protected boolean approveStoryViaApi(Long storyId) {
        try {
            String token = getAdminToken();
            if (token == null) {
                System.out.println("Admin token alınamadı, API onayı atlanıyor");
                return false;
            }
            String url = BACKEND_URL + "/api/haberler/" + storyId + "/onayla";
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
            
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Story API üzerinden onaylandı: " + storyId);
                return true;
            } else {
                System.out.println("Story API onayı başarısız: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Story API üzerinden onaylanamadı: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Story ID'yi slug'dan al (veritabanından)
     */
    protected Long getStoryIdFromSlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return null;
        }
        
        // Önce API'den dene (Jenkins'te DB yok)
        Long apiId = getStoryIdViaApiBySlug(slug);
        if (apiId != null) {
            System.out.println("Story ID API üzerinden alındı: " + apiId + " (slug: " + slug + ")");
            return apiId;
        }
        
        if (USE_DB) {
            // Slug'dan ID almayı dene (yalnızca local geliştirme için)
        try (Connection conn = getTestDatabaseConnection()) {
            String sql = "SELECT id FROM stories WHERE slug = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, slug);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Long id = rs.getLong("id");
                        System.out.println("Story ID slug'dan alındı: " + id + " (slug: " + slug + ")");
                        return id;
                    }
                }
            }
            
                // Slug bulunamazsa, slug'ın son kısmını dene (URL format farkı için)
            String slugPart = slug;
            if (slug.contains("/")) {
                slugPart = slug.substring(slug.lastIndexOf("/") + 1);
            }
            if (!slugPart.equals(slug)) {
                sql = "SELECT id FROM stories WHERE slug = ? OR slug LIKE ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, slugPart);
                    stmt.setString(2, "%" + slugPart);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Long id = rs.getLong("id");
                            System.out.println("Story ID slug'dan alındı (partial match): " + id + " (slug: " + slugPart + ")");
                            return id;
                        }
                    }
                }
            }
        } catch (SQLException e) {
                System.err.println("Story ID slug'dan alınamadı (DB): " + e.getMessage());
        }
        }
        
        return null;
    }
    
    /**
     * Email'den kullanıcı ID'sini al
     */
    protected Long getUserIdByEmail(String email) {
        Long apiUserId = getUserIdViaApi(email);
        if (apiUserId != null) {
            System.out.println("Kullanıcı ID API üzerinden alındı: " + apiUserId + " (email: " + email + ")");
            return apiUserId;
        }
        
        if (USE_DB) {
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
                System.err.println("Kullanıcı ID alınamadı (DB): " + e.getMessage());
            }
        }
        return null;
    }
    
    /**
     * Kullanıcı ID'yi backend API üzerinden email ile al
     */
    private Long getUserIdViaApi(String email) {
        try {
            String encoded = URLEncoder.encode(email, StandardCharsets.UTF_8);
            String url = BACKEND_URL + "/api/kullanicilar/email/" + encoded;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(response.body());
                if (node.has("id")) {
                    return node.get("id").asLong();
                }
            } else {
                System.out.println("API user isteği başarısız: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("API'den kullanıcı ID alınamadı: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Kullanıcının en son story'sini backend API üzerinden al
     */
    private Long getLatestStoryIdViaApi(Long userId) {
        try {
            String url = BACKEND_URL + "/api/haberler/kullanici/" + userId + "?size=1";
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(response.body());
                JsonNode content = node.get("content");
                if (content != null && content.isArray() && content.size() > 0) {
                    JsonNode first = content.get(0);
                    if (first.has("id")) {
                        return first.get("id").asLong();
                    }
                }
            } else {
                System.out.println("API story listesi isteği başarısız: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("API'den en son story ID alınamadı: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Beğeni sayısını backend API üzerinden al
     */
    protected Long getLikeCountViaApi(Long storyId) {
        try {
            String url = BACKEND_URL + "/api/begeniler/haber/" + storyId + "/sayi";
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return Long.parseLong(response.body());
            } else {
                System.out.println("API beğeni sayısı isteği başarısız: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("API'den beğeni sayısı alınamadı: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Story detayını backend API üzerinden al (başlık/içerik kontrolü için)
     */
    protected JsonNode getStoryViaApi(Long storyId) {
        try {
            String url = BACKEND_URL + "/api/haberler/" + storyId;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readTree(response.body());
            } else {
                System.out.println("API story detay isteği başarısız: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("API'den story alınamadı: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Kullanıcının aktiflik durumunu API üzerinden al
     */
    protected Boolean getUserActiveStatusViaApi(Long userId) {
        try {
            String url = BACKEND_URL + "/api/kullanicilar/" + userId;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(response.body());
                if (node.has("aktif") || node.has("isActive")) {
                    return node.has("aktif") ? node.get("aktif").asBoolean() : node.get("isActive").asBoolean();
                }
            } else {
                System.out.println("API user detail isteği başarısız: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("API'den user aktif durumu alınamadı: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Kullanıcının bir story'i kaydedip kaydetmediğini API üzerinden kontrol et
     */
    protected Boolean getSaveStatusViaApi(Long userId, Long storyId) {
        try {
            String url = BACKEND_URL + "/api/kaydedilenler/kullanici/" + userId + "/story/" + storyId;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(response.body());
                // API true/false dönüyor varsayımı
                if (node.isBoolean()) {
                    return node.asBoolean();
                }
                // Eğer obje dönüyorsa isActive alanını oku
                if (node.has("isActive")) {
                    return node.get("isActive").asBoolean();
                }
            } else {
                System.out.println("API save status isteği başarısız: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("API'den save status alınamadı: " + e.getMessage());
        }
        return null;
    }
    
    protected String getStoryTitleViaApi(Long storyId) {
        JsonNode node = getStoryViaApi(storyId);
        if (node != null) {
            if (node.has("baslik")) return node.get("baslik").asText();
            if (node.has("title")) return node.get("title").asText();
        }
        return null;
    }
    
    protected String getStoryContentViaApi(Long storyId) {
        JsonNode node = getStoryViaApi(storyId);
        if (node != null) {
            if (node.has("icerik")) return node.get("icerik").asText();
            if (node.has("content")) return node.get("content").asText();
        }
        return null;
    }
    
    /**
     * Story ID'yi backend API üzerinden slug ile al
     */
    private Long getStoryIdViaApiBySlug(String slug) {
        try {
            String url = BACKEND_URL + "/api/haberler/slug/" + slug;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(response.body());
                if (node.has("id")) {
                    return node.get("id").asLong();
                }
            } else {
                System.out.println("API slug isteği başarısız: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("API'den story ID alınamadı (slug): " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Story ID'yi backend API üzerinden başlıkla arayarak al
     */
    private Long getStoryIdViaApiByTitle(String title) {
        try {
            String encoded = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String url = BACKEND_URL + "/api/haberler/arama?q=" + encoded + "&size=1";
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(response.body());
                JsonNode content = node.get("content");
                if (content != null && content.isArray() && content.size() > 0) {
                    JsonNode first = content.get(0);
                    if (first.has("id")) {
                        return first.get("id").asLong();
                    }
                }
            } else {
                System.out.println("API başlık arama isteği başarısız: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("API'den story ID alınamadı (title): " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Kullanıcının en son oluşturduğu story ID'sini al
     */
    protected Long getLatestStoryIdByUserEmail(String userEmail) {
        Long userId = getUserIdByEmail(userEmail);
        if (userId != null) {
            Long apiStoryId = getLatestStoryIdViaApi(userId);
            if (apiStoryId != null) {
                System.out.println("En son story ID API üzerinden alındı: " + apiStoryId + " (userId: " + userId + ")");
                return apiStoryId;
            }
        }
        
        if (USE_DB) {
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
                System.err.println("Kullanıcının en son story ID'si alınamadı (DB): " + e.getMessage());
            }
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
        
        Thread.sleep(1000); // 3000 -> 1000
        WebElement publishButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".publish-button, button.publish-button")
            )
        );
        
        // Scroll to button
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", publishButton);
        Thread.sleep(500); // 2000 -> 500
        
        publishButton.click();
        
        Thread.sleep(500); // kısaltıldı
        
        // Alert'leri kontrol et ve kabul et
        try {
            org.openqa.selenium.Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            System.out.println("Publish sonrası alert: " + alertText);
            alert.accept();
            Thread.sleep(1000); // 3000 -> 1000
        } catch (Exception alertEx) {
            // Alert yoksa devam et
        }
        
        waitForPageLoad();
        Thread.sleep(500); // kısaltıldı
    }
    
    /**
     * Text bloğuna kod bloğu ekle
     * @param textBlock Hover yapılacak text bloğu (textarea) - null ise boş text bloğu bulunur
     * @param codeContent Kod içeriği
     */
    protected void addCodeBlock(WebElement textBlock, String codeContent) throws Exception {
        // Boş text bloğu bul (buton sadece boş text bloğunda görünür)
        java.util.List<WebElement> textBlocks = driver.findElements(By.cssSelector("textarea.block-textarea"));
        WebElement emptyTextBlock = null;
        
        // Önce boş text bloğu ara
        for (WebElement block : textBlocks) {
            String content = block.getAttribute("value");
            if (content == null || content.trim().isEmpty()) {
                emptyTextBlock = block;
                break;
            }
        }
        
        // Boş text bloğu bulunamazsa, son text bloğunu kullan
        // (Frontend'de kod bloğu eklendikten sonra yeni boş text bloğu oluşur)
        if (emptyTextBlock == null && !textBlocks.isEmpty()) {
            emptyTextBlock = textBlocks.get(textBlocks.size() - 1);
        } else if (emptyTextBlock == null && textBlock != null) {
            emptyTextBlock = textBlock;
        }
        
        if (emptyTextBlock == null) {
            throw new Exception("Boş text bloğu bulunamadı");
        }
        
        // Text bloğuna hover yap (JavaScript ile hover simüle et)
        Actions actions = new Actions(driver);
        actions.moveToElement(emptyTextBlock).perform();
        Thread.sleep(1000);
        
        // JavaScript ile hover event'ini tetikle (React'ın hover state'ini güncellemek için)
        ((JavascriptExecutor) driver).executeScript(
            "var event = new MouseEvent('mouseenter', { bubbles: true, cancelable: true }); " +
            "arguments[0].dispatchEvent(event);", emptyTextBlock);
        Thread.sleep(500);
        
        // + butonunu bekle ve tıkla (visible class'ı olan)
        WebElement addButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".block-add-button.visible, .editor-block .block-add-button.visible")
            )
        );
        addButton.click();
        Thread.sleep(1000);
        
        // Kod butonuna tıkla
        WebElement codeMenuButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".block-add-menu button[title='Kod'], .block-add-menu button:nth-child(4)")
            )
        );
        codeMenuButton.click();
        Thread.sleep(1000);
        
        // Kod bloğunu doldur
        WebElement codeBlock = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("textarea.code-editor-inline-textarea, .code-editor-inline textarea")
            )
        );
        codeBlock.clear();
        codeBlock.sendKeys(codeContent);
        
        Thread.sleep(1000);
        WebElement confirmButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".code-editor-btn.confirm, button.code-editor-btn[title='Onayla']")
            )
        );
        confirmButton.click();
        Thread.sleep(500);
    }
    
    /**
     * Story başlığından story ID'sini al (retry logic ile)
     * @param title Story başlığı
     * @param userEmail Opsiyonel: Kullanıcı email'i (fallback için)
     * @return Story ID veya null
     */
    protected Long getStoryIdByTitle(String title) {
        return getStoryIdByTitle(title, null);
    }
    
    /**
     * Story başlığından story ID'sini al (retry logic ile)
     * @param title Story başlığı
     * @param userEmail Opsiyonel: Kullanıcı email'i (fallback için)
     * @return Story ID veya null
     */
    protected Long getStoryIdByTitle(String title, String userEmail) {
        // Öncelik: URL -> API -> (opsiyonel) DB
        try {
            String currentUrl = driver.getCurrentUrl();
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("/(?:haberler|yazar/haber-duzenle)/(\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(currentUrl);
            if (matcher.find()) {
                Long idFromUrl = Long.parseLong(matcher.group(1));
                System.out.println("Story ID URL'den alındı: " + idFromUrl);
                return idFromUrl;
            }
        } catch (Exception e) {
            // URL'den alınamazsa devam
        }
        
        Long apiId = getStoryIdViaApiByTitle(title);
        if (apiId != null) {
            System.out.println("Story ID API aramasından alındı: " + apiId + " (title: " + title + ")");
            return apiId;
        }
        
        // Kullanıcının en son story'sini API ile dene
        if (userEmail != null) {
            Long latestId = getLatestStoryIdByUserEmail(userEmail);
            if (latestId != null) {
                System.out.println("Story ID kullanıcının en son story'sinden (API/DB fallback) alındı: " + latestId);
                return latestId;
            }
        }
        
        if (USE_DB) {
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
                System.err.println("Story ID başlıktan alınamadı (DB): " + e.getMessage());
            }
        }
        
        return null;
    }
    
    /**
     * Veritabanı üzerinden story onayla
     */
    protected void approveStoryViaBackend(Long storyId, Long adminId) {
        if (!USE_DB) {
            System.out.println("approveStoryViaBackend atlandı (USE_DB=false)");
            return;
        }
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
            Thread.sleep(500);
            
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
            Thread.sleep(500);
            
            // Alert'leri kontrol et ve kabul et
            try {
                org.openqa.selenium.Alert alert = driver.switchTo().alert();
                String alertText = alert.getText();
                System.out.println("Publish sonrası alert: " + alertText);
                alert.accept();
                Thread.sleep(500); // 2000 -> 500
            } catch (Exception alertEx) {
                // Alert yoksa devam et
            }
            
            // Publish işleminin tamamlanmasını bekle (URL değişimi veya başarı mesajı)
            // Ya story slug sayfasına yönlendiriliriz ya da dashboard'a gideriz
            System.out.println("Publish işlemi bekleniyor...");
            int publishWaitCount = 0;
            boolean publishCompleted = false;
            while (publishWaitCount < 20 && !publishCompleted) {
                Thread.sleep(500);
                currentUrl = driver.getCurrentUrl(); // Mevcut değişkeni kullan
                // Story sayfasına yönlendirildi mi veya dashboard'da mıyız?
                if (currentUrl.contains("/haberler/") || currentUrl.contains("/dashboard") || currentUrl.contains("/yazar/")) {
                    publishCompleted = true;
                    System.out.println("Publish işlemi tamamlandı. URL: " + currentUrl);
                } else {
                    // Publish butonunu kontrol et - disabled veya "Yayınlanıyor..." yazısı var mı?
                    try {
                        WebElement pubBtn = driver.findElement(By.cssSelector(".publish-button"));
                        String btnText = pubBtn.getText();
                        boolean isDisabled = !pubBtn.isEnabled() || btnText.contains("Yayınlanıyor");
                        if (!isDisabled) {
                            // Buton tekrar aktif oldu, publish tamamlandı
                            publishCompleted = true;
                            System.out.println("Publish butonu tekrar aktif oldu.");
                        }
                    } catch (Exception e) {
                        // Buton bulunamadı, devam et
                    }
                }
                publishWaitCount++;
            }
            
            if (!publishCompleted) {
                System.out.println("⚠️ Publish işlemi 10 saniye içinde tamamlanmadı, devam ediliyor...");
            }
            
            Thread.sleep(500); // Ek güvenlik için bekle
            
            // Story ID'yi al (retry ile
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
            
            // Story slug'ını al (yayınlandıktan sonra URL'den veya veritabanından)
            String storySlug = null;
            if (storyId != null) {
                storySlug = getStorySlugViaApi(storyId);
                
                if (storySlug == null && USE_DB) {
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
            }
            
            // URL'den slug almayı dene
            if (storySlug == null) {
                storySlug = getStorySlugFromUrl();
            }
            
            // Hala bulunamazsa title'dan oluştur
            if (storySlug == null) {
                storySlug = storyTitle.toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")
                    .replaceAll("\\s+", "-")
                    .replaceAll("-+", "-");
            }
            
            // Writer'dan logout yap (admin onayı için hazırlık)
            try {
                driver.get(BASE_URL + "/logout");
                Thread.sleep(500); // 2000 -> 500
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
     * Admin dashboard'da tüm sayfaları gezip story'yi bul
     * @param storyTitle Story başlığı
     * @return Story element'i veya null
     */
    protected WebElement findStoryInAllPages(String storyTitle) {
        try {
            driver.get(BASE_URL + "/admin/dashboard");
            waitForPageLoad();
            wait.until(
                ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-dashboard-container")),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-haber-item"))
                )
            );
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
            Thread.sleep(500);
            
            int currentPage = 1;
            int maxPages = 50; // güvenlik için üst sınır
            while (currentPage <= maxPages) {
                try {
                    WebElement storyElement = wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//div[contains(@class, 'admin-haber-item')]//*[contains(text(), '" + storyTitle + "')] | //*[contains(@class, 'admin-haber-item')]//*[contains(text(), '" + storyTitle + "')]")
                        )
                    );
                    System.out.println("Story bulundu (sayfa " + currentPage + "): " + storyTitle);
                    return storyElement;
                } catch (org.openqa.selenium.TimeoutException e) {
                    // bu sayfada yok, sonraki sayfayı dene
                    try {
                        WebElement nextButton = driver.findElement(
                            By.xpath("//div[contains(@class, 'admin-pagination')]//button[contains(text(), 'Sonraki') or contains(text(), 'Next')]")
                        );
                        if (nextButton.getAttribute("disabled") != null) {
                            System.out.println("Story bulunamadı, son sayfaya ulaşıldı: " + storyTitle);
                            return null;
                        }
                        safeClick(nextButton);
                        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
                        Thread.sleep(500);
                        currentPage++;
                    } catch (org.openqa.selenium.NoSuchElementException ex) {
                        System.out.println("Story bulunamadı, pagination yok: " + storyTitle);
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Story aranırken hata oluştu: " + storyTitle + " - " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Admin kullanıcı sayfasında tüm sayfaları gezip kullanıcıyı bul
     * @param userEmail Kullanıcı email'i
     * @return Kullanıcı element'i veya null
     */
    protected WebElement findUserInAllPages(String userEmail) {
        // Email yerine kullanıcı adı ile arama yapmak için overload edilmiş metodu kullan
        // Email'den kullanıcı adını çıkar (email formatından)
        String username = userEmail;
        if (userEmail.contains("@")) {
            // Email formatından kullanıcı adını çıkar
            username = userEmail.substring(0, userEmail.indexOf("@"));
        }
        return findUserInAllPagesByUsername(username);
    }
    
    protected WebElement findUserInAllPagesByUsername(String username) {
        try {
            // Kullanıcılar sayfasına git
            driver.get(BASE_URL + "/admin/users");
            waitForPageLoad();
            Thread.sleep(3000);
            
            // Sayfa yüklemesini bekle
            wait.until(
                ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-dashboard-container")),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-table"))
                )
            );
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
            Thread.sleep(500);
            
            // Arama çubuğunu bul
            System.out.println("Kullanıcı arama çubuğu ile aranıyor (kullanıcı adı): " + username);
            
            WebElement searchInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("form.admin-search-form input[type='text'], .admin-search-input, input[placeholder*='Email veya ad ile ara']")
                )
            );
            
            // Arama çubuğunu görünür yap
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", searchInput);
            Thread.sleep(500);
            
            // Arama çubuğunu temizle ve kullanıcı adını gir
            searchInput.clear();
            Thread.sleep(300);
            searchInput.sendKeys(username);
            Thread.sleep(500);
            
            System.out.println("Arama çubuğuna kullanıcı adı girildi: " + username);
            
            // Arama butonunu bul ve tıkla (en güvenilir yöntem)
            WebElement searchButton = null;
            try {
                // Önce form'u bul
                WebElement searchForm = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("form.admin-search-form")
                    )
                );
                
                // Arama butonunu bul - önce submit butonunu dene
                try {
                    searchButton = wait.until(
                        ExpectedConditions.elementToBeClickable(
                            searchForm.findElement(By.cssSelector("button[type='submit']"))
                        )
                    );
                    System.out.println("Submit butonu bulundu, tıklanıyor...");
                } catch (Exception e1) {
                    // Submit butonu yoksa, admin-btn-secondary class'ına sahip butonu bul
                    searchButton = wait.until(
                        ExpectedConditions.elementToBeClickable(
                            searchForm.findElement(By.cssSelector("button.admin-btn-secondary"))
                        )
                    );
                    System.out.println("Ara butonu bulundu, tıklanıyor...");
                }
                
                // Butonu JavaScript ile tıkla (daha güvenilir)
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", searchButton);
                System.out.println("Arama butonu tıklandı");
                
            } catch (Exception e1) {
                System.out.println("Arama butonu bulunamadı, form submit deneniyor...");
                // Buton bulunamazsa form'u JavaScript ile submit et
                try {
                    WebElement searchForm = searchInput.findElement(By.xpath("./ancestor::form"));
                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].submit();", searchForm);
                    System.out.println("Form JavaScript ile submit edildi");
                } catch (Exception e2) {
                    System.out.println("Form submit başarısız, Enter tuşu deneniyor...");
                    // Son çare: Enter tuşu
                    searchInput.sendKeys(org.openqa.selenium.Keys.RETURN);
                }
            }
            
            Thread.sleep(1000);
            System.out.println("Arama yapıldı, sonuçlar bekleniyor...");
            
            // Arama sonuçlarının yüklenmesini bekle
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
            Thread.sleep(3000);
            
            // Tablo görünür olana kadar bekle
            wait.until(
                ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-table")),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-empty-state"))
                )
            );
            Thread.sleep(1000);
            
            // Kullanıcıyı bul - kullanıcı adı kolonunda ara
            WebElement userElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//table//tr//td[contains(text(), '" + username + "')]")
                )
            );
            System.out.println("Kullanıcı bulundu (kullanıcı adı): " + username);
            return userElement;
            
        } catch (org.openqa.selenium.TimeoutException e) {
            // Kullanıcı bulunamadı - sayfadaki tüm kullanıcı adlarını logla
            try {
                java.util.List<WebElement> allUsernames = driver.findElements(By.xpath("//table//tr//td[3]")); // Kullanıcı adı kolonu
                System.out.println("Sayfadaki kullanıcı adları:");
                for (WebElement usernameCell : allUsernames) {
                    System.out.println("  - " + usernameCell.getText());
                }
            } catch (Exception ex) {
                System.out.println("Kullanıcı adları loglanamadı: " + ex.getMessage());
            }
            System.out.println("Kullanıcı arama ile bulunamadı (kullanıcı adı): " + username);
            return null;
        } catch (Exception e) {
            System.out.println("Kullanıcı arama ile bulunamadı (kullanıcı adı): " + username + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Admin yorumlar sayfasında tüm sayfaları gezip yorumu bul
     * @param commentText Yorum metni
     * @param status Yorum durumu (ONAY_BEKLIYOR, ONAYLANDI, REDDEDILDI)
     * @return Yorum element'i veya null
     */
    protected WebElement findCommentInAllPages(String commentText, String status) {
        try {
            // İlk sayfayı kontrol et
            driver.get(BASE_URL + "/admin/comments");
            waitForPageLoad();
            Thread.sleep(1000);
            
            // Durum seçimini yap
            if (status != null) {
                try {
                    WebElement statusSelect = wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("select.admin-select")
                        )
                    );
                    org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(statusSelect);
                    select.selectByValue(status);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    // Status select bulunamadı, devam et
                }
            }
            
            int currentPage = 0;
            int maxPages = 100; // Maksimum sayfa sayısı (güvenlik için)
            
            while (currentPage < maxPages) {
                try {
                    // Yorumu bulmayı dene
                    WebElement commentElement = wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//div[contains(@class, 'admin-haber-item')]//*[contains(text(), '" + commentText + "')]")
                        )
                    );
                    return commentElement;
                } catch (org.openqa.selenium.TimeoutException e) {
                    // Yorum bu sayfada bulunamadı, sonraki sayfaya geç
                    try {
                        // Pagination butonlarını kontrol et
                        WebElement nextButton = driver.findElement(
                            By.xpath("//div[contains(@class, 'admin-pagination')]//button[contains(text(), 'Sonraki')]")
                        );
                        
                        // Buton disabled mı kontrol et
                        if (nextButton.getAttribute("disabled") != null) {
                            // Son sayfaya ulaşıldı
                            break;
                        }
                        
                        // Sonraki sayfaya git
                        safeClick(nextButton);
                        waitForPageLoad();
                        Thread.sleep(1000);
                        currentPage++;
                    } catch (org.openqa.selenium.NoSuchElementException ex) {
                        // Pagination butonu yok, son sayfadayız
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Yorum tüm sayfalarda arandı ama bulunamadı: " + commentText);
        }
        return null;
    }
    
    /**
     * Admin etiketler sayfasında tüm sayfaları gezip etiketi bul
     * @param tagName Etiket adı
     * @return Etiket satırı (tr element'i) veya null
     */
    protected WebElement findTagInAllPages(String tagName) {
        try {
            // Sayfayı yenile ve etiketler sayfasına git
            driver.get(BASE_URL + "/admin/etiketler");
            waitForPageLoad();
            Thread.sleep(3000);
            
            // Sayfa yüklemesini bekle
            wait.until(
                ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-dashboard-container")),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-table"))
                )
            );
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
            Thread.sleep(500);
            
            int currentPage = 0;
            int maxPages = 10; // Maksimum 10 sayfa kontrol et (optimizasyon)
            
            while (currentPage < maxPages) {
                System.out.println("Etiket aranıyor (sayfa " + (currentPage + 1) + "): " + tagName);
                
                try {
                    // Önce td elementini bul, sonra parent tr'yi al
                    WebElement tagTd = wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//table//tbody//tr//td[contains(text(), '" + tagName + "')]")
                        )
                    );
                    // Parent tr elementini al
                    WebElement tagRow = tagTd.findElement(By.xpath("./parent::tr"));
                    System.out.println("Etiket bulundu (sayfa " + (currentPage + 1) + "): " + tagName);
                    return tagRow;
                } catch (org.openqa.selenium.TimeoutException e) {
                    // Etiket bu sayfada bulunamadı, sonraki sayfaya geç
                    try {
                        // Pagination butonlarını kontrol et - birden fazla selector dene
                        WebElement nextButton = null;
                        try {
                            nextButton = driver.findElement(
                                By.xpath("//div[contains(@class, 'admin-pagination')]//button[contains(text(), 'Sonraki')]")
                            );
                        } catch (Exception ex1) {
                            try {
                                nextButton = driver.findElement(
                                    By.cssSelector(".admin-pagination button:not([disabled])")
                                );
                            } catch (Exception ex2) {
                                // Pagination butonu bulunamadı
                                throw new org.openqa.selenium.NoSuchElementException("Pagination butonu bulunamadı");
                            }
                        }
                        
                        // Buton disabled mı kontrol et - hem attribute hem de class kontrolü
                        String disabledAttr = nextButton.getAttribute("disabled");
                        boolean isDisabled = disabledAttr != null && !disabledAttr.isEmpty();
                        
                        if (isDisabled) {
                            // Son sayfaya ulaşıldı
                            System.out.println("Son sayfaya ulaşıldı, etiket bulunamadı: " + tagName);
                            break;
                        }
                        
                        // Sonraki sayfaya git
                        System.out.println("Sonraki sayfaya geçiliyor... (sayfa " + (currentPage + 2) + ")");
                        safeClick(nextButton);
                        
                        // Loading'in bitmesini bekle
                        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
                        Thread.sleep(500);
                        
                        // Tablonun yüklendiğini bekle
                        wait.until(
                            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".admin-table tbody"))
                        );
                        Thread.sleep(1000);
                        
                        currentPage++;
                    } catch (org.openqa.selenium.NoSuchElementException ex) {
                        // Pagination butonu yok, son sayfadayız
                        System.out.println("Pagination butonu yok veya son sayfadayız");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Etiket tüm sayfalarda arandı ama bulunamadı: " + tagName + " - Hata: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Admin editör seçimleri sayfasında tüm sayfaları gezip story'yi bul
     * @param storyTitle Story başlığı
     * @return Story element'i veya null
     */
    protected WebElement findStoryInEditorPicksAllPages(String storyTitle) {
        try {
            // İlk sayfayı kontrol et
            driver.get(BASE_URL + "/admin/editor-secimleri");
            waitForPageLoad();
            Thread.sleep(1000);
            
            int currentPage = 0;
            int maxPages = 100; // Maksimum sayfa sayısı (güvenlik için)
            
            while (currentPage < maxPages) {
                try {
                    // Story'yi bulmayı dene
                    WebElement storyElement = wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//div[contains(@class, 'admin-editor-pick-item')]//*[contains(text(), '" + storyTitle + "')]")
                        )
                    );
                    return storyElement;
                } catch (org.openqa.selenium.TimeoutException e) {
                    // Story bu sayfada bulunamadı, sonraki sayfaya geç
                    try {
                        // Pagination butonlarını kontrol et
                        WebElement nextButton = driver.findElement(
                            By.xpath("//div[contains(@class, 'admin-pagination')]//button[contains(text(), 'Sonraki')]")
                        );
                        
                        // Buton disabled mı kontrol et
                        if (nextButton.getAttribute("disabled") != null) {
                            // Son sayfaya ulaşıldı
                            break;
                        }
                        
                        // Sonraki sayfaya git
                        safeClick(nextButton);
                        waitForPageLoad();
                        Thread.sleep(1000);
                        currentPage++;
                    } catch (org.openqa.selenium.NoSuchElementException ex) {
                        // Pagination butonu yok, son sayfadayız
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Story editör seçimlerinde tüm sayfalarda arandı ama bulunamadı: " + storyTitle);
        }
        return null;
    }
    
    /**
     * Admin olarak giriş yap ve story'yi onayla
     * @param storyTitle Story başlığı (onay bekleyen story'yi bulmak için)
     * @return Story slug'ı (onaylandıktan sonra)
     */
    protected String approveStoryAsAdmin(String storyTitle) {
        try {
            try { driver.get(BASE_URL + "/logout"); Thread.sleep(500); } catch (Exception ignored) {}
            AdminCredentials adminCreds = ensureAdminUserExists();
            loginUser(adminCreds.getEmail(), adminCreds.getPassword());
            
            WebElement storyTextElement = findStoryInAllPages(storyTitle);
            if (storyTextElement == null) {
                System.out.println("Story UI'da bulunamadı: " + storyTitle);
                return null;
            }
            
            WebElement storyRow = storyTextElement.findElement(By.xpath("./ancestor::div[contains(@class, 'admin-haber-item')]"));
            WebElement approveButton = storyRow.findElement(
                By.xpath(".//button[contains(text(), 'Onayla') or contains(text(), 'onayla') or contains(@class, 'approve')]")
            );
            
            // Scroll & güvenli tıklama
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", approveButton);
            safeClick(approveButton);
            
            Thread.sleep(1000);
            try { driver.switchTo().alert().accept(); } catch (Exception ignored) {}
            Thread.sleep(1000);
            
            // Slug'ı API veya title'dan türet
            Long storyId = getStoryIdByTitle(storyTitle, null);
            if (storyId != null) {
                String slug = getStorySlugViaApi(storyId);
                if (slug != null) return slug;
            }
            return storyTitle.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        } catch (Exception e) {
            System.err.println("Admin onaylama hatası: " + e.getMessage());
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
            Thread.sleep(500);
            
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
                        Thread.sleep(500); // 2000 -> 500
                    } catch (Exception ex) {
                        // Logout sayfası yoksa devam et
                    }
                    
                    // Admin olarak giriş yap
                    loginUser(adminCreds.getEmail(), adminCreds.getPassword());
                    
                    // Admin dashboard'a git
                    driver.get(BASE_URL + "/admin/dashboard");
                    waitForPageLoad();
                    Thread.sleep(1000); // 3000 -> 1000
                    
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
                    
                    Thread.sleep(1000); // 3000 -> 1000
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

