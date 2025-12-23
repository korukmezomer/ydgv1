# Jenkins Docker Compose Kurulum Rehberi

Bu rehber, Jenkins'in Docker Compose yapılandırmasına nasıl eklendiğini ve nasıl kullanılacağını açıklar.

## 📋 Mevcut Yapılandırma

Jenkins artık `docker-compose.yml` dosyasına eklenmiştir ve şu özelliklere sahiptir:

- **Port**: `8082` (http://localhost:8082)
- **Jenkins Home**: Docker volume'da saklanır (`jenkins_home`)
- **Network**: `backend-network` (PostgreSQL ve Backend ile aynı network)
- **Docker Socket**: Jenkins container'ından Docker komutları çalıştırılabilir
- **Otomatik Kurulum**: PostgreSQL client, Chrome, Git, Maven, JDK otomatik kurulur

## 🚀 Jenkins'i Başlatma

### 1. Tüm Servisleri Başlat (PostgreSQL + Backend + Jenkins)

```bash
# Proje root dizininde
docker-compose up -d
```

Bu komut şunları başlatır:
- PostgreSQL (port 5433)
- Backend (port 8080)
- Jenkins (port 8082)

### 2. Jenkins İlk Kurulum

Jenkins ilk kez başlatıldığında admin şifresini almak için:

```bash
# Jenkins container loglarını kontrol et
docker logs yazilimdogrulama-jenkins

# Veya direkt şifreyi al
docker exec yazilimdogrulama-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### 3. Jenkins'e Erişim

1. Tarayıcınızda `http://localhost:8082` adresine gidin
2. İlk kurulum sihirbazında:
   - Admin şifresini girin (yukarıdaki komutla aldığınız)
   - "Install suggested plugins" seçeneğini seçin
   - Admin kullanıcısı oluşturun

## 🔧 Jenkins Yapılandırması

### 1. Global Tools Yapılandırması

Jenkins container'ında Maven ve JDK zaten kurulu, ancak Jenkins UI'dan yapılandırmanız gerekir:

1. **Manage Jenkins** → **Tools**
2. **JDK** bölümü:
   - **Name**: `JDK-17`
   - **JAVA_HOME**: `/usr/lib/jvm/default-java` (container içindeki path)
   - **Add JDK**

3. **Maven** bölümü:
   - **Name**: `Maven-3.9.5`
   - **MAVEN_HOME**: `/usr/share/maven` (container içindeki path)
   - **Add Maven**

4. **Git** bölümü:
   - **Name**: `Default`
   - **Path to Git executable**: `/usr/bin/git`
   - **Add Git**

5. **Save**

### 2. Pipeline Projesi Oluşturma

1. **New Item** → **Item name**: `yazilimdogrulama-backend-pipeline`
2. **Pipeline** seçeneğini seçin
3. **OK**

4. **Pipeline** bölümünde:
   - **Definition**: `Pipeline script from SCM`
   - **SCM**: `Git`
   - **Repository URL**: 
     - Local repo için: `/var/jenkins_home/workspace/backend` (container içindeki path)
     - Remote repo için: `https://github.com/kullaniciadi/yazilimdogrulama.git`
   - **Branches**: `*/main` veya `*/master`
   - **Script Path**: `Jenkinsfile`

5. **Build Triggers**:
   - ✅ **Poll SCM**: `H/5 * * * *` (her 5 dakikada bir)
   - Veya **GitHub hook trigger** (remote repo için)

6. **Save**

## 🔗 Network Yapılandırması

Jenkins container'ı `backend-network` içinde olduğu için:

- **PostgreSQL'e erişim**: `postgres:5432` (container name ile)
- **Backend'e erişim**: `backend:8080` (container name ile)
- **Host'tan erişim**: `localhost:8080` (port mapping ile)

### Jenkinsfile'da Network Erişimi

Jenkinsfile içinde test veritabanına erişim için:

```groovy
environment {
    TEST_DB_URL = 'jdbc:postgresql://postgres:5432/yazilimdogrulama_test'
    TEST_DB_USER = 'postgres'
    TEST_DB_PASSWORD = 'postgres'
    
    // Backend ve Frontend URL'leri
    BACKEND_URL = 'http://backend:8080'  // Container içinden
    FRONTEND_URL = 'http://localhost:5173'  // Host'tan (eğer frontend ayrı çalışıyorsa)
}
```

## 🧪 Test Veritabanı Oluşturma

Jenkins container'ından test veritabanını oluşturmak için:

```bash
# Jenkins container'ına gir
docker exec -it yazilimdogrulama-jenkins bash

# PostgreSQL'e bağlan ve test veritabanını oluştur
PGPASSWORD=postgres psql -h postgres -U postgres -d postgres -c "CREATE DATABASE yazilimdogrulama_test;"
```

Veya Jenkinsfile içinde otomatik oluşturulabilir (zaten ekli).

## 📊 Jenkins Dashboard Erişimi

- **URL**: http://localhost:8082
- **Admin Panel**: http://localhost:8082/manage
- **Pipeline**: http://localhost:8082/job/yazilimdogrulama-backend-pipeline

## 🔄 Servisleri Yönetme

### Tüm servisleri başlat
```bash
docker-compose up -d
```

### Sadece Jenkins'i başlat
```bash
docker-compose up -d jenkins
```

### Jenkins loglarını görüntüle
```bash
docker logs -f yazilimdogrulama-jenkins
```

### Jenkins'i durdur
```bash
docker-compose stop jenkins
```

### Jenkins'i yeniden başlat
```bash
docker-compose restart jenkins
```

### Jenkins verilerini sil (dikkatli!)
```bash
docker-compose down -v jenkins_home
```

## 🐛 Sorun Giderme

### Problem: Jenkins container başlamıyor
**Çözüm**: 
```bash
docker logs yazilimdogrulama-jenkins
# Hata mesajlarını kontrol edin
```

### Problem: PostgreSQL'e erişemiyor
**Çözüm**: 
- Network'ün doğru olduğundan emin olun: `docker network ls`
- Container'ların aynı network'te olduğunu kontrol edin: `docker inspect yazilimdogrulama-jenkins | grep NetworkMode`

### Problem: Maven/JDK bulunamıyor
**Çözüm**: 
- Container içinde kontrol edin: `docker exec yazilimdogrulama-jenkins which mvn`
- Global Tools Configuration'da path'leri kontrol edin

### Problem: Git repository'ye erişemiyor
**Çözüm**: 
- Local repo için: `/var/jenkins_home/workspace/backend` path'ini kullanın
- Remote repo için: Git credentials ekleyin

### Problem: Selenium testleri başarısız
**Çözüm**: 
- Chrome'un kurulu olduğunu kontrol edin: `docker exec yazilimdogrulama-jenkins which chromium`
- Frontend'in çalıştığından emin olun
- Headless mod aktif mi kontrol edin: `-Dselenium.headless=true`

## 📝 Jenkinsfile Güncellemesi

Jenkinsfile'ı Docker Compose ortamına uygun hale getirmek için:

```groovy
environment {
    // Container içinden erişim
    TEST_DB_URL = 'jdbc:postgresql://postgres:5432/yazilimdogrulama_test'
    BACKEND_URL = 'http://backend:8080'
    
    // Host'tan erişim (Selenium testleri için)
    FRONTEND_URL = 'http://host.docker.internal:5173'  // Docker Desktop için
    // veya
    FRONTEND_URL = 'http://172.17.0.1:5173'  // Linux için
}
```

## 🎯 Avantajlar

✅ **Tüm servisler aynı network'te**: Kolay iletişim
✅ **Volume persistence**: Jenkins verileri kalıcı
✅ **Otomatik kurulum**: Gerekli araçlar container başlatılırken kurulur
✅ **Docker-in-Docker**: Jenkins container'ından Docker komutları çalıştırılabilir
✅ **Kolay yönetim**: `docker-compose` ile tüm servisleri yönetin

## 🚀 Hızlı Başlangıç

```bash
# 1. Tüm servisleri başlat
docker-compose up -d

# 2. Jenkins admin şifresini al
docker exec yazilimdogrulama-jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# 3. Tarayıcıda aç
# http://localhost:8082

# 4. Pipeline'ı oluştur (yukarıdaki adımları takip edin)

# 5. İlk build'i çalıştır
# Jenkins Dashboard → yazilimdogrulama-backend-pipeline → Build Now
```

Artık Jenkins projenizin Docker Compose yapılandırmasının bir parçası! 🎉

