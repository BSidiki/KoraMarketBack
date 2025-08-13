package com.koramarket.product.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductReviewResponseDTO {
    private Long id;
    private Long productId;
    private Long userId;
    private Integer note;
    private String commentaire;
    private LocalDateTime date;
}
