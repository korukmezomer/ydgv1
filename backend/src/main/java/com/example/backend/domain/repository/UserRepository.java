package com.example.backend.domain.repository;

import com.example.backend.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveByEmail(@Param("email") String email);
    
    @Query(value = "SELECT * FROM kullanicilar ORDER BY id DESC", 
           nativeQuery = true,
           countQuery = "SELECT COUNT(*) FROM kullanicilar")
    Page<User> findAllOrderedByIdDesc(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.email) LIKE LOWER(:search) OR " +
           "LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(u.username, 'ö', 'o'), 'Ö', 'O'), 'ş', 's'), 'Ş', 'S'), 'ı', 'i'), 'İ', 'I'), 'ü', 'u'), 'Ü', 'U')) LIKE LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(:search, 'ö', 'o'), 'Ö', 'O'), 'ş', 's'), 'Ş', 'S'), 'ı', 'i'), 'İ', 'I'), 'ü', 'u'), 'Ü', 'U')) OR " +
           "LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(u.firstName, 'ö', 'o'), 'Ö', 'O'), 'ş', 's'), 'Ş', 'S'), 'ı', 'i'), 'İ', 'I'), 'ü', 'u'), 'Ü', 'U')) LIKE LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(:search, 'ö', 'o'), 'Ö', 'O'), 'ş', 's'), 'Ş', 'S'), 'ı', 'i'), 'İ', 'I'), 'ü', 'u'), 'Ü', 'U')) OR " +
           "LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(u.lastName, 'ö', 'o'), 'Ö', 'O'), 'ş', 's'), 'Ş', 'S'), 'ı', 'i'), 'İ', 'I'), 'ü', 'u'), 'Ü', 'U')) LIKE LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(:search, 'ö', 'o'), 'Ö', 'O'), 'ş', 's'), 'Ş', 'S'), 'ı', 'i'), 'İ', 'I'), 'ü', 'u'), 'Ü', 'U')) OR " +
           "LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(CONCAT(u.firstName, ' ', u.lastName), 'ö', 'o'), 'Ö', 'O'), 'ş', 's'), 'Ş', 'S'), 'ı', 'i'), 'İ', 'I'), 'ü', 'u'), 'Ü', 'U')) LIKE LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(:search, 'ö', 'o'), 'Ö', 'O'), 'ş', 's'), 'Ş', 'S'), 'ı', 'i'), 'İ', 'I'), 'ü', 'u'), 'Ü', 'U'))) " +
           "AND u.isActive = true ORDER BY u.id DESC")
    Page<User> search(@Param("search") String search, Pageable pageable);
}

