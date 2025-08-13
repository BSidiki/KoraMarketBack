package com.koramarket.product.controller;
import com.koramarket.product.dto.ProductReviewRequestDTO;
import com.koramarket.product.dto.ProductReviewResponseDTO;
import com.koramarket.product.mappper.ProductReviewMapper;
import com.koramarket.product.model.Product;
import com.koramarket.product.model.ProductReview;
import com.koramarket.product.service.ProductService;
import com.koramarket.product.service.ProductReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product-reviews")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService productReviewService;
    private final ProductService productService;

    @GetMapping
    public List<ProductReviewResponseDTO> getAll() {
        return productReviewService.findAll().stream()
                .map(ProductReviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/by-product/{productId}")
    public List<ProductReviewResponseDTO> getByProduct(@PathVariable Long productId) {
        return productReviewService.findByProduct(productId).stream()
                .map(ProductReviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/by-user/{userId}")
    public List<ProductReviewResponseDTO> getByUser(@PathVariable Long userId) {
        return productReviewService.findByUser(userId).stream()
                .map(ProductReviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductReviewResponseDTO> getById(@PathVariable Long id) {
        return productReviewService.findById(id)
                .map(ProductReviewMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductReviewResponseDTO> create(@RequestBody ProductReviewRequestDTO dto) {
        // Valider la note côté back (1-5)
        if (dto.getNote() == null || dto.getNote() < 1 || dto.getNote() > 5) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Product> optProduct = productService.findById(dto.getProductId());
        if (optProduct.isEmpty()) return ResponseEntity.badRequest().build();
        ProductReview saved = productReviewService.save(
                ProductReviewMapper.toEntity(dto, optProduct.get())
        );
        return ResponseEntity.ok(ProductReviewMapper.toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (productReviewService.findById(id).isPresent()) {
            productReviewService.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Endpoint : moyenne des notes
    @GetMapping("/average/{productId}")
    public ResponseEntity<Double> getAverageNote(@PathVariable Long productId) {
        double avg = productReviewService.getAverageNoteForProduct(productId);
        return ResponseEntity.ok(avg);
    }

    // Endpoint : nombre d’avis
    @GetMapping("/count/{productId}")
    public ResponseEntity<Long> getReviewCount(@PathVariable Long productId) {
        long count = productReviewService.getReviewCountForProduct(productId);
        return ResponseEntity.ok(count);
    }

    // Récupérer les avis négatifs
    @GetMapping("/negative/{productId}")
    public List<ProductReviewResponseDTO> getNegativeReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "2") int noteMax // ou 3 selon ta politique
    ) {
        return productReviewService.getNegativeReviews(productId, noteMax)
                .stream()
                .map(ProductReviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Derniers avis (par ex. top 5)
    @GetMapping("/last/{productId}")
    public List<ProductReviewResponseDTO> getLastReviews(@PathVariable Long productId) {
        return productReviewService.getLastReviews(productId, 5)
                .stream()
                .map(ProductReviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Tous les avis d'un utilisateur
    @GetMapping("/user/{userId}")
    public List<ProductReviewResponseDTO> getReviewsByUser(@PathVariable Long userId) {
        return productReviewService.getReviewsByUser(userId)
                .stream()
                .map(ProductReviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Répartition des notes (ex: {5: 10, 4: 8, ...})
    @GetMapping("/distribution/{productId}")
    public Map<Integer, Long> getNoteDistribution(@PathVariable Long productId) {
        return productReviewService.getNoteDistribution(productId);
    }

    // Dernier avis laissé par chaque utilisateur sur un produit
    @GetMapping("/last-by-user/{productId}")
    public List<ProductReviewResponseDTO> getLastReviewByUser(@PathVariable Long productId) {
        return productReviewService.getLastReviewByUserForProduct(productId)
                .stream()
                .map(ProductReviewMapper::toResponse)
                .collect(Collectors.toList());
    }
}
