# Kurulum ve Çalıştırma Kılavuzu

## Senaryo 1: Docker Compose ile Çalıştırma (Önerilen)

### Adımlar:

1. **Docker Compose ile servisleri başlat:**
   ```bash
   cd /Users/omerkorukmez/Desktop/yazılımdogrulama
   make up
   # veya
   docker-compose up -d
   ```

2. **PostgreSQL'in hazır olmasını bekle:**
   ```bash
   docker-compose ps
   # postgres servisinin "healthy" olmasını bekleyin
   ```

3. **Backend'i çalıştır:**
   - IDE'den `BackendApplication` sınıfını çalıştırın
   - Profile: `docker` seçin (Run Configuration'da)
   - Veya environment variable: `SPRING_PROFILES_ACTIVE=docker`

### Docker Compose Portları:
- PostgreSQL: `localhost:5433` (5432 zaten kullanılıyorsa)
- Backend: `localhost:8080`

---

## Senaryo 2: Local PostgreSQL ile Çalıştırma

### Önkoşullar:
- Local PostgreSQL kurulu ve çalışıyor
- Port 5432'de çalışıyor

### Adımlar:

1. **Veritabanını oluştur:**
   ```sql
   CREATE DATABASE yazilimdogrulama;
   ```

2. **Şifreyi kontrol et:**
   - `application.properties` dosyasındaki şifre local PostgreSQL şifrenizle eşleşmeli
   - Varsayılan: `postgres`

3. **Backend'i çalıştır:**
   - IDE'den `BackendApplication` sınıfını çalıştırın
   - Profile: `default` veya `local` kullanın

### Local PostgreSQL Şifresini Değiştirme:

Eğer local PostgreSQL şifreniz farklıysa:

1. `application.properties` dosyasını açın
2. `spring.datasource.password` değerini güncelleyin
3. Veya `application-local.properties` oluşturup profile olarak kullanın

---

## Senaryo 3: Port Çakışması Çözümü

Eğer port 5432 zaten kullanılıyorsa:

### Seçenek A: Docker Compose'u farklı portta çalıştır
- `docker-compose.yml` dosyasında port `5433:5432` olarak ayarlanmış
- Backend'de `application-docker.properties`'te URL'yi güncelleyin:
  ```
  spring.datasource.url=jdbc:postgresql://localhost:5433/yazilimdogrulama
  ```

### Seçenek B: Local PostgreSQL'i durdur
```bash
# macOS
brew services stop postgresql

# veya
pg_ctl -D /usr/local/var/postgres stop
```

---

## IDE'de Profile Ayarlama

### IntelliJ IDEA:

1. Run Configuration oluştur/düzenle
2. "Environment variables" bölümüne ekle:
   ```
   SPRING_PROFILES_ACTIVE=docker
   ```
3. Veya "Active profiles" alanına: `docker`

### VS Code:

`.vscode/launch.json`:
```json
{
  "configurations": [
    {
      "type": "java",
      "request": "launch",
      "mainClass": "com.example.backend.BackendApplication",
      "projectName": "backend",
      "vmArgs": "-Dspring.profiles.active=docker"
    }
  ]
}
```

---

## Hızlı Test

### 1. PostgreSQL bağlantısını test et:
```bash
# Docker'daki PostgreSQL
docker-compose exec postgres psql -U postgres -d yazilimdogrulama

# Local PostgreSQL
psql -U postgres -d yazilimdogrulama
```

### 2. Backend health check:
```bash
curl http://localhost:8080/actuator/health
```

### 3. API test:
```bash
# Kayıt ol
curl -X POST http://localhost:8080/api/auth/kayit \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "sifre": "123456",
    "ad": "Test",
    "soyad": "User"
  }'
```

---

## Sorun Giderme

### "password authentication failed"
- PostgreSQL şifresini kontrol edin
- `application.properties` veya `application-docker.properties` dosyasındaki şifreyi güncelleyin

### "port already in use"
- Port 5432 kullanılıyorsa Docker Compose port 5433 kullanıyor
- `application-docker.properties`'te URL'yi güncelleyin

### "Connection refused"
- PostgreSQL servisinin çalıştığından emin olun
- Docker: `docker-compose ps`
- Local: `brew services list` veya `pg_ctl status`

### Veritabanı yok
- Docker: Otomatik oluşturulur
- Local: `CREATE DATABASE yazilimdogrulama;` çalıştırın

