package com.koramarket.product.repository;

import com.koramarket.product.model.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    List<ProductReview> findByProductId(Long productId);
    List<ProductReview> findByUserId(Long userId);

    @Query("SELECT AVG(pr.note) FROM ProductReview pr WHERE pr.product.id = :productId")
    Double getAverageNoteByProductId(Long productId);

    Long countByProductId(Long productId);

    // Avis négatifs (note <= 2)
    List<ProductReview> findByProductIdAndNoteLessThanEqual(Long productId, Integer noteMax);

    // 5 derniers avis pour un produit
    List<ProductReview> findTop5ByProductIdOrderByDateDesc(Long productId);

    // Répartition des notes (compte chaque note possible)
    @Query("SELECT pr.note, COUNT(pr) FROM ProductReview pr WHERE pr.product.id = :productId GROUP BY pr.note")
    List<Object[]> countNotesByProductId(@Param("productId") Long productId);

    // Dernier avis de chaque utilisateur sur un produit (bonus)
    @Query("SELECT pr FROM ProductReview pr WHERE pr.product.id = :productId AND pr.date = " +
            "(SELECT MAX(pr2.date) FROM ProductReview pr2 WHERE pr2.product.id = :productId AND pr2.userId = pr.userId)")
    List<ProductReview> findLastReviewByUserForProduct(@Param("productId") Long productId);
}
