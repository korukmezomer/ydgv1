# 🔧 Jenkins Credentials Sorunu Çözümü

## Problem: Credentials Dropdown'da "None" Görünüyor

Bu sorun genellikle credentials'ın yanlış tipte veya yanlış yerde oluşturulmasından kaynaklanır.

## ✅ Çözüm: Doğru Credentials Tipini Kullan

### Yöntem 1: Username with Password (Önerilen)

1. **Jenkins** → **"Manage Jenkins"** → **"Credentials"**
2. **"System"** → **"Global credentials (unrestricted)"**
3. **"Add Credentials"** butonuna tıkla
4. Formu şu şekilde doldur:
   - **Kind**: **"Username with password"** seç (Secret text değil!)
   - **Scope**: "Global" (varsayılan)
   - **Username**: GitHub kullanıcı adın (örnek: `omerkorukmez`)
   - **Password**: GitHub Personal Access Token'ın (token'ı buraya yapıştır)
   - **ID**: `github-token` (veya istediğin bir isim)
   - **Description**: "GitHub Personal Access Token"
5. **"Create"** butonuna tıkla

### Yöntem 2: Secret Text (Alternatif)

Eğer "Username with password" çalışmazsa:

1. **Kind**: **"Secret text"** seç
2. **Secret**: GitHub Personal Access Token'ını yapıştır
3. **ID**: `github-token`
4. **Description**: "GitHub Personal Access Token"
5. **"Create"** butonuna tıkla

**Not**: Secret text kullanıyorsan, Repository URL'de token'ı manuel eklemen gerekebilir:
- URL formatı: `https://TOKEN@github.com/kullaniciadi/repo.git`

## 🔍 Credentials'ı Kontrol Et

1. **Manage Jenkins** → **"Credentials"**
2. **"System"** → **"Global credentials (unrestricted)"**
3. Listede oluşturduğun credentials'ı görmelisin
4. Eğer görmüyorsan, yukarıdaki adımları tekrar takip et

## 📝 Pipeline'da Credentials Kullanımı

Pipeline yapılandırmasında:

1. **Repository URL**: `https://github.com/kullaniciadi/yazilimdogrulama.git`
2. **Credentials**: Dropdown'dan az önce oluşturduğun credentials'ı seç
   - Eğer hala "None" görünüyorsa, sayfayı yenile (F5)
   - Veya credentials'ı silip yeniden oluştur

## 🚨 Hala Çalışmıyorsa

### Adım 1: Mevcut Credentials'ı Sil
1. **Manage Jenkins** → **"Credentials"**
2. Oluşturduğun credentials'ı bul
3. Yanındaki dropdown'dan **"Delete"** seç

### Adım 2: Yeniden Oluştur
Yukarıdaki **"Yöntem 1: Username with password"** adımlarını takip et.

### Adım 3: Jenkins'i Yeniden Başlat (Gerekirse)
```bash
docker-compose restart jenkins
```

## ✅ Doğru Yapılandırma Özeti

**Credentials:**
- Kind: **Username with password**
- Username: GitHub kullanıcı adın
- Password: GitHub Personal Access Token
- ID: `github-token`

**Pipeline:**
- Repository URL: `https://github.com/kullaniciadi/yazilimdogrulama.git`
- Credentials: `github-token` (dropdown'dan seç)
- Script Path: `backend/Jenkinsfile`

Bu ayarlarla credentials dropdown'da görünmeli! 🎯

