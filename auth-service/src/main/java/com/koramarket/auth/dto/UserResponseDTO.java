package com.koramarket.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponseDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String statut;
    private LocalDateTime dateInscription;
    private LocalDateTime lastLogin;
    private Set<String> roles;
}
