package com.example.backend.infrastructure.config;

import com.example.backend.domain.entity.Role;
import com.example.backend.domain.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository rolRepository;

    @Override
    public void run(String... args) throws Exception {
        // Eski rol isimlerini yeni isimlere güncelle
        updateRoleName("READER", "USER", "Kullanıcı - Okuma, beğeni, kayıt, liste oluşturma ve takip yetkisi");
        updateRoleName("AUTHOR", "WRITER", "Yazar - İçerik oluşturma yetkisi");
        
        // Varsayılan roller oluştur (3 ana rol: ADMIN, WRITER, USER)
        createRoleIfNotExists("ADMIN", "Yönetici - Tüm yetkilere sahip");
        createRoleIfNotExists("WRITER", "Yazar - İçerik oluşturma yetkisi");
        createRoleIfNotExists("USER", "Kullanıcı - Okuma, beğeni, kayıt, liste oluşturma ve takip yetkisi");
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
}

