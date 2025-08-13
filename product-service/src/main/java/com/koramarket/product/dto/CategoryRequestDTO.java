package com.koramarket.product.dto;

import lombok.Data;

@Data
public class CategoryRequestDTO {
    private String nom;
    private String description;
    private Long parentId; // nullable si racine
}
