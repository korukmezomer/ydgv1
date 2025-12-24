# Selenium Test Suite

Bu klasör, uygulamanın frontend kısmını test etmek için Selenium WebDriver testlerini içerir.

## Test Case'ler

### Case 1: Kullanıcı Kaydı (User Registration)
- **Dosya**: `Case1_UserRegistrationTest.java`
- **Use Case**: Yeni kullanıcı sisteme kayıt olabilmeli
- **Senaryo**: 
  - Ana sayfadan kayıt sayfasına git
  - Form alanlarını doldur (ad, soyad, email, kullanıcı adı, şifre)
  - Kayıt butonuna tıkla
  - Başarılı kayıt sonrası dashboard'a yönlendirildiğini doğrula

### Case 2: Kullanıcı Girişi (User Login)
- **Dosya**: `Case2_UserLoginTest.java`
- **Use Case**: Mevcut kullanıcı sisteme giriş yapabilmeli
- **Senaryo**:
  - Ana sayfadan giriş sayfasına git
  - Email ve şifre alanlarını doldur
  - Giriş butonuna tıkla
  - Başarılı giriş sonrası dashboard'a yönlendirildiğini doğrula

### Case 3: Dashboard Erişimi (Dashboard Access)
- **Dosya**: `Case3_DashboardAccessTest.java`
- **Use Case**: Kullanıcı giriş yaptıktan sonra rolüne göre doğru dashboard'a yönlendirilmeli
- **Senaryo**:
  - Kullanıcı giriş yapar
  - Rolüne göre doğru dashboard'a yönlendirildiğini doğrula
  - Dashboard sayfasının yüklendiğini doğrula

### Case 4: Story Oluşturma (Story Creation)
- **Dosya**: `Case4_StoryCreationTest.java`
- **Use Case**: WRITER rolündeki kullanıcı yeni story oluşturabilmeli
- **Senaryo**:
  - WRITER olarak giriş yap
  - Yeni story oluştur sayfasına git
  - Başlık ve içerik gir
  - Story'yi kaydet
  - Story'nin oluşturulduğunu doğrula

### Case 5: Yorum Yapma (Comment Creation)
- **Dosya**: `Case5_CommentTest.java`
- **Use Case**: Kullanıcı bir story'ye yorum yapabilmeli
- **Senaryo**:
  - Kullanıcı giriş yapar
  - Bir story sayfasına gider
  - Yorum alanına yorum yazar
  - Yorum gönder butonuna tıklar
  - Yorumun eklendiğini doğrula

### Case 6: Karar Tablosu - Story Yayınlama (Decision Table - Story Publishing)
- **Dosya**: `Case6_StoryPublishDecisionTableTest.java`
- **Use Case**: Story yayınlama işleminin karar tablosuna göre test edilmesi
- **Karar Tablosu**:
  - **Koşul 1**: Kullanıcı WRITER rolünde mi? (E/H)
  - **Koşul 2**: Story durumu TASLAK mı? (E/H)
  - **Koşul 3**: Story içeriği 100 karakterden uzun mu? (E/H)
  
  **Karar Kuralları**:
  | WRITER | TASLAK | İçerik > 100 | Karar |
  |--------|--------|--------------|-------|
  | E      | E      | E            | Yayınlanabilir (YAYIN_BEKLIYOR) |
  | E      | E      | H            | Yayınlanamaz (İçerik yetersiz) |
  | E      | H      | E            | Zaten yayınlanmış/reddedilmiş |
  | H      | E      | E            | Yayınlanamaz (Yetki yok) |
  | H      | E      | H            | Yayınlanamaz (Yetki + içerik yetersiz) |
  
- **Test Senaryoları**:
  - Case 6.1: Tüm koşullar sağlandığında story yayınlanabilir
  - Case 6.2: İçerik yetersiz olduğunda story yayınlanamaz
  - Case 6.3: USER rolündeki kullanıcı story yayınlayamaz

### Case 7: Story Beğenme (Like Story)
- **Dosya**: `Case7_LikeStoryTest.java`
- **Use Case**: Kullanıcı bir story'yi beğenebilmeli
- **Senaryo**:
  - Kullanıcı giriş yapar
  - Bir story sayfasına gider
  - Beğeni butonuna tıklar
  - Beğeninin eklendiğini doğrula

### Case 8: Story Kaydetme (Save Story)
- **Dosya**: `Case8_SaveStoryTest.java`
- **Use Case**: Kullanıcı bir story'yi kaydedebilmeli
- **Senaryo**:
  - Kullanıcı giriş yapar
  - Bir story sayfasına gider
  - Kaydet butonuna tıklar
  - Story'nin kaydedildiğini doğrula

### Case 9: Profil Görüntüleme (Profile View)
- **Dosya**: `Case9_ProfileViewTest.java`
- **Use Case**: Kullanıcı kendi profilini görüntüleyebilmeli
- **Senaryo**:
  - Kullanıcı giriş yapar
  - Profil sayfasına gider
  - Profil bilgilerinin görüntülendiğini doğrula

### Case 10: Admin Story Onaylama (Admin Story Approval)
- **Dosya**: `Case10_AdminStoryApprovalTest.java`
- **Use Case**: ADMIN rolündeki kullanıcı story'leri onaylayabilmeli
- **Senaryo**:
  - ADMIN olarak giriş yap
  - Admin dashboard'a git
  - Onay bekleyen story'leri görüntüle
  - Story'yi onayla
  - Story'nin onaylandığını doğrula

## Çalıştırma

### Lokal Ortamda
```bash
cd backend
mvn test -Dtest="*Selenium*Test"
```

### CI/CD Ortamında (Jenkins)
```bash
mvn test -Dtest="*Selenium*Test" -Dselenium.headless=true
```

## Notlar

- Testler frontend'in `http://localhost:5173` adresinde çalıştığını varsayar
- CI/CD ortamında headless mod otomatik olarak etkinleştirilir
- Her test case bağımsız çalışır ve kendi test verilerini oluşturur
- Karar tablosu testi (Case 6) farklı koşul kombinasyonlarını test eder
