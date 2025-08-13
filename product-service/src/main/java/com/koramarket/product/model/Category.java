package com.koramarket.product.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories", schema = "product_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(length = 255)
    private String description;

    // Pour gestion hiérarchique (catégories imbriquées)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private Set<Category> subCategories = new HashSet<>();

    // Option : Pour retrouver facilement le nombre de produits par catégorie
    // (mappedBy côté Product)
}
