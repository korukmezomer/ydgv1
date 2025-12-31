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
                WebElement sidebarToggle = wait.until(
                    ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[aria-label*='menu'], .menu-toggle, .sidebar-toggle, button[class*='menu']")
                    )
                );
                sidebarToggle.click();
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Case 12d: Sidebar toggle bulunamadı, direkt linke tıklanacak");
            }
            
            // "Kategoriler" linkini bul ve tıkla
            WebElement categoriesLink = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@href, '/admin/kategoriler')] | //a[contains(text(), 'Kategoriler')]")
                )
            );
            categoriesLink.click();
            waitForPageLoad();
            Thread.sleep(3000);
            
            // 3. Yeni kategori ekle butonunu bul ve tıkla
            WebElement addButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Yeni Kategori')]")
                )
            );
            addButton.click();
            Thread.sleep(2000);
            
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
                    By.xpath("//div[contains(@class, 'admin-modal')]//button[contains(text(), 'Oluştur')]")
                )
            );
            saveButton.click();
            Thread.sleep(3000);
            
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
                WebElement sidebarToggle = wait.until(
                    ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[aria-label*='menu'], .menu-toggle, .sidebar-toggle, button[class*='menu']")
                    )
                );
                sidebarToggle.click();
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Case 12d Negative: Sidebar toggle bulunamadı");
            }
            
            WebElement categoriesLink = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@href, '/admin/kategoriler')] | //a[contains(text(), 'Kategoriler')]")
                )
            );
            categoriesLink.click();
            waitForPageLoad();
            Thread.sleep(3000);
            
            // 3. Önce bir kategori ekle (test için)
            String categoryName = "Silinecek Kategori " + System.currentTimeMillis();
            
            WebElement addButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Yeni Kategori')]")
                )
            );
            addButton.click();
            Thread.sleep(2000);
            
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
                    By.xpath("//div[contains(@class, 'admin-modal')]//button[contains(text(), 'Oluştur')]")
                )
            );
            saveButton.click();
            Thread.sleep(3000);
            
            // 4. Kategoriyi bul ve sil
            WebElement categoryRow = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//tr[.//td[contains(text(), '" + categoryName + "')]]")
                )
            );
            
            // Sil butonunu bul ve tıkla
            WebElement deleteButton = categoryRow.findElement(
                By.xpath(".//button[contains(text(), 'Sil')]")
            );
            deleteButton.click();
            
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
            
            // Kategorinin silindiğini kontrol et (artık listede görünmemeli)
            try {
                driver.findElement(
                    By.xpath("//tr[.//td[contains(text(), '" + categoryName + "')]]")
                );
                // Hala görünüyorsa test başarısız
                fail("Case 12d Negative: Kategori silinmedi (hala listede görünüyor)");
            } catch (org.openqa.selenium.NoSuchElementException e) {
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
            // 1. ADMIN olarak giriş yap
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
                WebElement sidebarToggle = wait.until(
                    ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[aria-label*='menu'], .menu-toggle, .sidebar-toggle, button[class*='menu']")
                    )
                );
                sidebarToggle.click();
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Case 12d: Sidebar toggle bulunamadı");
            }
            
            WebElement categoriesLink = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@href, '/admin/kategoriler')] | //a[contains(text(), 'Kategoriler')]")
                )
            );
            categoriesLink.click();
            waitForPageLoad();
            Thread.sleep(3000);
            
            // 3. Önce bir kategori ekle (test için)
            String originalCategoryName = "Düzenlenecek Kategori " + System.currentTimeMillis();
            
            WebElement addButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Yeni Kategori')]")
                )
            );
            addButton.click();
            Thread.sleep(2000);
            
            WebElement nameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".admin-modal input[type='text']")
                )
            );
            nameInput.clear();
            nameInput.sendKeys(originalCategoryName);
            
            Thread.sleep(1000);
            
            WebElement saveButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@class, 'admin-modal')]//button[contains(text(), 'Oluştur')]")
                )
            );
            saveButton.click();
            Thread.sleep(3000);
            
            // 4. Kategoriyi bul ve düzenle
            WebElement categoryRow = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//tr[.//td[contains(text(), '" + originalCategoryName + "')]]")
                )
            );
            
            // Düzenle butonunu bul ve tıkla
            WebElement editButton = categoryRow.findElement(
                By.xpath(".//button[contains(text(), 'Düzenle')]")
            );
            editButton.click();
            Thread.sleep(2000);
            
            // 5. Modal'da kategori bilgilerini güncelle
            String updatedCategoryName = "Güncellenmiş Kategori " + System.currentTimeMillis();
            String updatedDescription = "Bu güncellenmiş bir kategoridir.";
            
            // Kategori adını güncelle
            WebElement editNameInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".admin-modal input[type='text']")
                )
            );
            editNameInput.clear();
            editNameInput.sendKeys(updatedCategoryName);
            
            Thread.sleep(1000);
            
            // Açıklamayı güncelle
            try {
                WebElement editDescriptionInput = driver.findElement(
                    By.cssSelector(".admin-modal textarea")
                );
                editDescriptionInput.clear();
                editDescriptionInput.sendKeys(updatedDescription);
            } catch (Exception e) {
                // Açıklama alanı yoksa devam et
            }
            
            Thread.sleep(1000);
            
            // 6. Güncelle butonuna tıkla
            WebElement updateButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@class, 'admin-modal')]//button[contains(text(), 'Güncelle')]")
                )
            );
            updateButton.click();
            Thread.sleep(3000);
            
            // 7. Kategorinin güncellendiğini kontrol et
            driver.navigate().refresh();
            Thread.sleep(3000);
            
            // Güncellenmiş kategori adını kontrol et
            WebElement updatedCategoryRow = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//tr[.//td[contains(text(), '" + updatedCategoryName + "')]]")
                )
            );
            
            assertTrue(updatedCategoryRow.isDisplayed(),
                "Case 12d: Kategori güncellenemedi");
            
            // Eski kategori adının artık görünmediğini kontrol et
            try {
                driver.findElement(
                    By.xpath("//tr[.//td[contains(text(), '" + originalCategoryName + "')]]")
                );
                fail("Case 12d: Eski kategori adı hala görünüyor");
            } catch (org.openqa.selenium.NoSuchElementException e) {
                // Eski kategori adı bulunamadı - bu beklenen davranış
                assertTrue(true, "Case 12d: Kategori başarıyla güncellendi");
            }
            
            System.out.println("Case 12d: Kategori düzenleme başarıyla test edildi");
            
        } catch (Exception e) {
            System.err.println("Case 12d: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 12d: Test başarısız - " + e.getMessage());
        }
    }
}

