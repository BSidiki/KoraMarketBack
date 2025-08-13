package com.koramarket.product.repository;

import com.koramarket.product.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNom(String nom);

    // Pour retrouver toutes les sous-catégories racines
    List<Category> findByParentIsNull();
}
