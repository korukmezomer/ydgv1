# 🚀 GitHub'a Push ve Jenkins Entegrasyonu

## 1️⃣ GitHub Repository Oluşturma

### Adım 1: GitHub'da Yeni Repository Oluştur
1. GitHub.com'a git ve giriş yap
2. Sağ üstteki **"+"** butonuna tıkla → **"New repository"**
3. Repository bilgilerini doldur:
   - **Repository name**: `yazilimdogrulama` (veya istediğin isim)
   - **Description**: "Yazılım Doğrulama Projesi - Medium benzeri platform"
   - **Visibility**: Public veya Private (tercihine göre)
   - **Initialize this repository with**: Hiçbirini işaretleme (README, .gitignore, license)
4. **"Create repository"** butonuna tıkla

### Adım 2: GitHub Repository URL'ini Kopyala
Oluşturduğun repository'nin URL'ini kopyala:
- Örnek: `https://github.com/kullaniciadi/yazilimdogrulama.git`
- Veya SSH: `git@github.com:kullaniciadi/yazilimdogrulama.git`

## 2️⃣ Projeyi Git'e Hazırlama

### Terminal'de şu komutları çalıştır:

```bash
# Proje dizinine git
cd /Users/omerkorukmez/Desktop/yazılımdogrulama

# Git repository'yi başlat
git init

# Tüm dosyaları ekle
git add .

# İlk commit'i yap
git commit -m "Initial commit: Backend, Frontend ve Jenkins CI/CD pipeline"

# GitHub repository'yi remote olarak ekle
# (URL'yi kendi repository URL'in ile değiştir)
git remote add origin https://github.com/KULLANICIADI/REPO_ADI.git

# Ana branch'i main olarak ayarla
git branch -M main

# GitHub'a push yap
git push -u origin main
```

## 3️⃣ Jenkins'i GitHub ile Entegre Etme

### Adım 1: GitHub Personal Access Token Oluştur

1. GitHub.com → Sağ üstte profil fotoğrafına tıkla → **Settings**
2. Sol menüden **"Developer settings"** → **"Personal access tokens"** → **"Tokens (classic)"**
3. **"Generate new token"** → **"Generate new token (classic)"**
4. Token bilgilerini doldur:
   - **Note**: "Jenkins CI/CD"
   - **Expiration**: 90 days (veya istediğin süre)
   - **Scopes**: Şunları işaretle:
     - ✅ `repo` (Full control of private repositories)
     - ✅ `admin:repo_hook` (Full control of repository hooks)
5. **"Generate token"** butonuna tıkla
6. **Token'ı kopyala ve güvenli bir yere kaydet** (bir daha gösterilmeyecek!)

### Adım 2: Jenkins'te GitHub Credentials Ekle

1. Jenkins'e git: http://localhost:8082
2. **"Manage Jenkins"** → **"Credentials"**
3. **"System"** → **"Global credentials (unrestricted)"**
4. **"Add Credentials"** butonuna tıkla
5. Formu doldur:
   - **Kind**: "Secret text"
   - **Secret**: GitHub Personal Access Token'ını yapıştır
   - **ID**: `github-token` (veya istediğin bir isim)
   - **Description**: "GitHub Personal Access Token"
6. **"Create"** butonuna tıkla

### Adım 3: Pipeline'ı GitHub ile Yapılandır

1. Jenkins'te **"yazilimdogrulama-backend-pipeline"** projesine git
2. **"Configure"** butonuna tıkla
3. **"Pipeline"** bölümünde:
   - **Definition**: "Pipeline script from SCM"
   - **SCM**: "Git"
   - **Repository URL**: GitHub repository URL'in
     - Örnek: `https://github.com/kullaniciadi/yazilimdogrulama.git`
   - **Credentials**: Açılır menüden az önce oluşturduğun `github-token`'ı seç
   - **Branches to build**: `*/main` (veya `*/master`)
   - **Script Path**: `backend/Jenkinsfile`
4. **"Build Triggers"** bölümünde:
   - ✅ **"GitHub hook trigger for GITScm polling"** seçeneğini işaretle
   - Veya **"Poll SCM"** seçeneğini bırakabilirsin (her 5 dakikada bir kontrol eder)
5. **"Save"** butonuna tıkla

### Adım 4: GitHub Webhook Ekle (Otomatik Build İçin)

1. GitHub repository'ne git
2. **"Settings"** → **"Webhooks"** → **"Add webhook"**
3. Webhook ayarlarını yap:
   - **Payload URL**: `http://localhost:8082/github-webhook/`
     - ⚠️ **Not**: Eğer Jenkins internet'te değilse, bu çalışmayacak. Bu durumda "Poll SCM" kullan.
   - **Content type**: `application/json`
   - **Which events**: "Just the push event" seç
   - **Active**: ✅ işaretli
4. **"Add webhook"** butonuna tıkla

## 4️⃣ İlk Build'i Test Et

### Yöntem 1: Manuel Build
1. Jenkins'te pipeline sayfasına git
2. **"Build Now"** butonuna tıkla
3. Build'in başladığını gör

### Yöntem 2: GitHub'dan Push
1. Projede küçük bir değişiklik yap (örneğin README.md'ye bir satır ekle)
2. Git'te commit ve push yap:
   ```bash
   git add .
   git commit -m "Test commit for Jenkins"
   git push origin main
   ```
3. Jenkins'te otomatik olarak build başlamalı

## 5️⃣ Build Sonuçlarını Kontrol Et

1. Jenkins Dashboard'da build numarasına tıkla (#1, #2, vb.)
2. **"Console Output"** linkine tıklayarak logları görüntüle
3. **"Test Result"** linkine tıklayarak test sonuçlarını görüntüle

## ✅ Artık Her Commit'te Otomatik Test!

Her GitHub'a push yaptığında:
- ✅ Jenkins otomatik olarak build başlatır
- ✅ Unit testleri çalışır
- ✅ Integration testleri çalışır
- ✅ Selenium testleri çalışır
- ✅ Sonuçlar Jenkins Dashboard'da görüntülenir

## 🔧 Sorun Giderme

### Problem: "Repository not found" hatası
**Çözüm**: 
- GitHub token'ının doğru olduğundan emin ol
- Repository'nin private ise, token'da `repo` scope'unun olduğundan emin ol

### Problem: Webhook çalışmıyor
**Çözüm**: 
- Jenkins localhost'ta çalışıyorsa webhook çalışmaz
- "Poll SCM" seçeneğini kullan (her 5 dakikada bir kontrol eder)

### Problem: "Jenkinsfile not found" hatası
**Çözüm**: 
- Script Path'i `backend/Jenkinsfile` olarak ayarla (root'ta değil)

## 📝 Özet Komutlar

```bash
# Git repository başlat
git init
git add .
git commit -m "Initial commit"

# GitHub'a bağla
git remote add origin https://github.com/KULLANICIADI/REPO_ADI.git
git branch -M main
git push -u origin main

# Sonraki değişiklikler için
git add .
git commit -m "Commit mesajı"
git push origin main
```

Artık her `git push` yaptığında Jenkins otomatik olarak testleri çalıştıracak! 🎉

