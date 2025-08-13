package com.koramarket.product.model;

import com.koramarket.common.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "produits", schema = "product_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, scale = 2)
    private BigDecimal prix;

    @Column(nullable = false)
    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus statut; // ENUM : DISPONIBLE, INDISPONIBLE, EN_RUPTURE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "sku", length = 64, unique = true)
    private String sku;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "vendeur_email", length = 25, nullable = false)
    private String vendeurEmail;

    @Column(name = "vendeur_id", columnDefinition = "uuid")
    private UUID vendeurId;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
        if (this.statut == null) this.statut = ProductStatus.DISPONIBLE;
    }

    // Bonus : pour extension images, attributs, avis...
}
