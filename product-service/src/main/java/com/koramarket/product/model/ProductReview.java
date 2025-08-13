package com.koramarket.product.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "avis_clients", schema = "product_service")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Association forte avec Product
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id")
    private Product product;

    // Peut-être lié à un utilisateur distant (par userId simple ou User simplifié selon architecture)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer note; // 1 à 5 étoiles, on peut valider côté service

    @Column(length = 1000)
    private String commentaire;

    @Column(nullable = false)
    private LocalDateTime date;
}
