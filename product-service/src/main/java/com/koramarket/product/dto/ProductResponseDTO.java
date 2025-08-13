package com.koramarket.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductResponseDTO {
    private Long id;
    private String nom;
    private String description;
    private BigDecimal prix;
    private Integer stock;
    private String statut;
    private Long categoryId;
    private String categoryNom;
    private LocalDateTime dateCreation;
    private String vendeurEmail;
    private UUID vendeurId;

    // 👇 NOUVEAUX CHAMPS
    private String imageUrl;   // URL de l’image par défaut
    private String sku;        // optionnel si tu as ce champ dans Product
}
