package com.koramarket.product.dto;

import lombok.Data;
import java.util.Set;

@Data
public class CategoryResponseDTO {
    private Long id;
    private String nom;
    private String description;
    private Long parentId;
    private String parentNom; // Pour affichage simple
    private Set<Long> subCategoryIds; // Pour navigation rapide
}
