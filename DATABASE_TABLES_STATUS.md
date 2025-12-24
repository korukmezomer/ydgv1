# Veritabanı Tabloları Durum Raporu

Bu dokümantasyon, projedeki tüm veritabanı tablolarının mevcut durumunu ve kullanım bilgilerini içerir.

## 📊 Tablo Durumları (Son Kontrol: 2025-12-23)

### ✅ Aktif Kullanılan Tablolar (Kayıt Var)

| Tablo | Kayıt Sayısı | Açıklama | Nasıl Doldurulur |
|-------|--------------|----------|------------------|
| `kullanicilar` | 5 | Kullanıcı hesapları | `/api/auth/kayit` endpoint'i ile kayıt |
| `roller` | 3 | Kullanıcı rolleri (ADMIN, WRITER, USER) | Otomatik oluşturulur (DataInitializer) |
| `stories` | 13 | Haber/hikaye içerikleri | `/api/stories` POST endpoint'i ile oluşturulur |
| `yorumlar` | 18 | Story'lere yapılan yorumlar | `/api/comments/haber/{haberId}` POST endpoint'i |
| `likes` | 12 | Story'lere yapılan beğeniler | `/api/likes/haber/{haberId}` POST endpoint'i |
| `listeler` | 13 | Kullanıcıların oluşturduğu listeler | `/api/lists` POST endpoint'i |
| `saved_stories` | 8 | Kullanıcıların kaydettiği story'ler | `/api/saved-stories/haber/{haberId}` POST |
| `takip` | 2 | Kullanıcılar arası takip ilişkileri | `/api/follow/{takipEdilenId}` POST |
| `bildirimler` | 21 | Kullanıcı bildirimleri | Otomatik oluşturulur (yorum, beğeni, takip vb.) |
| `ortam_dosyalari` | 13 | Yüklenen dosyalar (resim, video vb.) | `/api/media/yukle` POST endpoint'i |

### ⚠️ Tanımlı Ama Henüz Kullanılmayan Tablolar (Boş)

| Tablo | Durum | Açıklama | Implementasyon Durumu |
|-------|-------|----------|----------------------|
| `kategoriler` | 0 | Story kategorileri | ✅ Controller var (`CategoryController`) - Kullanılabilir |
| `etiketler` | 0 | Story etiketleri | ✅ Story oluştururken eklenebilir - Kullanılabilir |
| `yazar_profilleri` | 0 | Yazar profilleri | ✅ Controller var (`AuthorProfileController`) - Kullanılabilir |
| `abonelikler` | 0 | Premium abonelikler | ❌ Controller/Service yok - Henüz implement edilmedi |
| `bultenler` | 0 | E-posta bülteni abonelikleri | ❌ Controller/Service yok - Henüz implement edilmedi |
| `analiz_kayitlari` | 0 | Story görüntüleme/analitik kayıtları | ❌ Controller/Service yok - Henüz implement edilmedi |
| `raporlar` | 0 | İçerik/kullanıcı şikayetleri | ❌ Controller/Service yok - Henüz implement edilmedi |
| `story_versions` | 0 | Story versiyon geçmişi | ❌ Controller/Service yok - Henüz implement edilmedi |

---

## 🔧 Boş Tabloları Doldurma Kılavuzu

### 1. Kategoriler (kategoriler) - ✅ Kullanılabilir

**Endpoint**: `POST /api/categories`

**Örnek Request**:
```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Teknoloji",
    "description": "Teknoloji haberleri ve yazıları"
  }'
```

**pgAdmin ile SQL**:
```sql
INSERT INTO kategoriler (ad, slug, aciklama, is_active, created_at, updated_at)
VALUES 
  ('Teknoloji', 'teknoloji', 'Teknoloji haberleri', true, NOW(), NOW()),
  ('Spor', 'spor', 'Spor haberleri', true, NOW(), NOW()),
  ('Kültür', 'kultur', 'Kültür ve sanat', true, NOW(), NOW());
```

### 2. Etiketler (etiketler) - ✅ Story Oluştururken Eklenebilir

**Endpoint**: Story oluştururken `etiketler` array'i içinde gönderilir:
```json
{
  "baslik": "Örnek Haber",
  "icerik": "...",
  "etiketler": ["teknoloji", "yapay-zeka", "gelecek"]
}
```

**pgAdmin ile SQL**:
```sql
INSERT INTO etiketler (ad, slug, is_active, created_at, updated_at)
VALUES 
  ('Teknoloji', 'teknoloji', true, NOW(), NOW()),
  ('Yapay Zeka', 'yapay-zeka', true, NOW(), NOW()),
  ('Gelecek', 'gelecek', true, NOW(), NOW());
```

### 3. Yazar Profilleri (yazar_profilleri) - ✅ Kullanılabilir

**Endpoint**: `POST /api/author-profiles/kullanici/{kullaniciId}`

**Örnek Request**:
```bash
curl -X POST http://localhost:8080/api/author-profiles/kullanici/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "bio": "Yazılım geliştirici ve teknoloji yazarı",
    "website": "https://example.com",
    "socialMediaLinks": {
      "twitter": "@example",
      "linkedin": "example"
    }
  }'
```

**pgAdmin ile SQL**:
```sql
-- Önce bir kullanıcı ID'si alın
SELECT id FROM kullanicilar LIMIT 1;

-- Yazar profili oluşturun (kullanici_id'yi yukarıdaki sorgudan alın)
INSERT INTO yazar_profilleri (kullanici_id, bio, website, is_active, created_at, updated_at)
VALUES (1, 'Yazılım geliştirici', 'https://example.com', true, NOW(), NOW());
```

---

## 🚧 Henüz Implement Edilmemiş Özellikler

Aşağıdaki tablolar entity olarak tanımlanmış ancak henüz controller/service implementasyonu yapılmamış:

### 1. Abonelikler (abonelikler)
- **Amaç**: Premium abonelik yönetimi
- **Durum**: Entity var, Controller/Service yok
- **Gerekli**: SubscriptionController, SubscriptionService implementasyonu

### 2. Bültenler (bultenler)
- **Amaç**: E-posta bülteni abonelik yönetimi
- **Durum**: Entity var, Controller/Service yok
- **Gerekli**: NewsletterController, NewsletterService implementasyonu

### 3. Analiz Kayıtları (analiz_kayitlari)
- **Amaç**: Story görüntüleme, tıklama gibi analitik veriler
- **Durum**: Entity var, Controller/Service yok
- **Gerekli**: AnalyticsService implementasyonu (genellikle otomatik kayıt)

### 4. Raporlar (raporlar)
- **Amaç**: İçerik/kullanıcı şikayet yönetimi
- **Durum**: Entity var, Controller/Service yok
- **Gerekli**: ReportController, ReportService implementasyonu

### 5. Story Versiyonları (story_versions)
- **Amaç**: Story düzenleme geçmişi
- **Durum**: Entity var, Controller/Service yok
- **Gerekli**: StoryVersionService implementasyonu (Story güncellemelerinde otomatik kayıt)

---

## 📝 Öneriler

### Hemen Kullanılabilir:
1. **Kategoriler**: Story oluştururken kategori seçimi için kategoriler oluşturun
2. **Etiketler**: Story oluştururken etiketler ekleyin
3. **Yazar Profilleri**: WRITER rolündeki kullanıcılar için profil oluşturun

### Gelecek Geliştirmeler:
1. **Abonelik Sistemi**: Premium özellikler için abonelik yönetimi
2. **Bülten Sistemi**: E-posta pazarlama için abonelik yönetimi
3. **Analitik**: Story performans takibi için analitik kayıtları
4. **Rapor Sistemi**: İçerik moderasyonu için şikayet yönetimi
5. **Versiyon Kontrolü**: Story düzenleme geçmişi

---

## 🔍 Tablo İlişkileri

```
kullanicilar (User)
  ├── stories (Story)
  ├── yorumlar (Comment)
  ├── likes (Like)
  ├── listeler (ListEntity)
  ├── saved_stories (SavedStory)
  ├── takip (Follow) [follower/followed]
  ├── bildirimler (Notification)
  ├── yazar_profilleri (AuthorProfile) [OneToOne]
  └── abonelikler (Subscription)

stories (Story)
  ├── kategoriler (Category) [ManyToOne]
  ├── etiketler (Tag) [ManyToMany]
  ├── story_versions (StoryVersion) [OneToMany]
  └── analiz_kayitlari (AnalyticsRecord) [OneToMany]

bultenler (Newsletter)
  └── kategoriler (Category) [ManyToMany - bulten_kategoriler]
```

---

## 💡 Hızlı Test Verisi Oluşturma

pgAdmin'de aşağıdaki SQL'i çalıştırarak test verileri oluşturabilirsiniz:

```sql
-- Kategoriler
INSERT INTO kategoriler (ad, slug, aciklama, is_active, created_at, updated_at)
VALUES 
  ('Teknoloji', 'teknoloji', 'Teknoloji haberleri ve yazıları', true, NOW(), NOW()),
  ('Spor', 'spor', 'Spor haberleri ve analizleri', true, NOW(), NOW()),
  ('Kültür', 'kultur', 'Kültür ve sanat içerikleri', true, NOW(), NOW()),
  ('Ekonomi', 'ekonomi', 'Ekonomi haberleri', true, NOW(), NOW()),
  ('Sağlık', 'saglik', 'Sağlık ve yaşam', true, NOW(), NOW());

-- Etiketler
INSERT INTO etiketler (ad, slug, is_active, created_at, updated_at)
VALUES 
  ('Yapay Zeka', 'yapay-zeka', true, NOW(), NOW()),
  ('Blockchain', 'blockchain', true, NOW(), NOW()),
  ('Startup', 'startup', true, NOW(), NOW()),
  ('Futbol', 'futbol', true, NOW(), NOW()),
  ('Basketbol', 'basketbol', true, NOW(), NOW());

-- Yazar profilleri (mevcut kullanıcılar için)
INSERT INTO yazar_profilleri (kullanici_id, bio, website, is_active, created_at, updated_at)
SELECT 
  id,
  'Yazılım geliştirici ve teknoloji yazarı',
  'https://example.com',
  true,
  NOW(),
  NOW()
FROM kullanicilar
WHERE id NOT IN (SELECT kullanici_id FROM yazar_profilleri WHERE kullanici_id IS NOT NULL)
LIMIT 3;
```

---

## 📞 Sorun Giderme

### Tabloda kayıt yok ama endpoint var:
- Endpoint'in doğru çalıştığından emin olun
- Authentication token'ınızın geçerli olduğundan emin olun
- Backend loglarını kontrol edin

### Tabloda kayıt yok ve endpoint yok:
- Bu özellik henüz implement edilmemiş
- Gerekirse yeni controller/service ekleyin

### Tabloda kayıt var ama görünmüyor:
- `is_active = false` olabilir
- Soft delete kullanılıyor olabilir
- Filtreleme yapılıyor olabilir

