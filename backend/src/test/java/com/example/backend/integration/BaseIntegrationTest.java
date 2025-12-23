package com.example.backend.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for integration tests.
 * Uses separate PostgreSQL test database (yazilimdogrulama_test).
 * Make sure the test database exists before running tests.
 * 
 * Database connection can be overridden via system properties:
 * - spring.datasource.url
 * - spring.datasource.username
 * - spring.datasource.password
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    /**
     * Dynamically configure datasource properties.
     * System properties (from Maven -D flags) take precedence over defaults.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Database URL - use system property if provided, otherwise use default from properties file
        String dbUrl = System.getProperty("spring.datasource.url", 
            "jdbc:postgresql://localhost:5433/yazilimdogrulama_test");
        registry.add("spring.datasource.url", () -> dbUrl);
        
        // Database username - use system property if provided
        String dbUser = System.getProperty("spring.datasource.username", "postgres");
        registry.add("spring.datasource.username", () -> dbUser);
        
        // Database password - use system property if provided
        String dbPassword = System.getProperty("spring.datasource.password", "postgres");
        registry.add("spring.datasource.password", () -> dbPassword);
        
        // Other required properties
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
        registry.add("jwt.secret", () -> "test-secret-key-for-integration-tests-minimum-256-bits-required-here");
        registry.add("jwt.expiration", () -> "86400000");
        registry.add("file.upload-dir", () -> "test-uploads");
        registry.add("spring.servlet.multipart.max-file-size", () -> "10MB");
        registry.add("spring.servlet.multipart.max-request-size", () -> "10MB");
        registry.add("server.port", () -> "0");
    }
}

