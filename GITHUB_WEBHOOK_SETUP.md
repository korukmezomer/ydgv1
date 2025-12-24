# 🔗 GitHub Webhook ile Otomatik Jenkins Build

GitHub'a commit attığınızda Jenkins'te otomatik olarak test süreçlerinin başlaması için webhook yapılandırması.

## ⚠️ Önemli Not

Jenkins Docker container'ında `localhost:8082` üzerinde çalışıyor. GitHub webhook'ları **sadece public URL'lere** istek gönderebilir. Bu nedenle iki seçenek var:

### Seçenek 1: Poll SCM (Önerilen - Docker için)
Jenkins her X dakikada bir GitHub'ı kontrol eder ve yeni commit varsa build başlatır.

### Seçenek 2: GitHub Webhook (Public Jenkins için)
Jenkins public bir URL'de çalışıyorsa webhook kullanılabilir.

---

## 📋 Adım 1: Jenkins Pipeline Yapılandırması

### 1.1. Jenkins'te Pipeline'ı Yapılandır

1. Jenkins Dashboard → **"yazilimdogrulama-backend-pipeline"** → **"Configure"**

2. **"Build Triggers"** bölümünde:

   **Seçenek A: Poll SCM (Docker için önerilen)**
   - ✅ **"Poll SCM"** seçeneğini işaretle
   - **Schedule**: `H/2 * * * *` (Her 2 dakikada bir kontrol eder)
     - Veya `H/5 * * * *` (Her 5 dakikada bir)
     - Veya `* * * * *` (Her dakika - sadece test için)

   **Seçenek B: GitHub Webhook (Public Jenkins için)**
   - ✅ **"GitHub hook trigger for GITScm polling"** seçeneğini işaretle

3. **"Save"** butonuna tıkla

---

## 📋 Adım 2: GitHub Webhook Yapılandırması (Seçenek 2 için)

### 2.1. GitHub Repository'ye Git

1. GitHub'da repository'nize gidin: `https://github.com/korukmezomer/ydgv1`

2. **"Settings"** → **"Webhooks"** → **"Add webhook"**

### 2.2. Webhook Ayarları

**⚠️ NOT:** Jenkins localhost'ta çalışıyorsa webhook çalışmaz. Bu durumda **Poll SCM** kullanın.

Eğer Jenkins public bir sunucuda çalışıyorsa:

- **Payload URL**: `http://YOUR_JENKINS_IP:8082/github-webhook/`
  - Örnek: `http://123.45.67.89:8082/github-webhook/`
- **Content type**: `application/json`
- **Which events**: **"Just the push event"** seç
- **Active**: ✅ işaretli
- **"Add webhook"** butonuna tıkla

---

## 📋 Adım 3: Test Et

### 3.1. Test Commit Yap

```bash
cd /Users/omerkorukmez/Desktop/yazılımdogrulama

# Küçük bir değişiklik yap
echo "# Test" >> README.md

# Commit ve push
git add .
git commit -m "Test: Jenkins otomatik build"
git push origin main
```

### 3.2. Jenkins'te Kontrol Et

1. Jenkins Dashboard → **"yazilimdogrulama-backend-pipeline"**
2. **"Build History"** bölümünde yeni bir build başlamalı
3. Build'in otomatik başladığını görün

---

## 🔧 Poll SCM Schedule Formatı

Cron formatı: `MINUTE HOUR DAY MONTH DAY_OF_WEEK`

Örnekler:
- `H/2 * * * *` - Her 2 dakikada bir
- `H/5 * * * *` - Her 5 dakikada bir
- `H * * * *` - Her saat başı
- `H H/2 * * *` - Her 2 saatte bir
- `H 9 * * 1-5` - Hafta içi her gün saat 9'da

---

## ✅ Başarı Kontrolü

### Poll SCM Kullanıyorsanız:
1. GitHub'a commit push edin
2. 2-5 dakika içinde Jenkins'te otomatik build başlamalı
3. Build loglarında "Started by SCM polling" yazmalı

### GitHub Webhook Kullanıyorsanız:
1. GitHub'a commit push edin
2. Birkaç saniye içinde Jenkins'te otomatik build başlamalı
3. Build loglarında "Started by GitHub push" yazmalı

---

## 🐛 Sorun Giderme

### Problem: Build başlamıyor

**Çözüm 1: Poll SCM Schedule'ı kontrol et**
- Jenkins → Pipeline → Configure → Build Triggers
- Schedule formatının doğru olduğundan emin ol

**Çözüm 2: GitHub credentials kontrol et**
- Jenkins → Manage Jenkins → Credentials
- GitHub token'ının doğru olduğundan emin ol

**Çözüm 3: Repository URL kontrol et**
- Jenkins → Pipeline → Configure → Pipeline
- Repository URL'in doğru olduğundan emin ol

### Problem: Webhook çalışmıyor

**Çözüm:**
- Jenkins localhost'ta çalışıyorsa webhook çalışmaz
- **Poll SCM** kullanın (her 2-5 dakikada bir kontrol eder)

---

## 📝 Özet

✅ **Docker Jenkins için:** Poll SCM kullan (her 2-5 dakikada bir)  
✅ **Public Jenkins için:** GitHub Webhook kullan (anında tetiklenir)  
✅ **Test için:** Küçük bir commit yap ve Jenkins'te build'in başladığını kontrol et

---

## 🎯 Sonuç

Artık GitHub'a her commit push ettiğinizde:
- ✅ Jenkins otomatik olarak build başlatır
- ✅ Unit testleri çalışır
- ✅ Integration testleri çalışır
- ✅ Selenium testleri çalışır
- ✅ Sonuçlar Jenkins Dashboard'da görüntülenir

