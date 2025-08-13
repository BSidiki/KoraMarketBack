package com.koramarket.product.controller;

import com.koramarket.product.dto.ProductAttributeRequestDTO;
import com.koramarket.product.dto.ProductAttributeResponseDTO;
import com.koramarket.product.mappper.ProductAttributeMapper;
import com.koramarket.product.model.Product;
import com.koramarket.product.model.ProductAttribute;
import com.koramarket.product.service.ProductService;
import com.koramarket.product.service.ProductAttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product-attributes")
@RequiredArgsConstructor
public class ProductAttributeController {

    private final ProductAttributeService productAttributeService;
    private final ProductService productService;

    @GetMapping
    public List<ProductAttributeResponseDTO> getAll() {
        return productAttributeService.findAll().stream()
                .map(ProductAttributeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/by-product/{productId}")
    public List<ProductAttributeResponseDTO> getByProduct(@PathVariable Long productId) {
        return productAttributeService.findByProduct(productId).stream()
                .map(ProductAttributeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductAttributeResponseDTO> getById(@PathVariable Long id) {
        return productAttributeService.findById(id)
                .map(ProductAttributeMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductAttributeResponseDTO> create(@RequestBody ProductAttributeRequestDTO dto) {
        Optional<Product> optProduct = productService.findById(dto.getProductId());
        if (optProduct.isEmpty()) return ResponseEntity.badRequest().build();
        ProductAttribute saved = productAttributeService.save(
                ProductAttributeMapper.toEntity(dto, optProduct.get())
        );
        return ResponseEntity.ok(ProductAttributeMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductAttributeResponseDTO> update(@PathVariable Long id, @RequestBody ProductAttributeRequestDTO dto) {
        return productAttributeService.findById(id)
                .map(existing -> {
                    if (dto.getProductId() != null) {
                        productService.findById(dto.getProductId()).ifPresent(existing::setProduct);
                    }
                    existing.setAttribut(dto.getAttribut());
                    existing.setValeur(dto.getValeur());
                    ProductAttribute updated = productAttributeService.save(existing);
                    return ResponseEntity.ok(ProductAttributeMapper.toResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (productAttributeService.findById(id).isPresent()) {
            productAttributeService.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
