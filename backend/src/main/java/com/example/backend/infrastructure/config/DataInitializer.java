package com.example.backend.infrastructure.config;

import com.example.backend.domain.entity.Role;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.RoleRepository;
import com.example.backend.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Profile("!test") // Test profili dışında tüm profillerde çalışır
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository rolRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Override
    public void run(String... args) throws Exception {
        // Eski rol isimlerini yeni isimlere güncelle
        updateRoleName("READER", "USER", "Kullanıcı - Okuma, beğeni, kayıt, liste oluşturma ve takip yetkisi");
        updateRoleName("AUTHOR", "WRITER", "Yazar - İçerik oluşturma yetkisi");
        
        // Varsayılan roller oluştur (3 ana rol: ADMIN, WRITER, USER)
        createRoleIfNotExists("ADMIN", "Yönetici - Tüm yetkilere sahip");
        createRoleIfNotExists("WRITER", "Yazar - İçerik oluşturma yetkisi");
        createRoleIfNotExists("USER", "Kullanıcı - Okuma, beğeni, kayıt, liste oluşturma ve takip yetkisi");
        
        // Test için admin kullanıcısı oluştur (sadece test veritabanında)
        createAdminUserIfNotExists();
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
     * Test için admin kullanıcısı oluştur
     * Sadece test veritabanında (yazilimdogrulama_test) çalışır
     */
    private void createAdminUserIfNotExists() {
        String adminEmail = "admin@test.com";
        String adminPassword = "admin123";
        
        // Sadece test veritabanında admin kullanıcısı oluştur
        if (datasourceUrl == null || !datasourceUrl.contains("yazilimdogrulama_test")) {
            // Production veritabanında admin kullanıcısı oluşturma
            return;
        }
        
        try {
            // Admin kullanıcısı zaten var mı kontrol et
            if (userRepository.findActiveByEmail(adminEmail).isPresent()) {
                return; // Zaten var, oluşturma
            }
            
            // ADMIN rolünü bul
            Role adminRole = rolRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN rolü bulunamadı"));
            
            // Admin kullanıcısı oluştur
            User adminUser = new User();
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setUsername("admin");
            adminUser.setIsActive(true);
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            adminUser.setRoles(roles);
            
            userRepository.save(adminUser);
            System.out.println("✅ Test admin kullanıcısı oluşturuldu: " + adminEmail);
        } catch (Exception e) {
            // Hata durumunda logla ama uygulamayı durdurma
            System.err.println("⚠️ Admin kullanıcısı oluşturulurken hata: " + e.getMessage());
        }
    }
}

