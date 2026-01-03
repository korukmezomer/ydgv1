package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 12a: Admin Kullanıcı Yönetimi (Admin User Management)
 * 
 * Test Senaryoları:
 * 1. Admin kullanıcı listeleme
 * 2. Admin kullanıcı arama
 * 3. Admin kullanıcı aktif/pasif yapma
 * 4. Admin kullanıcı silme
 */
@DisplayName("Case 12a: Admin Kullanıcı Yönetimi")
public class Case12a_AdminUserManagementTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 12a: Admin kullanıcı aktif/pasif yapabilmeli")
    public void case12a_AdminToggleUserActive() {
        try {
            // 1. Önce test kullanıcısı oluştur
            String testUserEmail = "testuser_" + System.currentTimeMillis() + "@example.com";
            String testUserUsername = "testuser_" + System.currentTimeMillis();
            
            boolean testUserRegistered = registerUser("Test", "User", testUserEmail, testUserUsername, "Test123456");
            if (!testUserRegistered) {
                fail("Case 12a: Test kullanıcısı kaydı başarısız");
                return;
            }
            
            // Veritabanı transaction'ının commit olması için bekle
            Thread.sleep(2000);
            
            // Logout yap
            try {
                driver.get(BASE_URL + "/logout");
                Thread.sleep(2000);
            } catch (Exception e) {
                // Logout sayfası yoksa devam et
            }
            
            // 2. Admin olarak giriş yap
            AdminCredentials adminCreds = ensureAdminUserExists();
            loginUser(adminCreds.getEmail(), adminCreds.getPassword());
            
            // Admin dashboard'a yönlendirildiğini kontrol et
            String currentUrl = driver.getCurrentUrl();
            assertTrue(
                currentUrl.contains("/admin") || currentUrl.contains("/dashboard"),
                "Case 12a: Admin olarak giriş yapılamadı. URL: " + currentUrl
            );
            
            // 3. Direkt kullanıcılar sayfasına git
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
            Thread.sleep(2000);
            
            // 4. Sayfada kullanıcıyı bul (pagination ile)
            System.out.println("Kullanıcı sayfada aranıyor: " + testUserUsername);
            
            WebElement userElement = null;
            int currentPage = 0;
            int maxPages = 50; // Maksimum 50 sayfa kontrol et (daha fazla sayfa olabilir)
            
            while (currentPage < maxPages && userElement == null) {
                try {
                    // Önce sayfanın yüklendiğinden emin ol
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
                    Thread.sleep(1000);
                    
                    // Kullanıcıyı bu sayfada bulmayı dene (daha esnek XPath)
                    try {
                        userElement = driver.findElement(
                            By.xpath("//table//tr//td[contains(text(), '" + testUserUsername + "')]")
                    );
                        if (userElement.isDisplayed()) {
                    System.out.println("Kullanıcı bulundu (sayfa " + (currentPage + 1) + "): " + testUserUsername);
                    break;
                        }
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        // Kullanıcı bu sayfada yok, devam et
                    }
                    
                    // Kullanıcı bu sayfada bulunamadı, sonraki sayfaya geç
                    try {
                        WebElement nextButton = driver.findElement(
                            By.xpath("//div[contains(@class, 'admin-pagination')]//button[contains(text(), 'Sonraki')]")
                        );
                        
                        // Buton disabled mı veya görünür değil mi kontrol et
                        if (nextButton.getAttribute("disabled") != null || !nextButton.isDisplayed() || !nextButton.isEnabled()) {
                            // Son sayfaya ulaşıldı
                            System.out.println("Son sayfaya ulaşıldı (sayfa " + (currentPage + 1) + "), kullanıcı bulunamadı");
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
                        System.out.println("Pagination butonu yok, son sayfadayız (sayfa " + (currentPage + 1) + ")");
                        break;
                    }
                } catch (org.openqa.selenium.TimeoutException e) {
                    // Sayfa yüklenemedi, sonraki sayfaya geçmeyi dene
                    System.out.println("Sayfa yükleme timeout (sayfa " + (currentPage + 1) + "), sonraki sayfaya geçiliyor...");
                    try {
                        WebElement nextButton = driver.findElement(
                            By.xpath("//div[contains(@class, 'admin-pagination')]//button[contains(text(), 'Sonraki')]")
                        );
                        if (nextButton.isEnabled() && nextButton.isDisplayed()) {
                            safeClick(nextButton);
                            Thread.sleep(2000);
                            currentPage++;
                        } else {
                            break;
                        }
                    } catch (Exception ex) {
                        break;
                    }
                }
            }
            
            if (userElement == null) {
                fail("Case 12a: Test kullanıcısı listede bulunamadı (kullanıcı adı): " + testUserUsername + " (toplam " + (currentPage + 1) + " sayfa kontrol edildi)");
                return;
            }
            
            // 6. Kullanıcı satırını bul
            WebElement userRow = userElement.findElement(By.xpath("./ancestor::tr"));
            
            // 7. Aktif/pasif butonunu bul ve tıkla
            WebElement toggleButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    userRow.findElement(By.xpath(".//button[contains(text(), 'Pasif Yap') or contains(text(), 'Aktif Yap')]"))
                )
            );
            
            String buttonTextBefore = toggleButton.getText();
            System.out.println("Buton metni (öncesi): " + buttonTextBefore);
            
            safeClick(toggleButton);
            
            // Confirm dialog'u kabul et (window.confirm)
            Thread.sleep(2000);
            try {
                org.openqa.selenium.Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                alert.accept();
                System.out.println("Confirm dialog kabul edildi");
            } catch (Exception e) {
                // Alert yoksa devam et
                System.out.println("Alert bulunamadı, devam ediliyor");
            }
            
            // API çağrısının tamamlanmasını bekle
            Thread.sleep(5000);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
            Thread.sleep(3000);
            
            // 8. Durumun değiştiğini kontrol et - sayfayı yenile ve tekrar bul
            driver.navigate().refresh();
            Thread.sleep(5000);
            waitForPageLoad();
            
            wait.until(
                ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-dashboard-container")),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-table"))
                )
            );
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
            Thread.sleep(2000);
            
            // Kullanıcıyı tekrar bul (pagination ile)
            userRow = null;
            currentPage = 0;
            maxPages = 10;
            
            while (currentPage < maxPages && userRow == null) {
                try {
                    userRow = wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//table//tr[.//td[contains(text(), '" + testUserUsername + "')]]")
                        )
                    );
                    System.out.println("Kullanıcı tekrar bulundu (sayfa " + (currentPage + 1) + ")");
                    break;
                } catch (org.openqa.selenium.TimeoutException e) {
                    try {
                        WebElement nextButton = driver.findElement(
                            By.xpath("//div[contains(@class, 'admin-pagination')]//button[contains(text(), 'Sonraki')]")
                        );
                        if (nextButton.getAttribute("disabled") != null) {
                            break;
                        }
                        safeClick(nextButton);
                        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
                        Thread.sleep(2000);
                        currentPage++;
                    } catch (Exception ex) {
                        break;
                    }
                }
            }
            
            if (userRow == null) {
                fail("Case 12a: Kullanıcı durum kontrolü için bulunamadı: " + testUserUsername);
                return;
            }
            
            WebElement toggleButtonAfter = wait.until(
                ExpectedConditions.elementToBeClickable(
                    userRow.findElement(By.xpath(".//button[contains(text(), 'Pasif Yap') or contains(text(), 'Aktif Yap')]"))
                )
            );
            
            String buttonTextAfter = toggleButtonAfter.getText();
            System.out.println("Buton metni (sonrası): " + buttonTextAfter);
            
            assertNotEquals(buttonTextBefore, buttonTextAfter,
                "Case 12a: Kullanıcı aktif/pasif durumu değişmedi");
            
            System.out.println("Case 12a: Kullanıcı aktif/pasif yapma başarıyla test edildi");
            
        } catch (Exception e) {
            System.err.println("Case 12a: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 12a: Test başarısız - " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Case 12a Negative: Admin kullanıcı silebilmeli")
    public void case12a_AdminDeleteUser() {
        try {
            // 1. Önce test kullanıcısı oluştur
            String testUserEmail = "testuser_delete_" + System.currentTimeMillis() + "@example.com";
            String testUserUsername = "testuser_delete_" + System.currentTimeMillis();
            
            boolean testUserRegistered = registerUser("Test", "Delete", testUserEmail, testUserUsername, "Test123456");
            if (!testUserRegistered) {
                fail("Case 12a Negative: Test kullanıcısı kaydı başarısız");
                return;
            }
            
            // Veritabanı transaction'ının commit olması için bekle
            Thread.sleep(2000);
            
            // Logout yap
            try {
                driver.get(BASE_URL + "/logout");
                Thread.sleep(2000);
            } catch (Exception e) {
                // Logout sayfası yoksa devam et
            }
            
            // 2. Admin olarak giriş yap
            AdminCredentials adminCreds = ensureAdminUserExists();
            loginUser(adminCreds.getEmail(), adminCreds.getPassword());
            
            // 3. Direkt kullanıcılar sayfasına git
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
            Thread.sleep(2000);
            
            // 4. Sayfada kullanıcıyı bul (pagination ile)
            System.out.println("Kullanıcı sayfada aranıyor: " + testUserUsername);
            
            WebElement userElement = null;
            int currentPage = 0;
            int maxPages = 10; // Maksimum 10 sayfa kontrol et
            
            while (currentPage < maxPages && userElement == null) {
                try {
                    // Kullanıcıyı bu sayfada bulmayı dene
                    userElement = wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//table//tr//td[contains(text(), '" + testUserUsername + "')]")
                        )
                    );
                    System.out.println("Kullanıcı bulundu (sayfa " + (currentPage + 1) + "): " + testUserUsername);
                    break;
                } catch (org.openqa.selenium.TimeoutException e) {
                    // Kullanıcı bu sayfada bulunamadı, sonraki sayfaya geç
                    try {
                        WebElement nextButton = driver.findElement(
                            By.xpath("//div[contains(@class, 'admin-pagination')]//button[contains(text(), 'Sonraki')]")
                        );
                        
                        // Buton disabled mı kontrol et
                        if (nextButton.getAttribute("disabled") != null) {
                            // Son sayfaya ulaşıldı
                            System.out.println("Son sayfaya ulaşıldı, kullanıcı bulunamadı");
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
            
            if (userElement == null) {
                fail("Case 12a Negative: Test kullanıcısı listede bulunamadı (kullanıcı adı): " + testUserUsername);
                return;
            }
            
            // 6. Kullanıcı satırını bul
            WebElement userRow = userElement.findElement(By.xpath("./ancestor::tr"));
            
            // 7. Sil butonunu bul ve tıkla
            WebElement deleteButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    userRow.findElement(By.xpath(".//button[contains(text(), 'Sil')]"))
                )
            );
            
            System.out.println("Sil butonu bulundu, tıklanıyor...");
            safeClick(deleteButton);
            
            // Confirm dialog'u kabul et (window.confirm)
            Thread.sleep(2000);
            try {
                org.openqa.selenium.Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                alert.accept();
                System.out.println("Confirm dialog kabul edildi");
            } catch (Exception e) {
                // Alert yoksa devam et
                System.out.println("Alert bulunamadı, devam ediliyor");
            }
            
            // API çağrısının tamamlanmasını bekle
            Thread.sleep(5000);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
            Thread.sleep(3000);
            
            // 8. Kullanıcının silindiğini kontrol et - sayfayı yenile ve tüm sayfalarda ara
            System.out.println("Kullanıcının silindiği kontrol ediliyor...");
            
            driver.navigate().refresh();
            Thread.sleep(5000);
            waitForPageLoad();
            
            wait.until(
                ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-dashboard-container")),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".admin-table"))
                )
            );
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
            Thread.sleep(3000);
            
            // Tüm sayfalarda kullanıcıyı ara
            boolean userFound = false;
            boolean foundUserIsActive = false;
            currentPage = 0;
            maxPages = 10;
            
            while (currentPage < maxPages && !userFound) {
                try {
                    WebElement userCell = driver.findElement(
                        By.xpath("//table//tr//td[contains(text(), '" + testUserUsername + "')]")
                    );
                    userFound = true;
                    System.out.println("Kullanıcı hala listede görünüyor (sayfa " + (currentPage + 1) + ")");
                    
                    // Kullanıcının aktif durumunu kontrol et
                    WebElement foundUserRow = userCell.findElement(By.xpath("./ancestor::tr"));
                    try {
                        WebElement activeCell = foundUserRow.findElement(By.xpath(".//td[4]")); // 4. sütun "Aktif" sütunu
                        String activeText = activeCell.getText();
                        foundUserIsActive = activeText.contains("Evet");
                        System.out.println("Kullanıcı aktif durumu: " + activeText);
                    } catch (Exception e) {
                        System.out.println("Aktif durumu kontrol edilemedi");
                    }
                    break;
                } catch (org.openqa.selenium.NoSuchElementException e) {
                    // Kullanıcı bu sayfada yok, sonraki sayfaya geç
                    try {
                        WebElement nextButton = driver.findElement(
                            By.xpath("//div[contains(@class, 'admin-pagination')]//button[contains(text(), 'Sonraki')]")
                        );
                        if (nextButton.getAttribute("disabled") != null) {
                            break;
                        }
                        safeClick(nextButton);
                        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".admin-loading")));
                        Thread.sleep(2000);
                        currentPage++;
                    } catch (Exception ex) {
                        break;
                    }
                }
            }
            
            // Soft delete olduğu için kullanıcı listede görünebilir ama isActive = false olmalı
            if (userFound && foundUserIsActive) {
                fail("Case 12a Negative: Kullanıcı silinmedi (hala aktif durumda)");
                return;
            } else if (userFound && !foundUserIsActive) {
                System.out.println("Case 12a Negative: Kullanıcı soft delete yapıldı (isActive = false)");
            } else {
                System.out.println("Case 12a Negative: Kullanıcı listede bulunamadı (silindi)");
            }
            
            // 9. API kontrolü - soft delete olduğu için isActive = false olmalı
            Thread.sleep(500);
            Long deletedUserId = getUserIdByEmail(testUserEmail);
            Boolean isActive = deletedUserId != null ? getUserActiveStatusViaApi(deletedUserId) : null;
            if (deletedUserId == null || isActive == null) {
                fail("Case 12a Negative: Kullanıcı API'den doğrulanamadı (ID veya aktif durumu yok)");
                } else {
                assertFalse(isActive, "Case 12a Negative: Kullanıcı hala aktif (silinmedi, isActive = true)");
            }
            
            System.out.println("Case 12a Negative: Kullanıcı silme başarıyla test edildi");
            
        } catch (Exception e) {
            System.err.println("Case 12a Negative: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 12a Negative: Test başarısız - " + e.getMessage());
        }
    }
}
