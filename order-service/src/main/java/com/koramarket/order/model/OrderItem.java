package com.koramarket.order.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(
        name = "order_items",
        indexes = {
                @Index(name = "idx_orderitem_order_id", columnList = "order_id"),
                @Index(name = "idx_orderitem_product_id_ext", columnList = "product_id_ext")
        }
)
public class OrderItem {

    @Id
    @Column(columnDefinition = "uuid")
    @EqualsAndHashCode.Include @ToString.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="order_id", nullable=false)
    @ToString.Exclude
    private Order order;

    /** Identifiant technique du produit depuis product-service (numérique). */
    @NotNull
    @Column(name = "product_id_ext", nullable = false)
    private Long productIdExt;

    /** Snapshots produit (dénormalisés pour l’historique) */
    @NotNull
    @Column(name="product_name_snap", nullable=false)
    private String productNameSnap;

    @Column(name="product_sku_snap")
    private String productSkuSnap;

    @Column(name="product_image_snap")
    private String productImageSnap;

    /** Vendeur (si connu) */
    @Column(name="vendor_id_ext", columnDefinition = "uuid")
    private UUID vendorIdExt;

    @Column(name="vendor_email_snap")
    private String vendorEmailSnap;

    /** Montants en centimes */
    @NotNull
    @Column(name="unit_price_amount", nullable=false)
    private Long unitPriceAmount;

    @NotNull @Min(1)
    @Column(nullable=false)
    private Integer quantity;

    /** Taux de taxe (ex: 18.00 pour 18%) */
    @Column(name="tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    @NotNull
    @Column(name="tax_amount", nullable=false)
    private Long taxAmount;

    @NotNull
    @Column(name="line_total_amount", nullable=false)
    private Long lineTotalAmount;

    /** Sécurise les champs obligatoires et l’ID */
    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();

        // Garde-fous : on préfère une erreur claire ici plutôt qu’un NULL en DB
        Objects.requireNonNull(order, "order (FK) ne doit pas être null");
        Objects.requireNonNull(productIdExt, "productIdExt ne doit pas être null");
        Objects.requireNonNull(unitPriceAmount, "unitPriceAmount ne doit pas être null");
        Objects.requireNonNull(quantity, "quantity ne doit pas être null");

        // Valeurs par défaut pour éviter toute contrainte NOT NULL
        if (taxAmount == null) taxAmount = 0L;
        if (line_total_amount_missing()) {
            // à défaut, on calcule lineTotal = unitPrice * qty (+ taxAmount si déjà fourni)
            long lt = unitPriceAmount * quantity;
            lineTotalAmount = (taxAmount != null) ? lt + taxAmount : lt;
        }
    }

    private boolean line_total_amount_missing() {
        return lineTotalAmount == null;
    }
}
