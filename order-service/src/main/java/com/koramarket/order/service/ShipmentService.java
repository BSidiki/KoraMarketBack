// service/ShipmentService.java
package com.koramarket.order.service;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.dto.ShipmentRequestDTO;
import com.koramarket.order.dto.ShipmentResponseDTO;
import com.koramarket.order.dto.ShipmentStatusEventDTO;
import com.koramarket.order.mapper.ShipmentMapper;
import com.koramarket.order.model.*;
import com.koramarket.order.repository.OrderItemRepository;
import com.koramarket.order.repository.OrderRepository;
import com.koramarket.order.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo; // (si non utilisé ailleurs, tu peux le retirer)
    // private final OutboxService outboxService; // décommente si disponible

    /**
     * Crée un shipment pour une commande et un vendeur (facultatif).
     * Valide que les quantités expédiées ne dépassent pas le restant par ligne.
     */
    @Transactional
    public ShipmentResponseDTO create(ShipmentRequestDTO req, UUID userIdExt, boolean isPrivileged) {
        if (req == null || req.getOrderId() == null) {
            throw new BusinessException("orderId requis");
        }
        Order o = orderRepo.findOneWithItems(req.getOrderId())
                .orElseThrow(() -> new BusinessException("Commande introuvable"));

        // ACL: staff / vendor (client ne crée pas d’expédition)
        if (!isPrivileged) {
            throw new BusinessException("Accès interdit");
        }

        // Commande éligible ?
        if (!List.of("AWAITING_FULFILLMENT", "PARTIALLY_FULFILLED", "AWAITING_SHIPMENT", "PAID").contains(o.getOrderStatus())) {
            throw new BusinessException("Commande non prête pour expédition");
        }

        // Quantités déjà planifiées (tous shipments existants)
        Map<UUID, Integer> alreadyPlanned = computeAlreadyPlannedByOrderItem(o.getId());

        // Index des lignes de commande de l’ordre
        Map<UUID, OrderItem> orderItems = o.getItems().stream()
                .collect(Collectors.toMap(OrderItem::getId, it -> it));

        // Valider la requête & construire les lignes de shipment
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new BusinessException("Au moins une ligne est requise");
        }

        // Fusionner les lignes dupliquées (même orderItemId) côté requête
        Map<UUID, Integer> requestedQtyByOi = new LinkedHashMap<>();
        for (var l : req.getItems()) {
            if (l.getOrderItemId() == null) throw new BusinessException("orderItemId manquant");
            if (l.getQuantity() == 0 || l.getQuantity() <= 0) {
                throw new BusinessException("Quantité invalide pour la ligne: " + l.getOrderItemId());
            }
            requestedQtyByOi.merge(l.getOrderItemId(), l.getQuantity(), Integer::sum);
        }

        UUID vendorId = req.getVendorId();
        List<ShipmentItem> shipItems = new ArrayList<>();

        for (var entry : requestedQtyByOi.entrySet()) {
            UUID oiId = entry.getKey();
            int reqQty = entry.getValue();

            OrderItem oi = orderItems.get(oiId);
            if (oi == null) throw new BusinessException("Ligne commande introuvable: " + oiId);

            // Vérifier cohérence du vendeur si fourni
            if (vendorId != null && oi.getVendorIdExt() != null && !oi.getVendorIdExt().equals(vendorId)) {
                throw new BusinessException("La ligne " + oiId + " n’appartient pas au vendeur indiqué");
            }

            int ordered = (oi.getQuantity() == null ? 0 : oi.getQuantity());
            int used = alreadyPlanned.getOrDefault(oi.getId(), 0);
            int remaining = ordered - used;
            if (reqQty > remaining) {
                throw new BusinessException("Quantité demandée " + reqQty + " > restant " + remaining + " pour la ligne " + oiId);
            }

            shipItems.add(ShipmentItem.builder()
                    .orderItem(oi)
                    .quantity(reqQty)
                    .build());
        }

        // Inférer vendor si non fourni (toutes les lignes doivent être homogènes)
        if (vendorId == null) {
            vendorId = shipItems.stream()
                    .map(si -> si.getOrderItem().getVendorIdExt())
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        Shipment s = Shipment.builder()
                .order(o)
                .vendorIdExt(vendorId)
                .carrier(nvl(req.getCarrier()))
                .trackingNumber(nvl(req.getTrackingNumber()))
                .status(ShipmentStatus.CREATED)
                .build();

        for (var si : shipItems) si.setShipment(s);
        s.setItems(shipItems);

        s = shipmentRepo.save(s);

        // Recalcule statut de la commande
        recomputeOrderFulfillment(o);

        // // Outbox éventuelle
        // publishOutbox("SHIPMENT_CREATED", s);

        return ShipmentMapper.toResponse(s);
    }

    /**
     * Transition de statut (SHIPPED / DELIVERED) + tracking.
     */
    @Transactional
    public ShipmentResponseDTO event(UUID shipmentId, ShipmentStatusEventDTO evt, UUID userIdExt, boolean isPrivileged) {
        if (shipmentId == null) throw new BusinessException("shipmentId requis");
        if (evt == null || evt.getStatus() == null) throw new BusinessException("Statut requis");

        Shipment s = shipmentRepo.findById(shipmentId)
                .orElseThrow(() -> new BusinessException("Expédition introuvable"));
        Order o = s.getOrder();

        // ACL: staff/vendor
        if (!isPrivileged) throw new BusinessException("Accès interdit");

        ShipmentStatus newStatus = parseStatus(evt.getStatus());

        // Guards simples de transition
        if (newStatus == ShipmentStatus.DELIVERED && s.getStatus() == ShipmentStatus.CREATED) {
            throw new BusinessException("Impossible de livrer un colis non expédié");
        }

        // Metadonnées
        if (evt.getCarrier() != null && !evt.getCarrier().isBlank()) s.setCarrier(evt.getCarrier().trim());
        if (evt.getTrackingNumber() != null && !evt.getTrackingNumber().isBlank()) s.setTrackingNumber(evt.getTrackingNumber().trim());

        // Transition
        s.setStatus(newStatus);
        if (newStatus == ShipmentStatus.SHIPPED) {
            s.setShippedAt(Instant.now());
        } else if (newStatus == ShipmentStatus.DELIVERED) {
            if (s.getShippedAt() == null) s.setShippedAt(Instant.now()); // garde-fou
            s.setDeliveredAt(Instant.now());
        }

        // Recalcule statut de la commande
        recomputeOrderFulfillment(o);

        // // Outbox éventuelle
        // publishOutbox("SHIPMENT_" + newStatus.name(), s);

        return ShipmentMapper.toResponse(s);
    }

    /**
     * Liste des shipments d’une commande avec contrôle d’ownership.
     */
    @Transactional(readOnly = true)
    public List<ShipmentResponseDTO> listByOrder(UUID orderId, UUID userIdExt, boolean canReadAny) {
        Order o = orderRepo.findById(orderId).orElseThrow(() -> new BusinessException("Commande introuvable"));
        if (!canReadAny && !o.getUserIdExt().equals(userIdExt)) throw new BusinessException("Accès interdit");
        return shipmentRepo.findByOrder_IdOrderByCreatedAtAsc(orderId)
                .stream().map(ShipmentMapper::toResponse).toList();
    }

    /* ======================= Helpers ======================= */

    private static String nvl(String v) { return (v == null || v.isBlank()) ? null : v.trim(); }

    private static ShipmentStatus parseStatus(String raw) {
        try {
            return ShipmentStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new BusinessException("Statut de shipment invalide: " + raw);
        }
    }

    /**
     * Calcule, pour un orderId donné, les quantités déjà planifiées par OrderItem
     * en agrégeant toutes les shipment_items existantes (peu importe le statut).
     */
    private Map<UUID, Integer> computeAlreadyPlannedByOrderItem(UUID orderId) {
        var shipments = shipmentRepo.findByOrder_IdOrderByCreatedAtAsc(orderId);
        Map<UUID, Integer> map = new HashMap<>();
        for (var sh : shipments) {
            for (var li : sh.getItems()) {
                UUID oi = li.getOrderItem().getId();
                int q = (li.getQuantity() == null ? 0 : li.getQuantity());
                map.merge(oi, q, Integer::sum);
            }
        }
        return map;
    }

    /**
     * Recalcule un statut de commande simple selon les quantités expédiées et livrées.
     * COMPLETED si tout livré, PARTIALLY_FULFILLED si partiellement expédié, etc.
     */
    private void recomputeOrderFulfillment(Order o) {
        int totalQty = o.getItems().stream()
                .mapToInt(oi -> oi.getQuantity() == null ? 0 : oi.getQuantity()).sum();

        var shipments = shipmentRepo.findByOrder_IdOrderByCreatedAtAsc(o.getId());
        int shippedQty = 0, deliveredQty = 0;

        for (var sh : shipments) {
            boolean shipped   = sh.getStatus() == ShipmentStatus.SHIPPED || sh.getStatus() == ShipmentStatus.DELIVERED;
            boolean delivered = sh.getStatus() == ShipmentStatus.DELIVERED;
            for (var li : sh.getItems()) {
                int q = (li.getQuantity() == null ? 0 : li.getQuantity());
                if (shipped)   shippedQty   += q;
                if (delivered) deliveredQty += q;
            }
        }

        String prev = o.getOrderStatus();
        if (totalQty > 0 && deliveredQty >= totalQty) {
            o.setOrderStatus("COMPLETED");
        } else if (shippedQty >= totalQty && totalQty > 0) {
            o.setOrderStatus("FULFILLED");
        } else if (shippedQty > 0) {
            o.setOrderStatus("PARTIALLY_FULFILLED");
        } else {
            // Si paiement OK mais pas encore expédié -> AWAITING_SHIPMENT, sinon AWAITING_FULFILLMENT
            if ("PAID".equalsIgnoreCase(o.getPaymentStatus()) || "AWAITING_FULFILLMENT".equalsIgnoreCase(prev)) {
                o.setOrderStatus("AWAITING_SHIPMENT");
            } else {
                o.setOrderStatus("AWAITING_FULFILLMENT");
            }
        }

        // Rien à faire de plus : o est attaché au contexte persistant (Transaction ouverte)
        // orderRepo.save(o); // inutile ici, Hibernate flushera
    }

    // private void publishOutbox(String topic, Shipment s) {
    //     try {
    //         outboxService.publish(
    //             topic,
    //             java.util.Map.of(
    //                 "shipmentId", s.getId().toString(),
    //                 "orderId", s.getOrder().getId().toString(),
    //                 "vendorId", s.getVendorIdExt() != null ? s.getVendorIdExt().toString() : null,
    //                 "status", s.getStatus().name(),
    //                 "carrier", s.getCarrier(),
    //                 "trackingNumber", s.getTrackingNumber(),
    //                 "shippedAt", s.getShippedAt() != null ? s.getShippedAt().toString() : null,
    //                 "deliveredAt", s.getDeliveredAt() != null ? s.getDeliveredAt().toString() : null
    //             ),
    //             s.getOrder().getId()
    //         );
    //     } catch (Exception e) {
    //         org.slf4j.LoggerFactory.getLogger(getClass()).warn("Outbox publish KO: {}", e.toString());
    //     }
    // }
}
