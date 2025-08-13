package com.koramarket.commande.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "commande_items", schema = "commande_service")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id")
    private Commande commande;
}
