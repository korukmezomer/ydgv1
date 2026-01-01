package com.example.backend.infrastructure.config;

import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.RoleRepository;
import com.example.backend.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test") // Test profili dışında tüm profillerde çalışır
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository rolRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("===========================================");
        System.out.println("🚀 DataInitializer başlatılıyor...");
        System.out.println("📊 Veritabanı URL: " + (datasourceUrl != null ? datasourceUrl : "null"));
        System.out.println("===========================================");
        
        // Eski rol isimlerini yeni isimlere güncelle
        updateRoleName("READER", "USER", "Kullanıcı - Okuma, beğeni, kayıt, liste oluşturma ve takip yetkisi");
        updateRoleName("AUTHOR", "WRITER", "Yazar - İçerik oluşturma yetkisi");
        
        // Varsayılan roller oluştur (3 ana rol: ADMIN, WRITER, USER)
        createRoleIfNotExists("ADMIN", "Yönetici - Tüm yetkilere sahip");
        createRoleIfNotExists("WRITER", "Yazar - İçerik oluşturma yetkisi");
        createRoleIfNotExists("USER", "Kullanıcı - Okuma, beğeni, kayıt, liste oluşturma ve takip yetkisi");
        
        // Test için admin kullanıcısı oluştur (sadece test veritabanında)
        createAdminUserIfNotExists();
        
        System.out.println("===========================================");
        System.out.println("✅ DataInitializer tamamlandı");
        System.out.println("===========================================");
    }

    private void updateRoleName(String oldRolAdi, String newRolAdi, String newAciklama) {
        rolRepository.findByName(oldRolAdi).ifPresent(oldRol -> {
            // Eğer yeni rol adı zaten varsa, eski rolü sil
            if (rolRepository.existsByName(newRolAdi)) {
                oldRol.setIsActive(false);
                rolRepository.save(oldRol);
            } else {
                // Eski rol adını yeni isimle güncelle
                oldRol.setName(newRolAdi);
                oldRol.setDescription(newAciklama);
                rolRepository.save(oldRol);
            }
        });
    }

    private void createRoleIfNotExists(String rolAdi, String aciklama) {
        if (!rolRepository.existsByName(rolAdi)) {
            Role rol = new Role();
            rol.setName(rolAdi);
            rol.setDescription(aciklama);
            rol.setIsActive(true);
            rolRepository.save(rol);
        }
    }
    
    /**
     * Admin kullanıcısını kontrol et (oluşturma veya güncelleme yapılmaz)
     * Veritabanında zaten mevcut olan omer@gmail.com / 123456 kullanıcısı kullanılır
     */
    private void createAdminUserIfNotExists() {
        String adminEmail = "omer@gmail.com";
        
        System.out.println("📋 Admin kullanıcısı kontrolü başlatılıyor...");
        System.out.println("  - Email: " + adminEmail);
        System.out.println("  - Veritabanı URL: " + (datasourceUrl != null ? datasourceUrl : "null"));
        
        try {
            // Admin kullanıcısı zaten var mı kontrol et (aktif olanları)
            var existingActiveUser = userRepository.findActiveByEmail(adminEmail);
            if (existingActiveUser.isPresent()) {
                User user = existingActiveUser.get();
                System.out.println("✅ Admin kullanıcısı zaten var (aktif): " + adminEmail);
                System.out.println("  - ID: " + user.getId());
                System.out.println("  - Username: " + user.getUsername());
                System.out.println("  - Roller: " + user.getRoles().stream().map(Role::getName).toList());
                return; // Zaten var, hiçbir şey yapma
            }
            
            // Pasif admin kullanıcısı var mı kontrol et
            var existingUser = userRepository.findByEmail(adminEmail);
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                System.out.println("⚠️ Admin kullanıcısı var ama pasif: " + adminEmail);
                System.out.println("  - ID: " + user.getId());
                System.out.println("  - Username: " + user.getUsername());
                System.out.println("  - Roller: " + user.getRoles().stream().map(Role::getName).toList());
                System.out.println("ℹ️ Kullanıcı pasif durumda, manuel olarak aktif yapılması gerekebilir");
                return;
            }
            
            // Kullanıcı bulunamadı
            System.out.println("⚠️ Admin kullanıcısı bulunamadı: " + adminEmail);
            System.out.println("ℹ️ Admin kullanıcısı veritabanında mevcut olmalı (omer@gmail.com / 123456)");
        } catch (Exception e) {
            // Hata durumunda logla ama uygulamayı durdurma
            System.err.println("⚠️ Admin kullanıcısı kontrolü hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

