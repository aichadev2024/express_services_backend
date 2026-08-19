package com.expressservices.partenaire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartenaireRequest {
    private String nom;
    private String telephone;
}
