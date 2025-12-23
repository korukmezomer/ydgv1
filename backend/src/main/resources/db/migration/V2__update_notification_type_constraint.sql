-- Remove the old check constraint if it exists
ALTER TABLE bildirimler DROP CONSTRAINT IF EXISTS bildirimler_tip_check;

-- Add new check constraint with all notification types including YORUM_YANITI
ALTER TABLE bildirimler ADD CONSTRAINT bildirimler_tip_check 
CHECK (tip IN (
    'YENI_YORUM',
    'YORUM_YANITI',
    'YORUM_BEGENILDI',
    'HABER_BEGENILDI',
    'HABER_YAYINLANDI',
    'HABER_REDDEDILDI',
    'YENI_TAKIPCI',
    'GENEL'
));
