package com.expressservices.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String role; // "ROLE_ADMIN" or "ROLE_LIVREUR"
    private String nom;
    private String prenom;
    private String telephone;

    public RegisterRequest(String username, String password, String role, String nom, String prenom, String telephone) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
    }
}
