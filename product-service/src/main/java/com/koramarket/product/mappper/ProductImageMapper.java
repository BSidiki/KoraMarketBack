package com.koramarket.product.mappper;


import com.koramarket.product.dto.ProductImageRequestDTO;
import com.koramarket.product.dto.ProductImageResponseDTO;
import com.koramarket.product.model.Product;
import com.koramarket.product.model.ProductImage;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProductImageMapper {

    public static ProductImage toEntity(ProductImageRequestDTO dto, Product product) {
        return ProductImage.builder()
                .product(product)
                .urlImage(dto.getUrlImage())
                .isDefault(dto.getIsDefault() != null && dto.getIsDefault())
                .build();
    }

    public static ProductImageResponseDTO toResponse(ProductImage pi) {
        ProductImageResponseDTO dto = new ProductImageResponseDTO();
        dto.setId(pi.getId());
        dto.setProductId(pi.getProduct() != null ? pi.getProduct().getId() : null);
        dto.setUrlImage(pi.getUrlImage());
        dto.setIsDefault(pi.isDefault());
        return dto;
    }
}
