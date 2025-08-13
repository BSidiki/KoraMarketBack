package com.koramarket.product.mappper;

import com.koramarket.product.dto.ProductAttributeRequestDTO;
import com.koramarket.product.dto.ProductAttributeResponseDTO;
import com.koramarket.product.model.Product;
import com.koramarket.product.model.ProductAttribute;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProductAttributeMapper {

    public static ProductAttribute toEntity(ProductAttributeRequestDTO dto, Product product) {
        return ProductAttribute.builder()
                .product(product)
                .attribut(dto.getAttribut())
                .valeur(dto.getValeur())
                .build();
    }

    public static ProductAttributeResponseDTO toResponse(ProductAttribute pa) {
        ProductAttributeResponseDTO dto = new ProductAttributeResponseDTO();
        dto.setId(pa.getId());
        dto.setProductId(pa.getProduct() != null ? pa.getProduct().getId() : null);
        dto.setAttribut(pa.getAttribut());
        dto.setValeur(pa.getValeur());
        return dto;
    }
}
