package com.koramarket.product.dto;

import lombok.Data;

@Data
public class ProductImageRequestDTO {
    private Long productId;
    private String urlImage;
    private Boolean isDefault; // facultatif : si null, false par défaut
}
