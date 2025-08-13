package com.koramarket.product.dto;

import lombok.Data;

@Data
public class ProductAttributeRequestDTO {
    private Long productId;
    private String attribut;
    private String valeur;
}
