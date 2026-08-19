package com.expressservices.auth.dto;

import com.expressservices.auth.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String nom;
    private String prenom;
    private String role;
    private String photoUrl;
    private LocalDateTime dateCreation;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .role(user.getRole().name())
                .photoUrl(user.getPhotoUrl())
                .dateCreation(user.getDateCreation())
                .build();
    }
}
