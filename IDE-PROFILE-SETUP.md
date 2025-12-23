# IDE'de Profile Ayarlama (IntelliJ IDEA)

## Hızlı Çözüm ✅

`application.properties` dosyası artık Docker PostgreSQL'e (port 5433) bağlanacak şekilde güncellendi. 
**Artık profile ayarlamaya gerek yok!** Direkt çalıştırabilirsiniz.

---

## Eğer Profile Kullanmak İsterseniz

### IntelliJ IDEA'da Profile Ayarlama:

1. **Run Configuration Oluştur/Düzenle:**
   - `BackendApplication` sınıfına sağ tıklayın
   - "Modify Run Configuration..." seçin
   - VEYA üst menüden: Run → Edit Configurations...

2. **Active Profiles Ekle:**
   - "Active profiles" alanını bulun
   - `docker` yazın
   - "Apply" ve "OK" tıklayın

3. **VEYA Environment Variables:**
   - "Environment variables" bölümüne tıklayın
   - `+` butonuna tıklayın
   - Name: `SPRING_PROFILES_ACTIVE`
   - Value: `docker`
   - "Apply" ve "OK"

4. **Çalıştırın:**
   - Run butonuna tıklayın

---

## Profile'lar

- **default** → `application.properties` (localhost:5433 - Docker PostgreSQL)
- **docker** → `application-docker.properties` (localhost:5433 - Docker PostgreSQL)
- **local** → `application-local.properties` (localhost:5432 - Local PostgreSQL)
- **test** → `application-test.properties` (Test ortamı)

---

## Kontrol

Backend başladığında log'larda şunu görmelisiniz:
```
The following 1 profile is active: "docker"
```

VEYA

```
No active profile set, falling back to 1 default profile: "default"
```

Her iki durumda da Docker PostgreSQL'e (port 5433) bağlanacak.

