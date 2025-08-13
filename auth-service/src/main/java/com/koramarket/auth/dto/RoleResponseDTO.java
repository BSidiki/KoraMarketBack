package com.koramarket.auth.dto;

import lombok.Data;
import java.util.Set;

@Data
public class RoleResponseDTO {
    private Long id;
    private String nom;
    private String description;
    private Set<String> permissions;
}
