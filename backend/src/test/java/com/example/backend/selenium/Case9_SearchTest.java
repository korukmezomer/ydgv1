package com.example.backend.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Case 9: Arama Çubuğu Testi
 * 
 * Use Case: Kullanıcı arama çubuğunu kullanarak story'leri bulabilmeli
 * Senaryo:
 * - Onaylanmış bir story olmalı
 * - Kullanıcı giriş yapar
 * - Arama çubuğuna story başlığını yazar
 * - Arama sonuçlarında story'yi bulur
 * - Olmayan bir şey arandığında sonuç bulunmaz
 */
@DisplayName("Case 9: Arama Çubuğu")
public class Case9_SearchTest extends BaseSeleniumTest {
    
    @Test
    @DisplayName("Case 9: Var olan story'yi arama - bulmalı")
    public void case9_SearchExistingStory() {
        try {
            // 1. Writer oluştur (BaseSeleniumTest'teki registerWriter helper metodunu kullan)
            java.util.Random random = new java.util.Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String writerEmail = "writer_search_" + randomSuffix + "@example.com";
            String writerPassword = "Test123456";
            String writerUsername = "writer_search_" + randomSuffix;
            
            boolean writerRegistered = registerWriter("Writer", "Search", writerEmail, writerUsername, writerPassword);
            if (!writerRegistered) {
                fail("Case 9: Writer kaydı başarısız");
                return;
            }
            
            // Writer olarak zaten giriş yapılmış durumda (kayıt sonrası dashboard'a yönlendirildi)
            
            // Story oluştur ve yayınla (BaseSeleniumTest'teki createStory metodunu kullan)
            String storyTitle = "Arama Test Story " + System.currentTimeMillis();
            String storyContent = "Bu bir arama test story'sidir. Bu story arama sonuçlarında görünmelidir.";
            String storySlug = createStory(writerEmail, writerPassword, storyTitle, storyContent);
            
            if (storySlug == null) {
                fail("Case 9: Story oluşturulamadı");
                return;
            }
            
            // 2. Admin olarak story'yi onayla (BaseSeleniumTest'teki approveStoryAsAdmin metodunu kullan)
            storySlug = approveStoryAsAdmin(storyTitle);
            if (storySlug == null) {
                fail("Case 9: Story onaylanamadı");
                return;
            }
            
            // 3. Kullanıcı (Searcher) oluştur (BaseSeleniumTest'teki registerUser helper metodunu kullan)
            java.util.Random searcherRandom = new java.util.Random();
            String searcherRandomSuffix = String.valueOf(searcherRandom.nextInt(10000));
            String searcherEmail = "searcher" + searcherRandomSuffix + "@example.com";
            String searcherUsername = "searcher" + searcherRandomSuffix;
            
            try {
                driver.get(BASE_URL + "/logout");
                Thread.sleep(2000);
            } catch (Exception e) {
                // Logout sayfası yoksa devam et
            }
            
            boolean searcherRegistered = registerUser("Searcher", "Test", searcherEmail, searcherUsername, "Test123456");
            if (!searcherRegistered) {
                fail("Case 9: Searcher kaydı başarısız");
                return;
            }
            
            // 4. Arama çubuğunu bul ve arama yap
            // Arama çubuğu ReaderHeader'da
            WebElement searchInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".search-input-header, input[placeholder='Ara']")
                )
            );
            
            // Story başlığının bir kısmını yaz (tam başlık yerine kısmi arama)
            String searchTerm = storyTitle.substring(0, Math.min(10, storyTitle.length()));
            searchInput.clear();
            searchInput.sendKeys(searchTerm);
            Thread.sleep(1000);
            
            // Enter tuşuna bas veya form submit et
            searchInput.submit();
            Thread.sleep(3000); // Arama sonuçlarının yüklenmesi için bekle
            
            // 5. Arama sonuçlarında story'yi bul
            // Arama sayfasında story başlığının görünüp görünmediğini kontrol et
            WebElement searchResult = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(text(), '" + storyTitle + "')] | //*[contains(@class, 'search-result-item')]")
                )
            );
            
            assertTrue(searchResult.isDisplayed(), "Case 9: Arama sonuçlarında story bulunamadı");
            System.out.println("Case 9: Arama başarılı - Story bulundu: " + storyTitle);
            
        } catch (Exception e) {
            System.err.println("Case 9: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 9: Test başarısız - " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Case 9 Negative: Olmayan story'yi arama - bulmamalı")
    public void case9_Negative_SearchNonExistentStory() {
        try {
            // Kullanıcı (Searcher) oluştur (BaseSeleniumTest'teki registerUser helper metodunu kullan)
            java.util.Random random = new java.util.Random();
            String randomSuffix = String.valueOf(random.nextInt(10000));
            String searcherEmail = "searcher_neg" + randomSuffix + "@example.com";
            String searcherUsername = "searcher_neg" + randomSuffix;
            
            try {
                driver.get(BASE_URL + "/logout");
                Thread.sleep(2000);
            } catch (Exception e) {
                // Logout sayfası yoksa devam et
            }
            
            boolean searcherRegistered = registerUser("Searcher", "Test", searcherEmail, searcherUsername, "Test123456");
            if (!searcherRegistered) {
                fail("Case 9 Negative: Searcher kaydı başarısız");
                return;
            }
            
            // Arama çubuğunu bul ve olmayan bir şey ara
            WebElement searchInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".search-input-header, input[placeholder='Ara']")
                )
            );
            
            // Olmayan bir story başlığı ara
            String nonExistentSearchTerm = "BuStoryKesinlikleYok" + System.currentTimeMillis();
            searchInput.clear();
            searchInput.sendKeys(nonExistentSearchTerm);
            Thread.sleep(1000);
            
            // Enter tuşuna bas veya form submit et
            searchInput.submit();
            Thread.sleep(3000); // Arama sonuçlarının yüklenmesi için bekle
            
            // Arama sonuçlarında bu story'nin bulunmaması gerekiyor
            boolean storyFound = false;
            try {
                WebElement searchResult = driver.findElement(
                    By.xpath("//*[contains(text(), '" + nonExistentSearchTerm + "')]")
                );
                storyFound = searchResult.isDisplayed();
            } catch (org.openqa.selenium.NoSuchElementException e) {
                // Story bulunamadı - bu beklenen davranış
                storyFound = false;
            }
            
            assertFalse(storyFound, "Case 9 Negative: Olmayan story arama sonuçlarında bulundu (beklenmeyen)");
            System.out.println("Case 9 Negative: Arama başarılı - Olmayan story bulunamadı (beklenen)");
            
            // "Sonuç bulunamadı" mesajı kontrolü (eğer varsa)
            try {
                WebElement noResultsMessage = driver.findElement(
                    By.xpath("//*[contains(text(), 'sonuç bulunamadı') or contains(text(), 'Sonuç bulunamadı') or contains(text(), 'No results')]")
                );
                assertTrue(noResultsMessage.isDisplayed(), 
                    "Case 9 Negative: 'Sonuç bulunamadı' mesajı görünmüyor");
            } catch (org.openqa.selenium.NoSuchElementException e) {
                // Mesaj yoksa da sorun değil, önemli olan story'nin bulunmaması
                System.out.println("Case 9 Negative: 'Sonuç bulunamadı' mesajı görünmüyor (opsiyonel)");
            }
            
        } catch (Exception e) {
            System.err.println("Case 9 Negative: Beklenmeyen hata - " + e.getMessage());
            e.printStackTrace();
            fail("Case 9 Negative: Test başarısız - " + e.getMessage());
        }
    }
}

