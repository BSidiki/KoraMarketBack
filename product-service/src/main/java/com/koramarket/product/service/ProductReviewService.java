package com.koramarket.product.service;

import com.koramarket.product.model.ProductReview;
import com.koramarket.product.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductReviewService {

    private final ProductReviewRepository productReviewRepository;

    public List<ProductReview> findAll() {
        return productReviewRepository.findAll();
    }

    public Optional<ProductReview> findById(Long id) {
        return productReviewRepository.findById(id);
    }

    public List<ProductReview> findByProduct(Long productId) {
        return productReviewRepository.findByProductId(productId);
    }

    public List<ProductReview> findByUser(Long userId) {
        return productReviewRepository.findByUserId(userId);
    }

    public ProductReview save(ProductReview review) {
        return productReviewRepository.save(review);
    }

    public void delete(Long id) {
        productReviewRepository.deleteById(id);
    }

    // Statistique : moyenne des notes pour un produit
    public Double getAverageNoteForProduct(Long productId) {
        Double avg = productReviewRepository.getAverageNoteByProductId(productId);
        // Pour éviter les null pointer si aucun avis
        return avg != null ? Math.round(avg * 100.0) / 100.0 : 0.0;
    }

    // Statistique : nombre d’avis pour un produit
    public Long getReviewCountForProduct(Long productId) {
        return productReviewRepository.countByProductId(productId);
    }

    // Avis négatifs
    public List<ProductReview> getNegativeReviews(Long productId, int noteMax) {
        return productReviewRepository.findByProductIdAndNoteLessThanEqual(productId, noteMax);
    }

    // Derniers avis (ex: top 5)
    public List<ProductReview> getLastReviews(Long productId, int count) {
        // Le repository retourne déjà top 5 mais tu peux rendre ça paramétrable
        return productReviewRepository.findTop5ByProductIdOrderByDateDesc(productId);
    }

    // Tous les avis d'un utilisateur
    public List<ProductReview> getReviewsByUser(Long userId) {
        return productReviewRepository.findByUserId(userId);
    }

    // Répartition des notes (map: note -> nombre)
    public Map<Integer, Long> getNoteDistribution(Long productId) {
        List<Object[]> results = productReviewRepository.countNotesByProductId(productId);
        Map<Integer, Long> distribution = new HashMap<>();
        for (Object[] row : results) {
            Integer note = (Integer) row[0];
            Long count = (Long) row[1];
            distribution.put(note, count);
        }
        return distribution;
    }

    // Dernier avis par utilisateur sur un produit
    public List<ProductReview> getLastReviewByUserForProduct(Long productId) {
        return productReviewRepository.findLastReviewByUserForProduct(productId);
    }
}
