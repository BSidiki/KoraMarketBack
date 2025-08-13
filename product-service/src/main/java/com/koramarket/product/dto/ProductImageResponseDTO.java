package com.koramarket.product.dto;

import lombok.Data;

@Data
public class ProductImageResponseDTO {
    private Long id;
    private Long productId;
    private String urlImage;
    private Boolean isDefault;
}
