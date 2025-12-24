# 🚀 GitHub'a Push Yapma

## Problem: Authentication Hatası

Git push yaparken authentication hatası alıyorsunuz. İki çözüm var:

## ✅ Çözüm 1: GitHub Token ile Push (Önerilen)

### Adım 1: GitHub Personal Access Token Al
1. GitHub.com → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. "Generate new token (classic)"
3. Scopes: `repo` işaretle
4. Token'ı kopyala

### Adım 2: Token ile Push Yap

Terminal'de şu komutu çalıştır (TOKEN'ı kendi token'ın ile değiştir):

```bash
cd /Users/omerkorukmez/Desktop/yazılımdogrulama

# Token ile remote URL'i güncelle
git remote set-url origin https://YOUR_GITHUB_TOKEN@github.com/korukmezomer/ydgv1.git

# Push yap
git push origin main
```

**Veya tek seferde:**

```bash
git push https://TOKEN@github.com/korukmezomer/ydgv1.git main
```

## ✅ Çözüm 2: SSH Kullan (Alternatif)

### Adım 1: SSH Key Oluştur (eğer yoksa)

```bash
ssh-keygen -t ed25519 -C "your_email@example.com"
cat ~/.ssh/id_ed25519.pub
```

### Adım 2: SSH Key'i GitHub'a Ekle
1. Çıktıyı kopyala
2. GitHub → Settings → SSH and GPG keys → New SSH key
3. Key'i yapıştır ve kaydet

### Adım 3: Remote URL'i SSH ile Değiştir

```bash
git remote set-url origin git@github.com:korukmezomer/ydgv1.git
git push origin main
```

## ✅ Çözüm 3: GitHub CLI Kullan

```bash
# GitHub CLI kur (eğer yoksa)
brew install gh

# Login ol
gh auth login

# Push yap
git push origin main
```

## 🎯 Hızlı Komut (Token ile)

```bash
cd /Users/omerkorukmez/Desktop/yazılımdogrulama
git push https://GITHUB_TOKEN@github.com/korukmezomer/ydgv1.git main
```

Token'ı `GITHUB_TOKEN` yerine yapıştır.

## ✅ Push Sonrası Jenkins

Push yaptıktan sonra:
1. Jenkins'te Pipeline yapılandırmasına git
2. **Definition**: "Pipeline script from SCM" seç
3. **Repository URL**: `https://github.com/korukmezomer/ydgv1.git`
4. **Credentials**: GitHub token credentials'ını seç
5. **Script Path**: `backend/Jenkinsfile`
6. **Save** ve **Build Now**

Artık Jenkins GitHub'dan Jenkinsfile'ı okuyacak! 🎉

