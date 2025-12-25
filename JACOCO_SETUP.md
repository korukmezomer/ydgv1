# JaCoCo Code Coverage Setup

Bu dokümantasyon, projede JaCoCo (Java Code Coverage) implementasyonunu açıklar.

## Özellikler

- ✅ Unit test coverage raporları
- ✅ Integration test coverage raporları
- ✅ Selenium test coverage raporları
- ✅ Birleşik coverage raporu (tüm testler)
- ✅ HTML ve XML formatında raporlar
- ✅ Jenkins'te görüntülenebilir raporlar

## Maven Yapılandırması

`backend/pom.xml` dosyasına JaCoCo Maven plugin'i eklenmiştir:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <!-- Prepare agent for unit tests -->
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <!-- Generate report after unit tests -->
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <!-- Generate report after integration tests -->
        <execution>
            <id>report-integration</id>
            <phase>verify</phase>
            <goals>
                <goal>report-integration</goal>
            </goals>
        </execution>
        <!-- Check coverage thresholds -->
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.50</minimum>
                            </limit>
                        </limits>
                    </rule>
                    <rule>
                        <element>CLASS</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.50</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Jenkins Pipeline Yapılandırması

Jenkins pipeline'ında her test stage'inden sonra coverage raporları oluşturulur:

1. **Unit Tests Stage**: Unit test coverage raporu
2. **Integration Tests Stage**: Integration test coverage raporu
3. **Selenium Tests Stage**: Selenium test coverage raporu
4. **Code Coverage Report Stage**: Tüm testler için birleşik coverage raporu

## Coverage Raporlarını Görüntüleme

### Jenkins'te

1. Jenkins build sayfasına gidin
2. Sol menüde "JaCoCo Coverage Report (All Tests)" linkine tıklayın
3. HTML formatında detaylı coverage raporunu görüntüleyin

### Lokal Ortamda

1. Testleri çalıştırın:
   ```bash
   cd backend
   mvn clean test jacoco:report
   ```

2. Coverage raporunu açın:
   ```bash
   open target/site/jacoco/index.html
   ```

   veya Windows'ta:
   ```bash
   start target/site/jacoco/index.html
   ```

## Coverage Metrikleri

JaCoCo aşağıdaki metrikleri sağlar:

- **Line Coverage**: Satır bazında coverage
- **Branch Coverage**: Dal (if/else) bazında coverage
- **Method Coverage**: Metod bazında coverage
- **Class Coverage**: Sınıf bazında coverage
- **Instruction Coverage**: Bytecode instruction bazında coverage

## Coverage Thresholds (Eşik Değerleri)

Şu anda minimum coverage eşikleri:
- **Package Level**: %50 line coverage
- **Class Level**: %50 line coverage

Bu değerleri `pom.xml` dosyasındaki `jacoco-maven-plugin` yapılandırmasından değiştirebilirsiniz.

## Rapor Formatları

### HTML Raporu
- Konum: `backend/target/site/jacoco/index.html`
- Detaylı, interaktif rapor
- Paket, sınıf ve metod bazında coverage bilgisi
- Renk kodlu coverage gösterimi

### XML Raporu
- Konum: `backend/target/site/jacoco/jacoco.xml`
- Jenkins JaCoCo plugin için
- Programatik analiz için uygun

### Aggregate Raporu
- Konum: `backend/target/site/jacoco-aggregate/index.html`
- Tüm testler için birleşik rapor
- Unit, Integration ve Selenium testlerinin toplam coverage'ı

## Komutlar

### Sadece Coverage Raporu Oluşturma
```bash
cd backend
mvn jacoco:report
```

### Test Coverage ile Test Çalıştırma
```bash
cd backend
mvn clean test jacoco:report
```

### Coverage Eşiklerini Kontrol Etme
```bash
cd backend
mvn jacoco:check
```

### Birleşik Coverage Raporu
```bash
cd backend
mvn jacoco:report-aggregate
```

## Jenkins Plugin Gereksinimleri

Jenkins'te coverage raporlarını görüntülemek için aşağıdaki plugin'lerin yüklü olması gerekir:

1. **HTML Publisher Plugin**: HTML raporlarını görüntülemek için
2. **JaCoCo Plugin** (opsiyonel): XML raporlarını görüntülemek için

## Sorun Giderme

### Coverage Raporu Oluşturulmuyor

1. Maven clean yapın:
   ```bash
   mvn clean
   ```

2. Testleri tekrar çalıştırın:
   ```bash
   mvn test jacoco:report
   ```

3. `target/site/jacoco/` klasörünün oluştuğunu kontrol edin

### Jenkins'te Rapor Görünmüyor

1. Jenkins'te "HTML Publisher Plugin" yüklü mü kontrol edin
2. Build log'larında "Publishing HTML reports" mesajını kontrol edin
3. Pipeline'da `publishHTML` adımının çalıştığını kontrol edin

## İleri Seviye Yapılandırma

### Coverage Eşiklerini Artırma

`pom.xml` dosyasında minimum coverage değerlerini artırabilirsiniz:

```xml
<minimum>0.80</minimum> <!-- %80 coverage gereksinimi -->
```

### Belirli Paketleri Hariç Tutma

Coverage hesaplamasından belirli paketleri hariç tutmak için:

```xml
<configuration>
    <excludes>
        <exclude>**/dto/**</exclude>
        <exclude>**/entity/**</exclude>
    </excludes>
</configuration>
```

## Kaynaklar

- [JaCoCo Maven Plugin Documentation](https://www.jacoco.org/jacoco/trunk/doc/maven.html)
- [JaCoCo Official Website](https://www.jacoco.org/jacoco/)

