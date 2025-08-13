package com.koramarket.product.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "produit_images", schema = "product_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id")
    private Product product;

    @Column(nullable = false, length = 500)
    private String urlImage;

    @Column(nullable = false)
    private boolean isDefault;
}
