package com.koramarket.product.repository;

import com.koramarket.common.enums.ProductStatus;
import com.koramarket.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByStatut(ProductStatus statut);

    // Recherche de tous les produits créés par un vendeur donné (par email)
    List<Product> findByVendeurEmail(String vendeurEmail);

    // Exemples d'autres requêtes
    List<Product> findByNomContainingIgnoreCase(String nom);

    @Modifying
    @Query("update Product p set p.stock = p.stock - :qty where p.id = :productId and p.stock >= :qty")
    int decrementIfEnough(@Param("productId") Long productId, @Param("qty") int qty);

    @Modifying
    @Query("update Product p set p.stock = p.stock + :qty where p.id = :productId")
    int increment(@Param("productId") Long productId, @Param("qty") int qty);
}
