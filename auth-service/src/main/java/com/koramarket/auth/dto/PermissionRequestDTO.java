package com.koramarket.auth.dto;

import lombok.Data;

@Data
public class PermissionRequestDTO {
    private Long id;
    private String nom;
    private String description;
}
