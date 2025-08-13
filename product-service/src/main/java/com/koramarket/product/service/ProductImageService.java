package com.koramarket.product.service;

import com.koramarket.product.model.ProductImage;
import com.koramarket.product.repository.ProductImageRepository;
import com.koramarket.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    public List<ProductImage> findAll() {
        return productImageRepository.findAll();
    }

    public Optional<ProductImage> findById(Long id) {
        return productImageRepository.findById(id);
    }

    public List<ProductImage> findByProduct(Long productId) {
        return productImageRepository.findByProductId(productId);
    }

    public ProductImage save(ProductImage productImage) {
        // Innovation : on force une seule image par défaut par produit
        if (productImage.isDefault() && productImage.getProduct() != null) {
            List<ProductImage> existings = productImageRepository.findByProductIdAndIsDefaultTrue(productImage.getProduct().getId());
            for (ProductImage img : existings) {
                if (!Objects.equals(img.getId(), productImage.getId())) {
                    img.setDefault(false);
                    productImageRepository.save(img);
                }
            }
        }
        return productImageRepository.save(productImage);
    }

    public void delete(Long id) {
        productImageRepository.deleteById(id);
    }
}
