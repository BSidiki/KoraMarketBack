package com.koramarket.commande.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "commandes", schema = "commande_service")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDateTime commandeDate;

    @Enumerated(EnumType.STRING)
    private CommandeStatus status;

    private double totalAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommandeItem> items;

    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL)
    private Payment payment;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
