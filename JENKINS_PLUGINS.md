# Jenkins Plugin Gereksinimleri

Bu proje için Jenkins'te aşağıdaki plugin'lerin yüklü olması gerekmektedir:

## Zorunlu Plugin'ler

### 1. JUnit Plugin
- **Amaç**: Test sonuçlarını görüntülemek için
- **Kurulum**: Jenkins > Manage Jenkins > Plugins > "JUnit" ara ve yükle
- **Kullanım**: Pipeline'da `junit` adımı ile test sonuçlarını kaydeder

### 2. HTML Publisher Plugin (Önerilen)
- **Amaç**: JaCoCo coverage raporlarını HTML formatında görüntülemek için
- **Kurulum**: Jenkins > Manage Jenkins > Plugins > "HTML Publisher" ara ve yükle
- **Kullanım**: Pipeline'da `publishHTML` adımı ile HTML raporlarını yayınlar
- **Not**: Yüklü değilse, coverage raporları artifact olarak kaydedilir ve manuel olarak indirilebilir

## Opsiyonel Plugin'ler

### 3. JaCoCo Plugin
- **Amaç**: JaCoCo XML raporlarını görüntülemek için
- **Kurulum**: Jenkins > Manage Jenkins > Plugins > "JaCoCo" ara ve yükle
- **Kullanım**: XML formatındaki coverage raporlarını görselleştirir

## Plugin Kurulum Adımları

1. Jenkins ana sayfasına gidin
2. **Manage Jenkins** > **Plugins** seçeneğine tıklayın
3. **Available** sekmesine gidin
4. Arama kutusuna plugin adını yazın (örn: "HTML Publisher")
5. Plugin'i seçin ve **Install without restart** veya **Download now and install after restart** seçeneğini seçin
6. Jenkins'i yeniden başlatın (gerekirse)

## Coverage Raporlarına Erişim

### HTML Publisher Plugin Yüklüyse:
1. Build sayfasına gidin
2. Sol menüde "JaCoCo Coverage Report (All Tests)" linkine tıklayın
3. HTML formatında detaylı coverage raporunu görüntüleyin

### HTML Publisher Plugin Yüklü Değilse:
1. Build sayfasına gidin
2. **Artifacts** bölümüne gidin
3. `target/site/jacoco-aggregate/index.html` dosyasını indirin
4. Tarayıcınızda açın

## Plugin Kontrolü

Pipeline'da plugin yüklü mü kontrol etmek için build log'larına bakın:
- ✅ Plugin yüklüyse: "JaCoCo Coverage Report" linki görünür
- ⚠️ Plugin yüklü değilse: "HTML Publisher Plugin yüklü değil" uyarısı görünür ve raporlar artifact olarak kaydedilir

## Sorun Giderme

### publishHTML Metodu Bulunamıyor
**Hata**: `No such DSL method 'publishHTML' found`

**Çözüm**: 
1. HTML Publisher Plugin'i yükleyin
2. Jenkins'i yeniden başlatın
3. Pipeline'ı tekrar çalıştırın

### Coverage Raporları Görünmüyor
**Çözüm**:
1. Build log'larında "Analyzed bundle" mesajını kontrol edin
2. `backend/target/site/jacoco-aggregate/` klasörünün oluştuğunu kontrol edin
3. Artifacts bölümünden manuel olarak indirin

