package com.koramarket.product.repository;

import com.koramarket.product.model.Product;
import com.koramarket.common.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByStatut(ProductStatus statut);

    // Recherche de tous les produits créés par un vendeur donné (par email)
    List<Product> findByVendeurEmail(String vendeurEmail);

    // Exemples d'autres requêtes
    List<Product> findByNomContainingIgnoreCase(String nom);
}
