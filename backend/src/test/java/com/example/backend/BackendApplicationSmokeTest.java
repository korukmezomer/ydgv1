package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class BackendApplicationSmokeTest {

    @Test
    void main_shouldStartWithoutExceptions() {
        // Spring Boot uygulamasını başlat ve hemen kapat
        // Veritabanı bağlantısı olmadan sadece main metodunu test et
        // DataSource ve JPA auto-configuration'ı devre dışı bırak
        System.setProperty("spring.autoconfigure.exclude", 
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
            "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration");
        
        String[] args = new String[]{
            "--spring.profiles.active=test",
            "--spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
            "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
        };
        
        ConfigurableApplicationContext context = null;
        try {
            context = SpringApplication.run(BackendApplication.class, args);
            // Uygulama başarıyla başlatıldı, şimdi kapat
        } finally {
            if (context != null) {
                context.close();
            }
            // System property'yi temizle
            System.clearProperty("spring.autoconfigure.exclude");
        }
    }
}


