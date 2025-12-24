# ⚠️ Güvenlik Uyarısı

## GitHub Token Güvenliği

GitHub Personal Access Token'ınız kod içinde commit edilmişti. Bu token artık **güvenli değil** ve derhal revoke edilmelidir.

## 🔒 Token'ı İptal Etme

1. GitHub.com → **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)**
2. İlgili token'ı bulun (kod içinde commit edilmiş olan token)
3. **Revoke** butonuna tıklayın
4. Yeni bir token oluşturun (gerekirse)

## ✅ Yeni Token ile Push

Yeni token oluşturduktan sonra:

```bash
# Token'ı environment variable olarak kullan (önerilen)
export GITHUB_TOKEN="your_new_token_here"
git push https://${GITHUB_TOKEN}@github.com/korukmezomer/ydgv1.git main

# Veya remote URL'i güncelle
git remote set-url origin https://YOUR_NEW_TOKEN@github.com/korukmezomer/ydgv1.git
git push origin main
```

## 🛡️ Güvenlik Best Practices

1. **Token'ları asla kod içine commit etmeyin**
2. **Environment variables kullanın**
3. **Token'ları `.gitignore` ile koruyun**
4. **Düzenli olarak token'ları rotate edin**

## 📝 Not

`GITHUB_PUSH.md` dosyasındaki token kaldırıldı ve placeholder ile değiştirildi.

