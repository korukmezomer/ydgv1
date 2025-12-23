# 🚀 Jenkins Hızlı Başlangıç Rehberi

## 1️⃣ Jenkins'i Başlat

```bash
# Proje root dizininde
docker-compose up -d jenkins
```

## 2️⃣ Jenkins Admin Şifresini Al

```bash
docker exec yazilimdogrulama-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

## 3️⃣ Jenkins'e Giriş Yap

1. Tarayıcıda aç: **http://localhost:8082**
2. Admin şifresini yapıştır
3. "Install suggested plugins" seç
4. Admin kullanıcısı oluştur

## 4️⃣ Pipeline Oluştur

1. **New Item** → İsim: `yazilimdogrulama-backend-pipeline`
2. **Pipeline** seç → **OK**
3. **Pipeline** bölümü:
   - **Definition**: `Pipeline script from SCM`
   - **SCM**: `Git`
   - **Repository URL**: 
     - Local için: `/var/jenkins_home/workspace/backend`
     - Remote için: Git repo URL'iniz
   - **Script Path**: `Jenkinsfile`
4. **Build Triggers**: ✅ **Poll SCM** → `H/5 * * * *`
5. **Save**

## 5️⃣ İlk Build'i Çalıştır

1. Pipeline sayfasında **Build Now** butonuna tıkla
2. **#1** build numarasına tıklayarak logları izle
3. **Test Result** linkinden test sonuçlarını görüntüle

## ✅ Artık Her Commit'te Otomatik Test!

Her commit'te Jenkins otomatik olarak:
- ✅ Unit testleri çalıştırır
- ✅ Integration testleri çalıştırır  
- ✅ Selenium testleri çalıştırır
- ✅ Sonuçları Jenkins Dashboard'da gösterir

## 📊 Test Sonuçlarını Görüntüleme

- **Jenkins Dashboard**: http://localhost:8082
- **Pipeline**: http://localhost:8082/job/yazilimdogrulama-backend-pipeline
- **Test Results**: Build sayfasında **Test Result** linki

## 🔧 Sorun mu Var?

Detaylı kurulum için: `JENKINS_DOCKER_SETUP.md` dosyasına bakın.

