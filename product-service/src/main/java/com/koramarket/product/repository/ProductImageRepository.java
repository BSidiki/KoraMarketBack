package com.koramarket.product.repository;

import com.koramarket.product.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    Optional<ProductImage> findFirstByProduct_IdAndIsDefaultTrueOrderByIdAsc(Long productId);

    Optional<ProductImage> findFirstByProduct_IdOrderByIdAsc(Long productId);
    List<ProductImage> findByProductId(Long productId);
    List<ProductImage> findByProductIdAndIsDefaultTrue(Long productId);

    }