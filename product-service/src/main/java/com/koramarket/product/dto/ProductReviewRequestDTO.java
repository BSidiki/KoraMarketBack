package com.koramarket.product.dto;

import lombok.Data;

@Data
public class ProductReviewRequestDTO {
    private Long productId;
    private Long userId;
    private Integer note;
    private String commentaire;
}
