package com.koramarket.product.mappper;
import com.koramarket.product.dto.ProductReviewRequestDTO;
import com.koramarket.product.dto.ProductReviewResponseDTO;
import com.koramarket.product.model.Product;
import com.koramarket.product.model.ProductReview;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public class ProductReviewMapper {

    public static ProductReview toEntity(ProductReviewRequestDTO dto, Product product) {
        return ProductReview.builder()
                .product(product)
                .userId(dto.getUserId())
                .note(dto.getNote())
                .commentaire(dto.getCommentaire())
                .date(LocalDateTime.now())
                .build();
    }

    public static ProductReviewResponseDTO toResponse(ProductReview review) {
        ProductReviewResponseDTO dto = new ProductReviewResponseDTO();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct() != null ? review.getProduct().getId() : null);
        dto.setUserId(review.getUserId());
        dto.setNote(review.getNote());
        dto.setCommentaire(review.getCommentaire());
        dto.setDate(review.getDate());
        return dto;
    }
}
