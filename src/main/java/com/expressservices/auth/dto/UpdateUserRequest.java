package com.expressservices.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    
    // Le mot de passe est optionnel en modification
    private String password;
}
