# Docker Setup Guide

Bu proje Docker ve Docker Compose kullanarak çalıştırılabilir.

## Gereksinimler

- Docker Desktop (veya Docker Engine + Docker Compose)
- Minimum 4GB RAM
- 2GB disk alanı

## Hızlı Başlangıç

### 1. Tüm servisleri başlatma

```bash
# Servisleri build edip başlat
make build
make up

# veya direkt olarak
docker-compose up -d --build
```

### 2. Servisleri durdurma

```bash
make down

# veya
docker-compose down
```

### 3. Logları görüntüleme

```bash
# Tüm servislerin logları
make logs

# Sadece backend logları
make logs-backend

# Sadece PostgreSQL logları
make logs-postgres
```

## Servisler

### Backend API
- **URL**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Container**: `yazilimdogrulama-backend`

### PostgreSQL Database
- **Host**: localhost
- **Port**: 5432
- **Database**: yazilimdogrulama
- **Username**: postgres
- **Password**: postgres
- **Container**: `yazilimdogrulama-postgres`

## Test Ortamı

Otomasyon testleri için ayrı bir test ortamı:

```bash
# Test ortamını başlat
make test-up

# Test ortamını durdur
make test-down

# Testleri çalıştır
make test
```

Test ortamı:
- Backend: http://localhost:8081
- PostgreSQL: localhost:5433
- Database: yazilimdogrulama_test

## Kullanışlı Komutlar

```bash
# Çalışan container'ları listele
make ps

# Backend container'ına shell aç
make shell-backend

# PostgreSQL shell'ine bağlan
make shell-postgres

# Tüm container ve volume'ları temizle
make clean
```

## Environment Variables

Docker Compose dosyasında aşağıdaki environment variable'lar tanımlı:

- `SPRING_DATASOURCE_URL`: Veritabanı bağlantı URL'i
- `SPRING_DATASOURCE_USERNAME`: Veritabanı kullanıcı adı
- `SPRING_DATASOURCE_PASSWORD`: Veritabanı şifresi
- `JWT_SECRET`: JWT token secret key
- `JWT_EXPIRATION`: JWT token süresi (milisaniye)

## Veritabanı Yönetimi

### Veritabanına bağlanma

```bash
# Docker üzerinden
make shell-postgres

# veya direkt
docker-compose exec postgres psql -U postgres -d yazilimdogrulama
```

### Veritabanını sıfırlama

```bash
# Volume'u sil ve yeniden oluştur
docker-compose down -v
docker-compose up -d
```

## Sorun Giderme

### Port çakışması

Eğer 5432 veya 8080 portları kullanılıyorsa, `docker-compose.yml` dosyasındaki port numaralarını değiştirin.

### Container başlamıyor

```bash
# Logları kontrol et
make logs

# Container durumunu kontrol et
make ps

# Container'ı yeniden build et
make build
make up
```

### Veritabanı bağlantı hatası

1. PostgreSQL container'ının çalıştığından emin olun: `make ps`
2. Health check'i kontrol edin: `docker-compose ps`
3. Logları kontrol edin: `make logs-postgres`

## Production Deployment

Production için:

1. `JWT_SECRET` değerini güçlü bir secret ile değiştirin
2. Veritabanı şifresini değiştirin
3. `application-docker.properties` dosyasını production ayarlarına göre güncelleyin
4. Health check endpoint'lerini yapılandırın
5. Logging yapılandırması ekleyin

## Otomasyon Testleri

Test ortamı otomatik olarak:
- Yeni bir veritabanı oluşturur (`yazilimdogrulama_test`)
- Her test çalıştırmasında veritabanını sıfırlar
- Test sonrası container'ları temizler

```bash
# Testleri çalıştır
make test
```

