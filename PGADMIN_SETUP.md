# pgAdmin ile PostgreSQL Bağlantı Kılavuzu

Bu kılavuz, Docker Compose ile çalışan PostgreSQL veritabanına pgAdmin ile nasıl bağlanacağınızı açıklar.

## 1. pgAdmin Servisini Başlatma

pgAdmin servisi `docker-compose.yml` dosyasına eklenmiştir. Servisi başlatmak için:

```bash
docker-compose up -d pgadmin
```

Tüm servisleri (PostgreSQL, Backend, Jenkins, pgAdmin) birlikte başlatmak için:

```bash
docker-compose up -d
```

## 2. pgAdmin'e Erişim

pgAdmin web arayüzüne tarayıcınızdan şu adresten erişebilirsiniz:

```
http://localhost:5050
```

### İlk Giriş Bilgileri:
- **Email**: `admin@admin.com`
- **Password**: `admin`

> ⚠️ **Güvenlik Uyarısı**: Bu varsayılan şifreler sadece geliştirme ortamı içindir. Production ortamında mutlaka değiştirin!

## 3. PostgreSQL Sunucusunu pgAdmin'e Ekleme

### Adım 1: "Add New Server" Tıklayın
pgAdmin'e giriş yaptıktan sonra, sol taraftaki "Servers" bölümüne sağ tıklayın ve "Register" > "Server..." seçeneğini seçin.

### Adım 2: General Sekmesi
- **Name**: `Yazilimdogrulama DB` (veya istediğiniz bir isim)

### Adım 3: Connection Sekmesi
Aşağıdaki bilgileri girin:

- **Host name/address**: `postgres` (Docker Compose network içinden erişim için)
  - ⚠️ **Önemli**: pgAdmin Docker container içinde çalıştığı için, PostgreSQL container'ının servis adını (`postgres`) kullanmalısınız.
  - Eğer pgAdmin'i Docker dışında kullanıyorsanız: `localhost` veya `127.0.0.1`

- **Port**: `5432` (PostgreSQL container'ının iç portu)

- **Maintenance database**: `yazilimdogrulama` (veya `postgres`)

- **Username**: `postgres`

- **Password**: `postgres`

- **Save password**: ✅ İşaretleyin (isteğe bağlı, şifreyi her seferinde girmemek için)

### Adım 4: Advanced Sekmesi (Opsiyonel)
- **DB restriction**: `yazilimdogrulama` (sadece bu veritabanını görmek istiyorsanız)

### Adım 5: Save
"Save" butonuna tıklayın.

## 4. Bağlantıyı Test Etme

Bağlantı başarılı olduysa, sol tarafta sunucu adı görünecek ve genişletildiğinde:
- `Databases` klasörü
- `yazilimdogrulama` veritabanı
- Tablolar, view'lar, function'lar vb. görünecektir.

## 5. Veritabanı İşlemleri

pgAdmin üzerinden:
- ✅ Tabloları görüntüleyebilirsiniz
- ✅ SQL sorguları çalıştırabilirsiniz
- ✅ Veri ekleyebilir, düzenleyebilir, silebilirsiniz
- ✅ Tablo yapılarını inceleyebilirsiniz
- ✅ İndeksleri, constraint'leri görebilirsiniz

## Sorun Giderme

### Bağlantı Hatası: "could not connect to server"

**Çözüm 1**: PostgreSQL servisinin çalıştığından emin olun:
```bash
docker-compose ps postgres
```

**Çözüm 2**: Host name'i kontrol edin:
- Docker Compose içinden: `postgres`
- Docker dışından: `localhost` veya `127.0.0.1`

**Çözüm 3**: Port'u kontrol edin:
- Container içi: `5432`
- Host'tan: `5433` (docker-compose.yml'de port mapping: `5433:5432`)

### pgAdmin'e Erişilemiyor

**Çözüm**: pgAdmin servisinin çalıştığından emin olun:
```bash
docker-compose ps pgadmin
docker-compose logs pgadmin
```

### Şifre Hatası

**Çözüm**: `docker-compose.yml` dosyasındaki PostgreSQL şifresini kontrol edin:
- `POSTGRES_PASSWORD: postgres`

## Hızlı Başlangıç Komutları

```bash
# Tüm servisleri başlat
docker-compose up -d

# Sadece PostgreSQL ve pgAdmin'i başlat
docker-compose up -d postgres pgadmin

# Servis durumunu kontrol et
docker-compose ps

# pgAdmin loglarını görüntüle
docker-compose logs pgadmin

# PostgreSQL loglarını görüntüle
docker-compose logs postgres
```

## Güvenlik Notları

1. **Production Ortamı**: Production'da mutlaka:
   - pgAdmin şifresini değiştirin
   - PostgreSQL şifresini güçlü bir şifre yapın
   - pgAdmin portunu (5050) firewall ile koruyun veya sadece localhost'tan erişilebilir yapın

2. **Şifre Değiştirme**: pgAdmin şifresini değiştirmek için:
   - pgAdmin arayüzünde: File > Preferences > Change Password

## Ek Kaynaklar

- [pgAdmin Dokümantasyonu](https://www.pgadmin.org/docs/)
- [PostgreSQL Docker Image](https://hub.docker.com/_/postgres)
- [pgAdmin Docker Image](https://hub.docker.com/r/dpage/pgadmin4)

