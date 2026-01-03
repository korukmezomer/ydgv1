#!/bin/bash

# Admin kullanıcısı hariç tüm kullanıcıları silme scripti
# Kullanım: ./delete_users_except_admin.sh

# Veritabanı bağlantı bilgileri (application.properties'ten alınmıştır)
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5433}"
DB_NAME="${DB_NAME:-yazilimdogrulama}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-postgres}"

echo "==========================================="
echo "🗑️  Admin kullanıcısı hariç tüm kullanıcıları silme"
echo "==========================================="
echo "Veritabanı: $DB_NAME"
echo "Host: $DB_HOST:$DB_PORT"
echo "Kullanıcı: $DB_USER"
echo ""
echo "⚠️  UYARI: Bu işlem geri alınamaz!"
echo "Admin kullanıcısı (omer@gmail.com) korunacak."
echo "==========================================="
echo ""

# Onay iste
read -p "Devam etmek istediğinize emin misiniz? (yes/no): " confirm
if [ "$confirm" != "yes" ]; then
    echo "İşlem iptal edildi."
    exit 1
fi

# SQL scriptini çalıştır
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f delete_users_except_admin.sql

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ İşlem başarıyla tamamlandı!"
else
    echo ""
    echo "❌ İşlem sırasında bir hata oluştu!"
    exit 1
fi

