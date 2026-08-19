package com.expressservices.partenaire.dto;

import com.expressservices.partenaire.model.Partenaire;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartenaireResponse {
    private Long id;
    private String nom;
    private String telephone;

    public static PartenaireResponse fromEntity(Partenaire partenaire) {
        return PartenaireResponse.builder()
                .id(partenaire.getId())
                .nom(partenaire.getNom())
                .telephone(partenaire.getTelephone())
                .build();
    }
}
