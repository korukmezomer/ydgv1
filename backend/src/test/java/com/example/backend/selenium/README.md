# Selenium Test Suite

Bu klasör, uygulamanın frontend kısmını test etmek için Selenium WebDriver testlerini içerir.

## Test Case'ler

### Test Case 1: Kullanıcı Kaydı (User Registration)
- **Dosya**: `TestCase1_UserRegistrationTest.java`
- **Açıklama**: Yeni kullanıcı kaydı işlemini test eder
- **Senaryo**: 
  - Ana sayfadan kayıt sayfasına gider
  - Form alanlarını doldurur (ad, soyad, email, kullanıcı adı, şifre, rol)
  - Kayıt butonuna tıklar
  - Başarılı kayıt sonrası dashboard'a yönlendirildiğini doğrular

### Test Case 2: Kullanıcı Girişi (User Login)
- **Dosya**: `TestCase2_UserLoginTest.java`
- **Açıklama**: Mevcut kullanıcı girişi işlemini test eder
- **Senaryo**:
  - Ana sayfadan giriş sayfasına gider
  - Email ve şifre alanlarını doldurur
  - Giriş butonuna tıklar
  - Başarılı giriş sonrası dashboard'a yönlendirildiğini doğrular
  - Yanlış şifre ile giriş yapılamadığını test eder

### Test Case 3: Dashboard Erişimi (Dashboard Access)
- **Dosya**: `TestCase3_DashboardAccessTest.java`
- **Açıklama**: Giriş sonrası dashboard erişimini test eder
- **Senaryo**:
  - Kullanıcı giriş yapar
  - Rolüne göre doğru dashboard'a yönlendirildiğini doğrular
  - Ana sayfadaki Dashboard butonunun çalıştığını test eder

### Test Case 4: İçerik Oluşturma (Content Creation)
- **Dosya**: `TestCase4_ContentCreationTest.java`
- **Açıklama**: Yazar olarak yeni içerik oluşturma işlemini test eder
- **Senaryo**:
  - Yazar olarak giriş yapar
  - "Yaz" butonuna tıklar veya `/reader/new-story` sayfasına gider
  - Başlık alanına başlık girer
  - İçerik blokları ekler (metin, başlık, kod, resim vb.)
  - İçeriğin kaydedildiğini doğrular

### Test Case 5: Profil Görüntüleme (Profile View)
- **Dosya**: `TestCase5_ProfileViewTest.java`
- **Açıklama**: Kullanıcı profil sayfasına erişimi test eder
- **Senaryo**:
  - Kullanıcı giriş yapar
  - Profil sayfasına gider
  - Profil bilgilerinin görüntülendiğini doğrular
  - Profil sayfasından dashboard'a geri dönebildiğini test eder

## Gereksinimler

### Yazılım Gereksinimleri
- Java 17 veya üzeri
- Maven 3.6+
- Chrome Browser (ChromeDriver otomatik olarak WebDriverManager tarafından yönetilir)

### Maven Bağımlılıkları
Testler için gerekli bağımlılıklar `pom.xml` dosyasına eklenmiştir:
- Selenium Java (4.15.0)
- WebDriverManager (5.6.2)
- JUnit Jupiter

## Test Çalıştırma

### Tüm Testleri Çalıştırma
```bash
cd backend
mvn test
```

### Belirli Bir Test Sınıfını Çalıştırma
```bash
cd backend
mvn test -Dtest=TestCase1_UserRegistrationTest
```

### IDE'den Çalıştırma
- IntelliJ IDEA veya Eclipse'de test sınıflarını açın
- Test metodlarının yanındaki "Run" butonuna tıklayın
- Veya sınıf seviyesinde "Run" butonuna tıklayarak tüm testleri çalıştırın

## Test Öncesi Hazırlık

### 1. Backend ve Frontend Servislerinin Çalışır Durumda Olması
```bash
# Backend'i başlat (port 8080)
cd backend
mvn spring-boot:run

# Frontend'i başlat (port 5173)
cd frontend
npm run dev
```

### 2. Test Kullanıcılarının Oluşturulması
Testlerin çalışması için aşağıdaki kullanıcıların veritabanında mevcut olması gerekir:

**Test Kullanıcısı (USER rolü):**
- Email: `test@example.com`
- Şifre: `Test123456`

**Yazar Kullanıcısı (WRITER rolü):**
- Email: `writer@example.com`
- Şifre: `Test123456`

Bu kullanıcıları manuel olarak oluşturabilir veya kayıt testini çalıştırarak otomatik oluşturabilirsiniz.

### 3. ChromeDriver
WebDriverManager otomatik olarak ChromeDriver'ı indirir ve yönetir. Manuel kurulum gerekmez.

## Test Yapılandırması

### Base URL Değiştirme
`BaseSeleniumTest.java` dosyasında `BASE_URL` değişkenini değiştirerek farklı bir URL kullanabilirsiniz:
```java
protected static final String BASE_URL = "http://localhost:5173";
```

### Timeout Ayarları
`DEFAULT_TIMEOUT` değişkenini değiştirerek bekleme sürelerini ayarlayabilirsiniz:
```java
protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
```

## Sorun Giderme

### ChromeDriver Bulunamıyor
- WebDriverManager otomatik olarak ChromeDriver'ı yönetir
- İnternet bağlantınızın olduğundan emin olun
- Chrome browser'ın yüklü olduğundan emin olun

### Element Bulunamıyor
- Frontend'in çalışır durumda olduğundan emin olun
- Sayfanın tamamen yüklendiğinden emin olun (waitForPageLoad() kullanılır)
- Selector'ların doğru olduğundan emin olun

### Test Başarısız Oluyor
- Backend ve frontend servislerinin çalıştığından emin olun
- Test kullanıcılarının veritabanında mevcut olduğundan emin olun
- Console loglarını kontrol edin
- Test sırasında browser'ı açık tutarak ne olduğunu gözlemleyebilirsiniz

## Notlar

- Testler gerçek bir browser (Chrome) kullanır
- Testler sırasında browser penceresi açılır ve kapanır
- Testler sıralı çalıştırılabilir veya paralel çalıştırılabilir (JUnit 5 varsayılan olarak paralel çalıştırmayı destekler)
- Her test bağımsızdır ve kendi setUp/tearDown metodlarına sahiptir

