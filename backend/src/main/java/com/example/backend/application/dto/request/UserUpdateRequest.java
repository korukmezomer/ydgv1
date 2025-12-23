package com.example.backend.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    
    @NotBlank(message = "Email zorunludur")
    @Email(message = "Geçerli bir email adresi giriniz")
    private String email;
    
    // Password optional for updates
    @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır")
    private String password;
    
    @NotBlank(message = "Ad zorunludur")
    @Size(max = 50, message = "Ad en fazla 50 karakter olabilir")
    private String firstName;
    
    @Size(max = 50, message = "Soyad en fazla 50 karakter olabilir")
    private String lastName;
    
    @Size(max = 50, message = "Kullanıcı adı en fazla 50 karakter olabilir")
    private String username;
}

