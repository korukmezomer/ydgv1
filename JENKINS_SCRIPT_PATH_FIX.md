# 🔧 Jenkins "Unable to find Jenkinsfile" Hatası Çözümü

## Problem: Jenkins Jenkinsfile'ı bulamıyor

Hata: `ERROR: Unable to find Jenkinsfile from git https://github.com/korukmezomer/ydgv1.git`

## ✅ Çözüm: Script Path'i Kontrol Et

### Adım 1: Jenkins Pipeline Yapılandırmasını Kontrol Et

1. Jenkins Dashboard → **"yazilimdogrulama-backend-pipeline"** → **"Configure"**
2. **"Pipeline"** bölümüne git
3. **"Script Path"** alanını kontrol et

### Adım 2: Doğru Script Path'i Ayarla

**Script Path** şu şekilde olmalı:

```
backend/Jenkinsfile
```

**ÖNEMLİ:**
- ❌ `/backend/Jenkinsfile` (başında slash olmamalı)
- ❌ `Jenkinsfile` (sadece dosya adı yeterli değil)
- ✅ `backend/Jenkinsfile` (doğru format)

### Adım 3: Diğer Ayarları Kontrol Et

1. **Definition**: "Pipeline script from SCM" seçili olmalı
2. **SCM**: "Git" seçili olmalı
3. **Repository URL**: `https://github.com/korukmezomer/ydgv1.git`
4. **Credentials**: GitHub token credentials seçili olmalı
5. **Branches to build**: `*/main` veya `*/master`
6. **Script Path**: `backend/Jenkinsfile` (en önemli!)

### Adım 4: Save ve Test Et

1. **"Save"** butonuna tıkla
2. **"Build Now"** ile test et
3. Build loglarını kontrol et

## 🔍 Alternatif: Root'ta Jenkinsfile Varsa

Eğer Jenkinsfile root dizinindeyse (backend/ dışında), Script Path:

```
Jenkinsfile
```

olmalı.

## 🐛 Hala Çalışmıyorsa

### Kontrol 1: GitHub'da Dosya Var mı?

Tarayıcıda aç:
```
https://github.com/korukmezomer/ydgv1/blob/main/backend/Jenkinsfile
```

Eğer 404 hatası alıyorsan, dosya GitHub'a push edilmemiş demektir.

### Kontrol 2: Git Push Yap

```bash
cd /Users/omerkorukmez/Desktop/yazılımdogrulama
git add backend/Jenkinsfile
git commit -m "Add Jenkinsfile"
git push https://TOKEN@github.com/korukmezomer/ydgv1.git main
```

### Kontrol 3: Jenkins Workspace'i Temizle

1. Jenkins → Pipeline projesi → **"Workspace"** linkine tıkla
2. **"Wipe Out Current Workspace"** butonuna tıkla
3. Tekrar **"Build Now"** yap

## ✅ Doğru Yapılandırma Özeti

```
Definition: Pipeline script from SCM
SCM: Git
Repository URL: https://github.com/korukmezomer/ydgv1.git
Credentials: github-token (veya oluşturduğun credentials)
Branches: */main
Script Path: backend/Jenkinsfile  ← BU ÇOK ÖNEMLİ!
```

Bu ayarlarla Jenkins Jenkinsfile'ı bulmalı! 🎯

