package com.koramarket.order.service;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.client.ProductClient;
import com.koramarket.order.dto.OrderRequestDTO;
import com.koramarket.order.dto.OrderResponseDTO;
import com.koramarket.order.mapper.OrderMapper;
import com.koramarket.order.model.Order;
import com.koramarket.order.model.OrderItem;
import com.koramarket.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Set<String> SELLABLE = Set.of("ACTIVE", "DISPONIBLE", "AVAILABLE");

    private final OrderRepository orderRepo;
    private final ProductClient productClient;

    @Value("${order.tax.default-rate:0}")       // ex: 18.0 = 18%
    private BigDecimal defaultTaxRate;

    @Transactional
    public OrderResponseDTO create(OrderRequestDTO req, UUID userIdExt, String idemKey) {
        // --- Validations d’entrée ---
        if (userIdExt == null) throw new BusinessException("Identifiant utilisateur manquant");
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new BusinessException("La commande doit contenir au moins un article");
        }

        // Idempotence: si la clé existe déjà, renvoyer l’ordre existant
        String trimmedKey = (idemKey == null || idemKey.isBlank()) ? null : idemKey.trim();
        if (trimmedKey != null) {
            var existing = orderRepo.findByIdempotencyKey(trimmedKey);
            if (existing.isPresent()) return OrderMapper.toResponse(existing.get());
        }

        // --- Entête ---
        Order o = Order.builder()
                .id(UUID.randomUUID())
                .orderNumber(generateOrderNumber())
                .userIdExt(userIdExt)
                .currency(safeCurrency(req.getCurrency()))
                .orderStatus("PENDING")
                .paymentStatus("AWAITING_PAYMENT")
                .subtotalAmount(0L)
                .taxTotalAmount(0L)
                .shippingTotalAmount(0L)
                .discountTotalAmount(0L)
                .grandTotalAmount(0L)
                .idempotencyKey(trimmedKey) // ⚠️ mettre AVANT le 1er save
                .build();

        long subtotal = 0L;
        long taxTotal = 0L;
        List<OrderItem> items = new ArrayList<>();

        for (var it : req.getItems()) {
            // 1) Lookup produit
            var p = productClient.getById(it.getProductId());
            if (p == null) {
                throw new BusinessException("Produit introuvable: " + it.getProductId());
            }

            // 2) Disponibilité & stock
            String status = normalize(p.getStatut());
            if (!SELLABLE.contains(status)) {
                throw new BusinessException("Produit non disponible: " + p.getNom()
                        + " (statut reçu: " + p.getStatut() + ")");
            }
            if (p.getStock() != null && p.getStock() < it.getQuantity()) {
                throw new BusinessException("Stock insuffisant pour: " + p.getNom()
                        + " (dispo: " + p.getStock() + ", demandé: " + it.getQuantity() + ")");
            }

            // 3) Prix & totaux
            long unitCents = toCents(p.getPrix());                  // BigDecimal -> centimes
            long lineNet   = mulExact(unitCents, it.getQuantity()); // protège overflow
            long tax       = calcTax(lineNet, defaultTaxRate);

            // 4) Ligne (snapshots)
            OrderItem item = OrderItem.builder()
                    .id(UUID.randomUUID())
                    .productIdExt(it.getProductId())
                    .productNameSnap(p.getNom())
                    .productSkuSnap(nvlBlank(p.preferredSku(), "PRD-" + it.getProductId()))
                    .productImageSnap(nvlBlank(p.preferredImage(), null)) // null si manquante
                    .vendorEmailSnap(p.getVendeurEmail())
                    .vendorIdExt(p.getVendeurId())
                    .unitPriceAmount(unitCents)
                    .quantity(it.getQuantity())
                    .taxAmount(tax)
                    .lineTotalAmount(lineNet + tax)
                    .build();

            item.setOrder(o);   // FK non nulle
            items.add(item);

            subtotal += lineNet;
            taxTotal += tax;
        }

        // 5) Totaux commande
        o.setItems(items);
        o.setSubtotalAmount(subtotal);
        o.setTaxTotalAmount(taxTotal);
        o.setGrandTotalAmount(subtotal + taxTotal + o.getShippingTotalAmount() - o.getDiscountTotalAmount());

        // 6) Persist & idempotence forte
        try {
            o = orderRepo.save(o); // unique index sur idempotency_key protège la 2e requête
        } catch (DataIntegrityViolationException e) {
            // Collision d’idempotence: renvoyer l’existante
            if (trimmedKey != null) {
                return orderRepo.findByIdempotencyKey(trimmedKey)
                        .map(OrderMapper::toResponse)
                        .orElseThrow(() -> e);
            }
            throw e;
        }
        return OrderMapper.toResponse(o);
    }

    private String generateOrderNumber() {
        return "KOR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> myOrders(UUID userIdExt) {
        return orderRepo.findByUserIdExtOrderByCreatedAtDesc(userIdExt)
                .stream().map(OrderMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Optional<OrderResponseDTO> findOne(UUID id) {
        return orderRepo.findOneWithItems(id).map(OrderMapper::toResponse);
    }

    @Transactional
    public void cancelOwn(UUID id, UUID userIdExt, boolean isAdminOrAny) {
        Order o = orderRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Commande introuvable"));

        if (!isAdminOrAny && !o.getUserIdExt().equals(userIdExt)) {
            throw new BusinessException("Vous ne pouvez annuler que vos propres commandes");
        }
        if (!List.of("PENDING", "AWAITING_PAYMENT", "PAID").contains(o.getOrderStatus())) {
            throw new BusinessException("Commande non annulable à ce stade");
        }
        o.setOrderStatus("CANCELED");
    }

    /* -------------------- Helpers -------------------- */

    private static String safeCurrency(String cur) {
        String v = (cur == null ? "XOF" : cur.trim());
        if (v.length() != 3) return "XOF";
        return v.toUpperCase(Locale.ROOT);
    }

    private static String normalize(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
    }

    private static long toCents(BigDecimal amount) {
        if (amount == null) return 0L;
        return amount.setScale(2, BigDecimal.ROUND_HALF_UP)
                .movePointRight(2)
                .longValueExact();
    }

    private static long calcTax(long baseCents, BigDecimal ratePercent) {
        if (ratePercent == null || ratePercent.compareTo(BigDecimal.ZERO) <= 0) return 0L;
        return BigDecimal.valueOf(baseCents)
                .multiply(ratePercent)
                .divide(BigDecimal.valueOf(100), 0, BigDecimal.ROUND_HALF_UP)
                .longValueExact();
    }

    private static long mulExact(long a, int b) {
        try {
            return Math.multiplyExact(a, (long) b);
        } catch (ArithmeticException ex) {
            throw new BusinessException("Montant trop élevé (overflow calcul) pour la quantité " + b);
        }
    }

    private static String nvlBlank(String v, String orElse) {
        return (v == null || v.isBlank()) ? orElse : v;
    }
}
