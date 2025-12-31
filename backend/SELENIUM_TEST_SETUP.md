# Selenium Testleri Çalıştırma Kılavuzu

## Gereksinimler

1. **PostgreSQL çalışıyor olmalı**
2. **Test veritabanı oluşturulmuş olmalı**
3. **Frontend ve Backend çalışıyor olmalı**

## Test Veritabanı Oluşturma

PostgreSQL'de test veritabanını oluşturun:

```sql
CREATE DATABASE yazilimdogrulama_test;
```

## Backend'i Test Veritabanı ile Çalıştırma

### Seçenek 1: Test Profili ile (Önerilen)

Backend'i test profiliyle çalıştırın:

```bash
cd backend
mvn spring-boot:run -Dspring.profiles.active=test
```

Bu durumda backend `application-test.properties` dosyasındaki ayarları kullanır ve test veritabanına (`yazilimdogrulama_test`) bağlanır.

### Seçenek 2: Ana application.properties ile

Eğer `application.properties` dosyası test veritabanına ayarlanmışsa:

```bash
cd backend
mvn spring-boot:run
```

**Not:** Bu durumda normal veritabanı yerine test veritabanı kullanılır.

## Frontend'i Çalıştırma

```bash
cd frontend
npm install  # İlk kez çalıştırıyorsanız
npm run dev
```

Frontend `http://localhost:5173` adresinde çalışmalı.

## Testleri Çalıştırma

Backend ve Frontend çalışırken, başka bir terminalde:

```bash
cd backend
# Tek bir test sınıfı
mvn test -Dtest=Case11_NotificationTest

# Tüm selenium testleri
mvn test -Dtest="*Selenium*"

# Belirli bir test metodu
mvn test -Dtest=Case11_NotificationTest#case11_1_CommentNotification
```

## Test Veritabanı Ayarları

Test veritabanı bağlantı bilgileri:
- **URL:** `jdbc:postgresql://localhost:5433/yazilimdogrulama_test`
- **Username:** `postgres`
- **Password:** `postgres`
- **Port:** `5433` (veya varsayılan `5432`)

System property'ler ile özelleştirilebilir:
```bash
mvn spring-boot:run -Dspring.profiles.active=test \
  -Dspring.datasource.url=jdbc:postgresql://localhost:5433/yazilimdogrulama_test \
  -Dspring.datasource.username=postgres \
  -Dspring.datasource.password=postgres
```

## Önemli Notlar

1. **Test veritabanı ayrı olmalı:** Normal veritabanınızı (`yazilimdogrulama`) test veritabanından (`yazilimdogrulama_test`) ayırın.

2. **Admin kullanıcısı:** Testler otomatik olarak test veritabanında admin kullanıcısı oluşturur.

3. **Veritabanı temizleme:** Test profili `create-drop` kullanır, bu yüzden her test çalıştırmasında veritabanı yeniden oluşturulur.

4. **Port kontrolü:** 
   - Frontend: `http://localhost:5173`
   - Backend: `http://localhost:8080`
   - PostgreSQL: `localhost:5433` (veya `5432`)

## Sorun Giderme

### Backend test veritabanına bağlanamıyor
- PostgreSQL'in çalıştığından emin olun
- Test veritabanının oluşturulduğunu kontrol edin
- Port numarasını kontrol edin (5433 veya 5432)

### Frontend backend'e bağlanamıyor
- Backend'in `http://localhost:8080` adresinde çalıştığından emin olun
- CORS ayarlarını kontrol edin

### Testler başarısız oluyor
- Backend ve Frontend'in çalıştığından emin olun
- Test veritabanının boş olduğundan emin olun (testler kendi verilerini oluşturur)
- ChromeDriver'ın güncel olduğundan emin olun

