package com.koramarket.auth.dto;

import lombok.Data;

@Data
public class PermissionResponseDTO {
    private Long id;
    private String nom;
    private String description;
}
