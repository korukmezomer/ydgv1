# Hızlı Başlangıç

## ✅ Çözüm: Docker PostgreSQL + IDE'den Backend

### Adım 1: Docker PostgreSQL'i Başlat

```bash
cd /Users/omerkorukmez/Desktop/yazılımdogrulama
docker-compose up -d postgres
```

PostgreSQL port **5433**'te çalışacak (5432 zaten kullanılıyor).

### Adım 2: IDE'de Backend'i Çalıştır

**IntelliJ IDEA:**

1. `BackendApplication` sınıfına sağ tıklayın
2. "Modify Run Configuration" seçin
3. "Environment variables" bölümüne ekleyin:
   ```
   SPRING_PROFILES_ACTIVE=docker
   ```
4. Veya "Active profiles" alanına: `docker` yazın
5. Çalıştırın

**Not:** `application-docker.properties` dosyası `localhost:5433` kullanıyor.

---

## 🔍 Alternatif: Local PostgreSQL Şifresini Öğren

Eğer local PostgreSQL kullanmak istiyorsanız:

1. Terminal'de:
   ```bash
   psql -U postgres
   ```
   Şifrenizi girin

2. Veritabanını oluşturun:
   ```sql
   CREATE DATABASE yazilimdogrulama;
   ```

3. `application.properties` dosyasındaki şifreyi local şifrenizle eşleştirin

---

## 🐳 Tüm Servisleri Docker'da Çalıştırma

```bash
# Tüm servisleri başlat
make up

# Logları görüntüle
make logs

# Durumu kontrol et
make ps
```

---

## ✅ Başarı Kontrolü

Backend başladıktan sonra:

```bash
# Health check
curl http://localhost:8080/actuator/health

# API test
curl http://localhost:8080/api/kategoriler
```

Başarılı yanıt alırsanız, her şey hazır! 🎉

