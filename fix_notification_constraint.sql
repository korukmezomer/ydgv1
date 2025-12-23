-- Fix notification type constraint to include YORUM_YANITI
-- Run this SQL command directly in your PostgreSQL database

-- Remove the old check constraint
ALTER TABLE bildirimler DROP CONSTRAINT IF EXISTS bildirimler_tip_check;

-- Add new check constraint with all notification types
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
