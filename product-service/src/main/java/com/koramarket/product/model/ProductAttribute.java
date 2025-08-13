package com.koramarket.product.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attributs_produits", schema = "product_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Association forte avec Product
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id")
    private Product product;

    @Column(nullable = false, length = 100)
    private String attribut;

    @Column(nullable = false, length = 250)
    private String valeur;
}
