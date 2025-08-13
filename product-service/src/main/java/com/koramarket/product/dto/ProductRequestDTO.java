package com.koramarket.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequestDTO {
    private String nom;
    private String description;
    private BigDecimal prix;
    private Integer stock;
    private String statut; // Optionnel en création, default DISPONIBLE
    private Long categoryId;
}
