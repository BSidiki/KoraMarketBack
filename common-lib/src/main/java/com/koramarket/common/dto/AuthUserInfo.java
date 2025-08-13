package com.koramarket.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * DTO pour propager l'utilisateur authentifié (JWT) dans les microservices.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserInfo {
    private Long id;
    private String email;
    private Set<String> roles;
    private String nom;
    private String prenom;

    public AuthUserInfo(String email, List<String> roles) {
        this.email = email;
        this.roles = Set.copyOf(roles);
    }
}
