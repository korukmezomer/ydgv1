package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 12d: Admin Kategori Yönetimi (Admin Category Management)
 * 
 * Test Senaryoları:
 * 1. Admin kategori ekleyebilmeli
 * 2. Admin kategori düzenleyebilmeli
 * 3. Admin kategori silebilmeli
 * 4. Admin kategori arayabilmeli
 */
@DisplayName("Case 12d: Admin Kategori Yönetimi")
public class Case12d_AdminCategoryManagementTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 12d: Admin kategori ekleyebilmeli")
    public void case12d_AdminAddCategory() {
        try {
            // 1. ADMIN olarak giriş yap (BaseSeleniumTest'teki loginUser helper metodunu kullan)
            AdminCredentials adminCreds = ensureAdminUserExists();
            loginUser(adminCreds.getEmail(), adminCreds.getPassword());
            
            // Admin dashboard'a yönlendirildiğini kontrol et
            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/admin") || currentUrl.contains("/dashboard"),
                "Case 12d: Admin olarak giriş yapılamadı. URL: " + currentUrl
            );
            
            // 2. Sidebar'ı aç ve "Kategoriler" linkine tıkla
            try {
                // Header içindeki hamburger menu butonunu bul (daha spesifik)
                WebElement sidebarToggle = wait.until(
                    ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".reader-header button.hamburger-menu, .reader-header-content button.hamburger-menu, button.hamburger-menu[aria-label='Menu']")
                    )
                );
                
                // Butonun görünür olduğundan emin ol
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", sidebarToggle);
                Thread.sleep(500);
                
                // JavaScript ile tıkla (React state güncellemesi için)
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", sidebarToggle);
                Thread.sleep(1000);
                
                // Sidebar'ın açılmasını bekle
                wait.until(
                    ExpectedConditions.and(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(".writer-sidebar.open")),
                        ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".writer-sidebar.open"))
                    )
                );
                Thread.sleep(1000);
            } catch (Exception e) {
                // Sidebar toggle bulunamadıysa direkt URL'e git
                System.out.println("Case 12d: Sidebar toggle bulunamadı, direkt URL'e gidiliyor: " + e.getMessage());
                driver.get(BASE_URL + "/admin/kategoriler");
                waitForPageLoad();
                Thread.sleep(2000);
            }
            
            // "Kategoriler" linkini bul ve tıkla (sidebar açıksa)
            try {
            WebElement categoriesLink = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".writer-sidebar.open .sidebar-link[href='/admin/kategoriler'], .writer-sidebar.open a[href*='/admin/kategoriler']")
                )
            );
                safeClick(categoriesLink);
            } catch (Exception e) {
                // Link bulunamazsa direkt URL'e git
                driver.get(BASE_URL + "/admin/kategoriler");
            }
            
            // Sayfa yüklemesini bekle
            wait.until(
                ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-dashboard-container")),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-dashboard-title"))
                )
            );
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
            Thread.sleep(2000);
            
            // 3. Yeni kategori ekle butonunu bul ve tıkla
            // Önce açık alert varsa kapat (dashboard'dan gelen alert olabilir)
            for (int i = 0; i < 3; i++) {
                try {
                    org.openqa.selenium.Alert alert = driver.switchTo().alert();
                    String alertText = alert.getText();
                    System.out.println("Case 12d: Alert bulundu: " + alertText);
                    alert.accept();
                    Thread.sleep(1000);
                } catch (Exception e) {
                    // Alert yoksa devam et
                    break;
                }
            }
            
            // CSS selector'da :contains() kullanılamaz, önce CSS selector dene, sonra XPath
            WebElement addButton = null;
            try {
                addButton = wait.until(
                    ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".admin-dashboard-header button.admin-btn-primary")
                    )
                );
            } catch (Exception e1) {
                try {
                    addButton = wait.until(
                        ExpectedConditions.elementToBeClickable(
                            By.cssSelector("button.admin-btn[class*='primary']")
                        )
                    );
                } catch (Exception e2) {
                    // XPath ile dene
                    addButton = wait.until(
                        ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[contains(@class, 'admin-btn-primary') or contains(text(), 'Yeni Kategori')]")
                        )
                    );
                }
            }
            
            // Modal'ı açmak için retry mekanizması ile buton tıklama
            WebElement modalOverlay = null;
            for (int retry = 0; retry < 3; retry++) {
                try {
                    // Loading'in bitmesini bekle
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
                    Thread.sleep(1000);
                    
                    // Butonun görünür olduğundan emin ol
                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", addButton);
                    Thread.sleep(500);
                    
                    // Alert'leri kapat (varsa)
                    for (int i = 0; i < 3; i++) {
                        try {
                            org.openqa.selenium.Alert alert = driver.switchTo().alert();
                            alert.accept();
                            Thread.sleep(500);
                        } catch (Exception e) {
                            break;
                        }
                    }
                    
                    // Butonu tıkla
                    safeClick(addButton);
                    Thread.sleep(1000);
                    
                    // Modal'ın açılmasını bekle
                    modalOverlay = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-modal-overlay")));
                    Thread.sleep(1000);
                    break; // Başarılı, döngüden çık
                } catch (Exception e) {
                    System.out.println("Case 12d: Modal açma denemesi " + (retry + 1) + "/3 başarısız: " + e.getMessage());
                    if (retry < 2) {
                        // Sayfayı yenile ve tekrar dene
                        driver.navigate().refresh();
                        Thread.sleep(3000);
                        waitForPageLoad();
                        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
                        Thread.sleep(2000);
                        
                        // Butonu tekrar bul
                        try {
                            addButton = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                    By.cssSelector(".admin-dashboard-header button.admin-btn-primary, button.admin-btn[class*='primary']")
                                )
                            );
                        } catch (Exception ex) {
                            // XPath ile dene
                            addButton = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                    By.xpath("//button[contains(@class, 'admin-btn-primary') or contains(text(), 'Yeni Kategori')]")
                                )
                            );
                        }
                    } else {
                        throw e; // Son deneme de başarısız, hatayı fırlat
                    }
                }
            }
            
            if (modalOverlay == null) {
                fail("Case 12d: Modal açılamadı (3 deneme sonrası)");
                return;
            }
            
            // 4. Modal'da kategori formunu doldur
            String categoryName = "Test Kategori " + System.currentTimeMillis();
            String categoryDescription = "Bu bir test kategorisidir.";
            
            WebElement nameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".admin-modal input[type='text'], .admin-form-input")
                )
            );
            nameInput.clear();
            nameInput.sendKeys(categoryName);
            
            Thread.sleep(1000);
            
            // Açıklama alanı
            try {
                WebElement descriptionInput = driver.findElement(
                    By.cssSelector(".admin-modal textarea")
                );
                descriptionInput.clear();
                descriptionInput.sendKeys(categoryDescription);
            } catch (Exception e) {
                // Açıklama alanı yoksa devam et
            }
            
            Thread.sleep(1000);
            
            // 5. Oluştur butonuna tıkla
            WebElement saveButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".admin-modal button[type='submit'].admin-btn-primary, .admin-modal button.admin-btn-primary")
                )
            );
            safeClick(saveButton);
            Thread.sleep(2000);
            
            // Alert'i handle et (kategori eklendi mesajı için)
            try {
                org.openqa.selenium.Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                alert.accept();
                System.out.println("Case 12d: Alert kapatıldı");
            } catch (Exception e) {
                // Alert yoksa devam et
            }
            
            Thread.sleep(2000);
            
            // 6. Kategorinin eklendiğini kontrol et (tabloda)
            WebElement categoryRow = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//tr[.//td[contains(text(), '" + categoryName + "')]]")
                )
            );
            
            assertTrue(categoryRow.isDisplayed(),
                "Case 12d: Kategori eklenemedi");
            
            System.out.println("Case 12d: Kategori ekleme başarıyla test edildi");
            
        } catch (Exception e) {
            System.err.println("Case 12d: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 12d: Test başarısız - " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Case 12d Negative: Admin kategori silebilmeli")
    public void case12d_AdminDeleteCategory() {
        try {
            // 1. ADMIN olarak giriş yap
            AdminCredentials adminCreds = ensureAdminUserExists();
            loginUser(adminCreds.getEmail(), adminCreds.getPassword());
            
            // 2. Sidebar'ı aç ve "Kategoriler" linkine tıkla
            try {
                // Header içindeki hamburger menu butonunu bul (daha spesifik)
                WebElement sidebarToggle = wait.until(
                    ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".reader-header button.hamburger-menu, .reader-header-content button.hamburger-menu, button.hamburger-menu[aria-label='Menu']")
                    )
                );
                
                // Butonun görünür olduğundan emin ol
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", sidebarToggle);
                Thread.sleep(500);
                
                // JavaScript ile tıkla (React state güncellemesi için)
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", sidebarToggle);
                Thread.sleep(1000);
                
                // Sidebar'ın açılmasını bekle
                wait.until(
                    ExpectedConditions.and(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(".writer-sidebar.open")),
                        ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".writer-sidebar.open"))
                    )
                );
                Thread.sleep(1000);
            } catch (Exception e) {
                // Sidebar toggle bulunamadıysa direkt URL'e git
                System.out.println("Case 12d Negative: Sidebar toggle bulunamadı, direkt URL'e gidiliyor: " + e.getMessage());
                driver.get(BASE_URL + "/admin/kategoriler");
                waitForPageLoad();
                Thread.sleep(2000);
            }
            
            // "Kategoriler" linkini bul ve tıkla (sidebar açıksa)
            try {
            WebElement categoriesLink = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".writer-sidebar.open .sidebar-link[href='/admin/kategoriler'], .writer-sidebar.open a[href*='/admin/kategoriler']")
                )
            );
                safeClick(categoriesLink);
            } catch (Exception e) {
                // Link bulunamazsa direkt URL'e git
                driver.get(BASE_URL + "/admin/kategoriler");
            }
            
            // Sayfa yüklemesini bekle
            wait.until(
                ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-dashboard-container")),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-dashboard-title"))
                )
            );
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
            Thread.sleep(2000);
            
            // Önce açık alert varsa kapat (dashboard'dan gelen alert olabilir)
            // Alert'i modal açılmadan önce kontrol et
            for (int i = 0; i < 3; i++) {
                try {
                    org.openqa.selenium.Alert alert = driver.switchTo().alert();
                    String alertText = alert.getText();
                    System.out.println("Case 12d Negative: Alert bulundu: " + alertText);
                    alert.accept();
                    Thread.sleep(1000);
                } catch (Exception e) {
                    // Alert yoksa devam et
                    break;
                }
            }
            
            // 3. Önce bir kategori ekle (test için)
            String categoryName = "Silinecek Kategori " + System.currentTimeMillis();
            
            WebElement addButton = null;
            try {
                addButton = wait.until(
                    ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".admin-dashboard-header button.admin-btn-primary")
                    )
                );
            } catch (Exception e1) {
                try {
                    addButton = wait.until(
                        ExpectedConditions.elementToBeClickable(
                            By.cssSelector("button.admin-btn[class*='primary']")
                        )
                    );
                } catch (Exception e2) {
                    // XPath ile dene
                    addButton = wait.until(
                        ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[contains(@class, 'admin-btn-primary') or contains(text(), 'Yeni Kategori')]")
                        )
                    );
                }
            }
            
            // Modal'ı açmak için retry mekanizması ile buton tıklama
            WebElement modalOverlay = null;
            for (int retry = 0; retry < 3; retry++) {
                try {
                    // Loading'in bitmesini bekle
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
                    Thread.sleep(1000);
                    
                    // Butonun görünür olduğundan emin ol
                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", addButton);
                    Thread.sleep(500);
                    
                    // Alert'leri kapat (varsa)
                    for (int i = 0; i < 3; i++) {
                        try {
                            org.openqa.selenium.Alert alert = driver.switchTo().alert();
                            alert.accept();
                            Thread.sleep(500);
                        } catch (Exception e) {
                            break;
                        }
                    }
                    
                    // Butonu tıkla
                    safeClick(addButton);
                    Thread.sleep(1000);
                    
                    // Modal'ın açılmasını bekle
                    modalOverlay = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-modal-overlay")));
                    Thread.sleep(1000);
                    break; // Başarılı, döngüden çık
                } catch (Exception e) {
                    System.out.println("Case 12d Negative: Modal açma denemesi " + (retry + 1) + "/3 başarısız: " + e.getMessage());
                    if (retry < 2) {
                        // Sayfayı yenile ve tekrar dene
                        driver.navigate().refresh();
                        Thread.sleep(3000);
                        waitForPageLoad();
                        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
                        Thread.sleep(2000);
                        
                        // Butonu tekrar bul
                        try {
                            addButton = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                    By.cssSelector(".admin-dashboard-header button.admin-btn-primary, button.admin-btn[class*='primary']")
                                )
                            );
                        } catch (Exception ex) {
                            // XPath ile dene
                            addButton = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                    By.xpath("//button[contains(@class, 'admin-btn-primary') or contains(text(), 'Yeni Kategori')]")
                                )
                            );
                        }
                    } else {
                        throw e; // Son deneme de başarısız, hatayı fırlat
                    }
                }
            }
            
            if (modalOverlay == null) {
                fail("Case 12d Negative: Modal açılamadı (3 deneme sonrası)");
                return;
            }
            
            WebElement nameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".admin-modal input[type='text']")
                )
            );
            nameInput.clear();
            nameInput.sendKeys(categoryName);
            
            Thread.sleep(1000);
            
            WebElement saveButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".admin-modal button[type='submit'].admin-btn-primary, .admin-modal button.admin-btn-primary")
                )
            );
            safeClick(saveButton);
            Thread.sleep(2000);
            
            // Alert'i handle et (kategori eklendi mesajı için)
            try {
                org.openqa.selenium.Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                alert.accept();
                System.out.println("Case 12d Negative: Alert kapatıldı");
            } catch (Exception e) {
                // Alert yoksa devam et
            }
            
            Thread.sleep(2000);
            
            // 4. Kategoriyi bul ve sil (pagination ile arama yap)
            WebElement categoryRow = null;
            int currentPage = 0;
            int maxPages = 50; // Maksimum 50 sayfa kontrol et
            
            while (currentPage < maxPages && categoryRow == null) {
                try {
                    categoryRow = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//tr[.//td[contains(text(), '" + categoryName + "')]]")
                )
            );
                    System.out.println("Kategori bulundu (sayfa " + (currentPage + 1) + "): " + categoryName);
                    break;
                } catch (org.openqa.selenium.TimeoutException e) {
                    // Kategori bu sayfada bulunamadı, sonraki sayfaya geç
                    try {
                        WebElement nextButton = driver.findElement(
                            By.xpath("//div[contains(@class, 'admin-pagination')]//button[contains(text(), 'Sonraki')]")
                        );
                        
                        // Buton disabled mı kontrol et
                        if (nextButton.getAttribute("disabled") != null) {
                            // Son sayfaya ulaşıldı
                            System.out.println("Son sayfaya ulaşıldı, kategori bulunamadı");
                            break;
                        }
                        
                        // Sonraki sayfaya git
                        System.out.println("Sonraki sayfaya geçiliyor... (sayfa " + (currentPage + 2) + ")");
                        safeClick(nextButton);
                        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
                        Thread.sleep(2000);
                        currentPage++;
                    } catch (org.openqa.selenium.NoSuchElementException ex) {
                        // Pagination butonu yok, son sayfadayız
                        System.out.println("Pagination butonu yok, son sayfadayız");
                        break;
                    }
                }
            }
            
            if (categoryRow == null) {
                fail("Case 12d Negative: Kategori bulunamadı: " + categoryName);
                return;
            }
            
            // Sil butonunu bul ve tıkla
            WebElement deleteButton = categoryRow.findElement(
                By.cssSelector("button.admin-btn-danger, button[class*='danger']")
            );
            safeClick(deleteButton);
            
            // Confirm dialog'u kabul et
            Thread.sleep(1000);
            try {
                driver.switchTo().alert().accept();
            } catch (Exception e) {
                // Alert yoksa devam et
            }
            
            // Silme işleminin tamamlanmasını bekle
            Thread.sleep(3000);
            
            // Sayfayı yenile (silme işleminin tamamlandığından emin olmak için)
            driver.navigate().refresh();
            Thread.sleep(3000);
            
            // Kategorinin silindiğini kontrol et (artık listede görünmemeli) - pagination ile tüm sayfaları kontrol et
            boolean categoryFound = false;
            currentPage = 0;
            maxPages = 50;
            
            // İlk sayfaya dön
            driver.navigate().refresh();
            Thread.sleep(3000);
            waitForPageLoad();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
            Thread.sleep(2000);
            
            while (currentPage < maxPages && !categoryFound) {
                try {
                    WebElement foundCategory = driver.findElement(
                    By.xpath("//tr[.//td[contains(text(), '" + categoryName + "')]]")
                );
                    // Kategori bulundu, hala listede görünüyor
                    categoryFound = true;
                    System.out.println("Kategori hala listede görünüyor (sayfa " + (currentPage + 1) + ")");
                    break;
                } catch (org.openqa.selenium.NoSuchElementException e) {
                    // Kategori bu sayfada yok, sonraki sayfaya geç
                    try {
                        WebElement nextButton = driver.findElement(
                            By.xpath("//div[contains(@class, 'admin-pagination')]//button[contains(text(), 'Sonraki')]")
                        );
                        
                        if (nextButton.getAttribute("disabled") != null) {
                            // Son sayfaya ulaşıldı, kategori bulunamadı (silindi)
                            break;
                        }
                        
                        safeClick(nextButton);
                        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
                        Thread.sleep(2000);
                        currentPage++;
                    } catch (org.openqa.selenium.NoSuchElementException ex) {
                        // Pagination butonu yok, son sayfadayız
                        break;
                    }
                }
            }
            
            if (categoryFound) {
                fail("Case 12d Negative: Kategori silinmedi (hala listede görünüyor)");
            } else {
                // Kategori bulunamadı - bu beklenen davranış (silindi)
                assertTrue(true, "Case 12d Negative: Kategori başarıyla silindi");
            }
            
            System.out.println("Case 12d Negative: Kategori silme başarıyla test edildi");
            
        } catch (Exception e) {
            System.err.println("Case 12d Negative: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 12d Negative: Test başarısız - " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Case 12d: Admin kategori düzenleyebilmeli")
    public void case12d_AdminUpdateCategory() {
        try {
            // 1. Admin olarak giriş yap
            AdminCredentials adminCreds = ensureAdminUserExists();
            loginUser(adminCreds.getEmail(), adminCreds.getPassword());
            
            // Admin dashboard'a yönlendirildiğini kontrol et
            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/admin") || currentUrl.contains("/dashboard"),
                "Case 12d: Admin olarak giriş yapılamadı. URL: " + currentUrl
            );
            
            // 2. Kategoriler sayfasına git
            driver.get(BASE_URL + "/admin/kategoriler");
            waitForPageLoad();
            Thread.sleep(3000);
            
            // Sayfa yüklemesini bekle
            wait.until(
                ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-dashboard-container")),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-dashboard-title"))
                )
            );
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
            Thread.sleep(2000);
            
            // Alert'leri kapat (varsa)
            for (int i = 0; i < 3; i++) {
                try {
                    org.openqa.selenium.Alert alert = driver.switchTo().alert();
                    alert.accept();
                    Thread.sleep(500);
                } catch (Exception e) {
                    break;
                }
            }
            
            // 3. Tablodaki ilk kategorinin "Düzenle" butonunu bul ve tıkla
            WebElement categoryRow = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".admin-table tbody tr")
                )
            );
            
            // Düzenle butonunu bul
            WebElement editButton = categoryRow.findElement(
                By.xpath(".//button[contains(text(), 'Düzenle')]")
            );
            
            // Butonun görünür olduğundan emin ol
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", editButton);
            Thread.sleep(500);
            
            // Butonu tıkla
            safeClick(editButton);
            Thread.sleep(2000);
            
            // Modal'ın açılmasını bekle
            wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-modal-overlay"))
            );
            Thread.sleep(1000);
            
            // 4. Modal'da kategori bilgilerini güncelle
            String updatedCategoryName = "Güncellenmiş Kategori " + System.currentTimeMillis();
            
            // Kategori adını güncelle
            WebElement nameInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".admin-modal input[type='text']")
                )
            );
            nameInput.clear();
            nameInput.sendKeys(updatedCategoryName);
            
            Thread.sleep(1000);
            
            // Güncelle butonuna tıkla
            WebElement updateButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".admin-modal button[type='submit'].admin-btn-primary, .admin-modal button.admin-btn-primary")
                )
            );
            safeClick(updateButton);
            Thread.sleep(2000);
            
            // Alert'i handle et (varsa)
            try {
                org.openqa.selenium.Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                alert.accept();
                Thread.sleep(1000);
            } catch (Exception e) {
                // Alert yoksa devam et
            }
            
            // Modal'ın kapanmasını bekle
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-modal-overlay")));
            Thread.sleep(2000);
            
            // 5. Kategorinin güncellendiğini kontrol et
            driver.navigate().refresh();
            Thread.sleep(3000);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
            Thread.sleep(2000);
            
            // Güncellenmiş kategori adını kontrol et
            WebElement updatedCategoryRow = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//tr[.//td[contains(text(), '" + updatedCategoryName + "')]]")
                )
            );
            
            assertTrue(updatedCategoryRow.isDisplayed(),
                "Case 12d: Kategori güncellenemedi");
            
            System.out.println("Case 12d: Kategori düzenleme başarıyla test edildi");
            
        } catch (Exception e) {
            System.err.println("Case 12d: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 12d: Test başarısız - " + e.getMessage());
        }
    }
}

