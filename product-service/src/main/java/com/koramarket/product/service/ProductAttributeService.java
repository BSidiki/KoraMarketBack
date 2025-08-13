package com.koramarket.product.service;

import com.koramarket.product.model.ProductAttribute;
import com.koramarket.product.repository.ProductAttributeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductAttributeService {

    private final ProductAttributeRepository productAttributeRepository;

    public List<ProductAttribute> findAll() {
        return productAttributeRepository.findAll();
    }

    public Optional<ProductAttribute> findById(Long id) {
        return productAttributeRepository.findById(id);
    }

    public List<ProductAttribute> findByProduct(Long productId) {
        return productAttributeRepository.findByProductId(productId);
    }

    public ProductAttribute save(ProductAttribute attribute) {
        return productAttributeRepository.save(attribute);
    }

    public void delete(Long id) {
        productAttributeRepository.deleteById(id);
    }
}
