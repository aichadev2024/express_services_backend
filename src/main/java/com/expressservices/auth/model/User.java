package com.expressservices.auth.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_utilisateur", unique = true, nullable = false)
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    private String password;

    private String nom;

    private String prenom;

    @Column(name = "telephone")
    private String telephone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "first_login", nullable = false)
    @Builder.Default
    private boolean firstLogin = false;

    @Column(name = "otp_code")
    private String otpCode;

    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;

    @Column(name = "otp_verified", nullable = false)
    @Builder.Default
    private boolean otpVerified = false;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}
