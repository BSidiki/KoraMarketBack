package com.koramarket.auth.dto;

import lombok.Data;

@Data
public class UserRequestDTO {
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private String statut;
}
