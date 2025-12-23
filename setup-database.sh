#!/bin/bash

# PostgreSQL veritabanı kurulum scripti

echo "PostgreSQL veritabanı kurulumu başlatılıyor..."

# Veritabanı oluştur (varsa hata vermez)
psql -U postgres <<EOF
-- Veritabanını oluştur (eğer yoksa)
SELECT 'CREATE DATABASE yazilimdogrulama'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'yazilimdogrulama')\gexec

-- Bağlantıyı test et
\c yazilimdogrulama
SELECT 'Veritabanı başarıyla oluşturuldu!' AS status;
EOF

if [ $? -eq 0 ]; then
    echo "✅ Veritabanı hazır!"
    echo "📝 Şifre kontrolü: application.properties dosyasındaki şifre local PostgreSQL şifrenizle eşleşmeli"
else
    echo "❌ Hata: PostgreSQL'e bağlanılamadı"
    echo "💡 Şifre sorulursa, local PostgreSQL şifrenizi girin"
    echo "💡 Veya application.properties dosyasındaki şifreyi güncelleyin"
fi

