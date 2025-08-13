package com.koramarket.product.controller;

import com.koramarket.product.dto.ProductImageRequestDTO;
import com.koramarket.product.dto.ProductImageResponseDTO;
import com.koramarket.product.mappper.ProductImageMapper;
import com.koramarket.product.model.Product;
import com.koramarket.product.model.ProductImage;
import com.koramarket.product.service.ProductImageService;
import com.koramarket.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;
    private final ProductService productService;

    @GetMapping
    public List<ProductImageResponseDTO> getAllImages() {
        return productImageService.findAll().stream()
                .map(ProductImageMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/by-product/{productId}")
    public List<ProductImageResponseDTO> getByProduct(@PathVariable Long productId) {
        return productImageService.findByProduct(productId).stream()
                .map(ProductImageMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductImageResponseDTO> getById(@PathVariable Long id) {
        return productImageService.findById(id)
                .map(ProductImageMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductImageResponseDTO> create(@RequestBody ProductImageRequestDTO dto) {
        Optional<Product> optProduct = productService.findById(dto.getProductId());
        if (optProduct.isEmpty()) return ResponseEntity.badRequest().build();
        ProductImage saved = productImageService.save(
                ProductImageMapper.toEntity(dto, optProduct.get())
        );
        return ResponseEntity.ok(ProductImageMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductImageResponseDTO> update(@PathVariable Long id, @RequestBody ProductImageRequestDTO dto) {
        return productImageService.findById(id)
                .map(existing -> {
                    if (dto.getProductId() != null) {
                        productService.findById(dto.getProductId()).ifPresent(existing::setProduct);
                    }
                    existing.setUrlImage(dto.getUrlImage());
                    if (dto.getIsDefault() != null) existing.setDefault(dto.getIsDefault());
                    ProductImage updated = productImageService.save(existing);
                    return ResponseEntity.ok(ProductImageMapper.toResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (productImageService.findById(id).isPresent()) {
            productImageService.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
