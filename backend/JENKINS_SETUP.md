# Jenkins CI/CD Kurulum Rehberi

Bu rehber, projeniz için Jenkins CI/CD pipeline'ının nasıl kurulacağını adım adım açıklar.

## 📋 Gereksinimler

- Java 17 veya üzeri
- Maven 3.9.5 veya üzeri
- PostgreSQL (test veritabanı için)
- Git
- Chrome/Chromium (Selenium testleri için)

## 🚀 Adım 1: Jenkins Kurulumu

### Windows/Mac/Linux için Jenkins Kurulumu

#### Yöntem 1: Docker ile (Önerilen)

```bash
# Jenkins container'ını çalıştır
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  jenkins/jenkins:lts

# İlk şifreyi al
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

#### Yöntem 2: Manuel Kurulum

1. [Jenkins İndirme Sayfası](https://www.jenkins.io/download/) adresinden Jenkins'i indirin
2. İndirilen dosyayı çalıştırın
3. Kurulum sihirbazını takip edin
4. İlk admin şifresini not edin

### Jenkins'e Erişim

1. Tarayıcınızda `http://localhost:8080` adresine gidin
2. İlk kurulum sihirbazında:
   - İlk admin şifresini girin
   - "Install suggested plugins" seçeneğini seçin
   - Admin kullanıcısı oluşturun

## 🔧 Adım 2: Jenkins Yapılandırması

### 2.1. Global Tools Yapılandırması

1. Jenkins Dashboard → **Manage Jenkins** → **Tools**
2. **JDK** bölümüne tıklayın:
   - **Name**: `JDK-17`
   - **JAVA_HOME**: Java 17 kurulum yolu (örn: `/usr/lib/jvm/java-17-openjdk` veya `C:\Program Files\Java\jdk-17`)
   - **Add JDK** butonuna tıklayın

3. **Maven** bölümüne tıklayın:
   - **Name**: `Maven-3.9.5`
   - **MAVEN_HOME**: Maven kurulum yolu (örn: `/opt/maven` veya `C:\Program Files\Apache\maven`)
   - **Add Maven** butonuna tıklayın

4. **Save** butonuna tıklayın

### 2.2. PostgreSQL Eklentisi (Opsiyonel)

1. **Manage Jenkins** → **Plugins** → **Available**
2. "PostgreSQL" araması yapın
3. "PostgreSQL API Plugin" kurun
4. Jenkins'i yeniden başlatın

### 2.3. Git Yapılandırması

1. **Manage Jenkins** → **Global Tool Configuration**
2. **Git** bölümünde:
   - **Name**: `Default`
   - **Path to Git executable**: Git kurulum yolu (örn: `/usr/bin/git` veya `C:\Program Files\Git\bin\git.exe`)

## 📦 Adım 3: Pipeline Projesi Oluşturma

### 3.1. Yeni Pipeline Oluştur

1. Jenkins Dashboard → **New Item**
2. **Item name**: `yazilimdogrulama-backend-pipeline`
3. **Pipeline** seçeneğini seçin
4. **OK** butonuna tıklayın

### 3.2. Pipeline Yapılandırması

1. **Pipeline** bölümünde:
   - **Definition**: `Pipeline script from SCM`
   - **SCM**: `Git`
   - **Repository URL**: Projenizin Git repository URL'i
     - Örnek: `https://github.com/kullaniciadi/yazilimdogrulama.git`
     - Veya: `file:///path/to/local/repo` (local repo için)
   - **Credentials**: Eğer private repo ise, Git credentials ekleyin
   - **Branches to build**: `*/main` veya `*/master`
   - **Script Path**: `backend/Jenkinsfile`

2. **Build Triggers** bölümünde:
   - ✅ **Poll SCM** seçeneğini işaretleyin
   - **Schedule**: `H/5 * * * *` (Her 5 dakikada bir kontrol eder)
   - Veya **GitHub hook trigger** kullanabilirsiniz (GitHub kullanıyorsanız)

3. **Save** butonuna tıklayın

## 🔗 Adım 4: Git Hook Yapılandırması (Otomatik Tetikleme)

### 4.1. GitHub Webhook (GitHub kullanıyorsanız)

1. GitHub repository → **Settings** → **Webhooks** → **Add webhook**
2. **Payload URL**: `http://your-jenkins-url:8080/github-webhook/`
3. **Content type**: `application/json`
4. **Events**: `Just the push event`
5. **Active**: ✅
6. **Add webhook**

### 4.2. GitLab Webhook (GitLab kullanıyorsanız)

1. GitLab repository → **Settings** → **Webhooks**
2. **URL**: `http://your-jenkins-url:8080/project/yazilimdogrulama-backend-pipeline`
3. **Trigger**: `Push events`
4. **Add webhook**

### 4.3. Local Git Hook (Manuel Commit için)

1. Proje klasöründe `.git/hooks/post-commit` dosyası oluşturun:

```bash
#!/bin/bash
# Jenkins'i tetikle (eğer local Jenkins kullanıyorsanız)
curl -X POST http://localhost:8080/job/yazilimdogrulama-backend-pipeline/build \
  --user admin:your-api-token
```

2. Dosyayı çalıştırılabilir yapın:
```bash
chmod +x .git/hooks/post-commit
```

## 🗄️ Adım 5: Test Veritabanı Hazırlama

### 5.1. PostgreSQL Test Veritabanı Oluşturma

```bash
# PostgreSQL'e bağlan
psql -U postgres -h localhost -p 5433

# Test veritabanını oluştur
CREATE DATABASE yazilimdogrulama_test;

# Çıkış
\q
```

### 5.2. Jenkins'te Veritabanı Erişimi

Jenkins'in PostgreSQL'e erişebildiğinden emin olun. Eğer Docker kullanıyorsanız:

```bash
# Jenkins container'ına PostgreSQL client kur
docker exec -it jenkins bash
apt-get update
apt-get install -y postgresql-client
```

## 🧪 Adım 6: Selenium Test Ortamı

### 6.1. Chrome/Chromium Kurulumu (Jenkins Server'da)

#### Linux:
```bash
# Chrome kurulumu
wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
sudo dpkg -i google-chrome-stable_current_amd64.deb
sudo apt-get install -f -y
```

#### Docker Jenkins için:
```bash
# Jenkins Dockerfile'ına ekleyin veya container'a girin
docker exec -it jenkins bash
apt-get update
apt-get install -y chromium chromium-driver
```

### 6.2. Display Server (Headless için gerekli değil)

Selenium testleri headless modda çalışacak şekilde yapılandırıldı, bu yüzden X server gerekmez.

## ▶️ Adım 7: İlk Pipeline Çalıştırma

1. Jenkins Dashboard → **yazilimdogrulama-backend-pipeline**
2. **Build Now** butonuna tıklayın
3. **#1** build numarasına tıklayarak detayları görüntüleyin
4. **Console Output** sekmesinden logları takip edin

## 📊 Adım 8: Test Sonuçlarını Görüntüleme

1. Build sayfasında **Test Result** linkine tıklayın
2. Test sonuçları görüntülenecektir:
   - ✅ Başarılı testler
   - ❌ Başarısız testler
   - ⏱️ Test süreleri

## 🔔 Adım 9: Bildirim Yapılandırması (Opsiyonel)

### Email Bildirimi

1. **Manage Jenkins** → **Configure System**
2. **E-mail Notification** bölümünde:
   - **SMTP server**: `smtp.gmail.com` (Gmail için)
   - **Default user e-mail suffix**: `@gmail.com`
   - **Use SMTP Authentication**: ✅
   - **User Name**: Email adresiniz
   - **Password**: App password (Gmail için)
   - **Test configuration** ile test edin

3. Pipeline projesinde:
   - **Post-build Actions** → **Email Notification**
   - **Recipients**: Bildirim gönderilecek email adresleri

## 🐛 Sorun Giderme

### Problem: Maven bulunamıyor
**Çözüm**: Global Tools Configuration'da Maven path'ini kontrol edin

### Problem: JDK bulunamıyor
**Çözüm**: Global Tools Configuration'da JDK path'ini kontrol edin

### Problem: PostgreSQL bağlantı hatası
**Çözüm**: 
- Test veritabanının oluşturulduğundan emin olun
- PostgreSQL'in çalıştığını kontrol edin: `systemctl status postgresql`
- Firewall ayarlarını kontrol edin

### Problem: Selenium testleri başarısız
**Çözüm**:
- Chrome/Chromium'un kurulu olduğundan emin olun
- Frontend ve Backend'in çalıştığından emin olun
- Headless mod aktif mi kontrol edin

### Problem: Git hook çalışmıyor
**Çözüm**:
- Webhook URL'inin doğru olduğundan emin olun
- Jenkins'in erişilebilir olduğundan emin olun
- GitHub/GitLab webhook loglarını kontrol edin

## 📝 Jenkinsfile Özelleştirme

`backend/Jenkinsfile` dosyasını ihtiyaçlarınıza göre özelleştirebilirsiniz:

- **Stages**: Test aşamalarını ekleyip çıkarabilirsiniz
- **Environment**: Ortam değişkenlerini değiştirebilirsiniz
- **Notifications**: Slack, Teams gibi bildirimler ekleyebilirsiniz

## 🎯 Sonuç

Artık her commit'te otomatik olarak:
1. ✅ Kod derlenecek
2. ✅ Unit testler çalışacak
3. ✅ Entegrasyon testleri çalışacak
4. ✅ Selenium testleri çalışacak
5. ✅ JAR dosyası oluşturulacak
6. ✅ Test sonuçları görüntülenecek

Jenkins Dashboard'da tüm bu süreçleri takip edebilirsiniz!

