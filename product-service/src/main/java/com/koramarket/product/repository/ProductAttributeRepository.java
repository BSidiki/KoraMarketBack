package com.koramarket.product.repository;

import com.koramarket.product.model.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
    List<ProductAttribute> findByProductId(Long productId);
}

