package com.koramarket.product.dto;

import lombok.Data;

@Data
public class ProductAttributeResponseDTO {
    private Long id;
    private Long productId;
    private String attribut;
    private String valeur;
}
