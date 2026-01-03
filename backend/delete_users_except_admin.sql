-- Admin kullanıcısı hariç tüm kullanıcıları silme scripti
-- Admin kullanıcısı: omer@gmail.com

-- Önce admin kullanıcısının ID'sini al
DO $$
DECLARE
    admin_user_id BIGINT;
    deleted_count INTEGER;
BEGIN
    -- Admin kullanıcısının ID'sini bul
    SELECT id INTO admin_user_id 
    FROM kullanicilar 
    WHERE email = 'omer@gmail.com';
    
    IF admin_user_id IS NULL THEN
        RAISE EXCEPTION 'Admin kullanıcısı (omer@gmail.com) bulunamadı!';
    END IF;
    
    RAISE NOTICE 'Admin kullanıcı ID: %', admin_user_id;
    
    -- İlişkili kayıtları sil (CASCADE olmayan tablolar için)
    
    -- 1. Raporlar (reporter ve reviewer) - Admin'in raporlarını koru
    -- Admin'in raporladığı veya admin tarafından incelenen raporları koru
    DELETE FROM raporlar 
    WHERE (raporlayan_kullanici_id IS NULL OR raporlayan_kullanici_id != admin_user_id)
      AND (inceleyici_kullanici_id IS NULL OR inceleyici_kullanici_id != admin_user_id);
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE 'Silinen rapor sayısı: %', deleted_count;
    
    -- 2. Bildirimler
    DELETE FROM bildirimler 
    WHERE kullanici_id != admin_user_id;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE 'Silinen bildirim sayısı: %', deleted_count;
    
    -- 3. Abonelikler
    DELETE FROM abonelikler 
    WHERE kullanici_id != admin_user_id;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE 'Silinen abonelik sayısı: %', deleted_count;
    
    -- 4. Kaydedilen story'ler
    DELETE FROM saved_stories 
    WHERE kullanici_id != admin_user_id;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE 'Silinen kaydedilen story sayısı: %', deleted_count;
    
    -- 5. Takip ilişkileri (follower ve followed) - Admin'in takip ilişkilerini koru
    -- Admin'in takip ettiği veya admin'i takip eden ilişkileri koru
    DELETE FROM takip 
    WHERE takipci_id != admin_user_id 
      AND takip_edilen_id != admin_user_id;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE 'Silinen takip ilişkisi sayısı: %', deleted_count;
    
    -- 6. Beğeniler
    DELETE FROM likes 
    WHERE kullanici_id != admin_user_id;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE 'Silinen beğeni sayısı: %', deleted_count;
    
    -- 7. Yorumlar (kullanıcıya ait yorumlar)
    DELETE FROM yorumlar 
    WHERE kullanici_id != admin_user_id;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE 'Silinen yorum sayısı: %', deleted_count;
    
    -- 8. Listeler - Önce list_stories join tablosundaki ilişkileri sil
    DELETE FROM list_stories 
    WHERE list_id IN (
        SELECT id FROM listeler WHERE kullanici_id != admin_user_id
    );
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE 'Silinen list_stories ilişkisi sayısı: %', deleted_count;
    
    -- Sonra listeleri sil
    DELETE FROM listeler 
    WHERE kullanici_id != admin_user_id;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE 'Silinen liste sayısı: %', deleted_count;
    
    -- 9. Story'ler - Önce story_tags join tablosundaki ilişkileri sil
    DELETE FROM story_tags 
    WHERE story_id IN (
        SELECT id FROM stories WHERE kullanici_id != admin_user_id
    );
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE 'Silinen story_tags ilişkisi sayısı: %', deleted_count;
    
    -- Sonra story'leri sil
    DELETE FROM stories 
    WHERE kullanici_id != admin_user_id;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE 'Silinen story sayısı: %', deleted_count;
    
    -- 10. Yazar profilleri
    DELETE FROM yazar_profilleri 
    WHERE kullanici_id != admin_user_id;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE 'Silinen yazar profil sayısı: %', deleted_count;
    
    -- 11. Kullanıcı-rol ilişkileri
    DELETE FROM kullanici_roller 
    WHERE kullanici_id != admin_user_id;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE 'Silinen kullanıcı-rol ilişkisi sayısı: %', deleted_count;
    
    -- 12. Son olarak kullanıcıları sil (admin hariç)
    DELETE FROM kullanicilar 
    WHERE id != admin_user_id;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE 'Silinen kullanıcı sayısı: %', deleted_count;
    
    RAISE NOTICE '✅ İşlem tamamlandı! Admin kullanıcısı (omer@gmail.com) korundu.';
    
END $$;

