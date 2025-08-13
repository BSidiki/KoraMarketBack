package com.koramarket.product.controller;

import com.koramarket.common.enums.ProductStatus;
import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.product.dto.ProductRequestDTO;
import com.koramarket.product.dto.ProductResponseDTO;
import com.koramarket.product.mappper.ProductMapper;
import com.koramarket.product.model.Category;
import com.koramarket.product.model.Product;
import com.koramarket.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /* =========================
       Helpers
       ========================= */
    private static ProductStatus parseStatusOrThrow(String raw) {
        try {
            return ProductStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BusinessException("Statut produit invalide: " + raw
                    + " (valeurs acceptées: " + Arrays.toString(ProductStatus.values()) + ")");
        }
    }

    private static String currentUserEmailOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("Authentification requise");
        }
        // Par défaut, getName() = username/email dans la plupart des setups
        return auth.getName();
    }

    /* =========================
       Public GET
       ========================= */

    @GetMapping
    public List<ProductResponseDTO> getAllProducts() {
        // Liste simple (non enrichie avec image/sku). OK pour catalogue / recherche.
        return productService.findAll().stream()
                .map(ProductMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        // Endpoint ENRICHI (imageUrl/sku) – implémenté dans le service
        // Renvoie 404 si introuvable via IllegalArgumentException -> transformable en 404 par @ControllerAdvice
        try {
            return ResponseEntity.ok(productService.getOne(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-category/{categoryId}")
    public List<ProductResponseDTO> getByCategory(@PathVariable Long categoryId) {
        return productService.findByCategory(categoryId).stream()
                .map(ProductMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/by-status/{status}")
    public List<ProductResponseDTO> getByStatus(@PathVariable String status) {
        ProductStatus s = parseStatusOrThrow(status);
        return productService.findByStatus(s).stream()
                .map(ProductMapper::toResponse)
                .collect(Collectors.toList());
    }

    /* =========================
       Protégés (roles/perms vus par SecurityConfig)
       ========================= */

    @GetMapping("/my-products")
    public List<ProductResponseDTO> getProductsOfCurrentVendeur() {
        String email = currentUserEmailOrThrow();
        return productService.findByVendeurEmail(email).stream()
                .map(ProductMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody ProductRequestDTO dto) {
        // Résolution éventuelle de la catégorie (le service revalide aussi)
        Category category = null;
        if (dto.getCategoryId() != null) {
            category = productService.findCategory(dto.getCategoryId())
                    .orElseThrow(() -> new BusinessException("Catégorie introuvable: " + dto.getCategoryId()));
        }

        Product entity = ProductMapper.toEntity(dto, category);
        // Le service forcera vendeurEmail = utilisateur courant + vérifiera les permissions
        Product saved = productService.save(entity);
        return ResponseEntity.ok(ProductMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id,
                                                            @RequestBody ProductRequestDTO dto) {
        Category category = null;
        if (dto.getCategoryId() != null) {
            category = productService.findCategory(dto.getCategoryId())
                    .orElseThrow(() -> new BusinessException("Catégorie introuvable: " + dto.getCategoryId()));
        }

        Product patch = ProductMapper.toEntity(dto, category);
        Product updated = productService.updateProduct(id, patch);
        return ResponseEntity.ok(ProductMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok().build();
    }
}
