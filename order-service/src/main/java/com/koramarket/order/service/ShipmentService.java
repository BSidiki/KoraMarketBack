// service/ShipmentService.java
package com.koramarket.order.service;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.dto.ShipmentRequestDTO;
import com.koramarket.order.dto.ShipmentResponseDTO;
import com.koramarket.order.dto.ShipmentStatusEventDTO;
import com.koramarket.order.mapper.ShipmentMapper;
import com.koramarket.order.model.*;
import com.koramarket.order.repository.*;
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
    private final OrderItemRepository orderItemRepo; // crée si pas encore
    // private final OutboxService outboxService; // (option) SHIPMENT_CREATED/SHIPPED/DELIVERED

    @Transactional
    public ShipmentResponseDTO create(ShipmentRequestDTO req, UUID userIdExt, boolean isPrivileged) {
        Order o = orderRepo.findOneWithItems(req.getOrderId())
                .orElseThrow(() -> new BusinessException("Commande introuvable"));

        // ACL: client propriétaire peut voir, mais création = staff/vendor
        if (!isPrivileged && !o.getUserIdExt().equals(userIdExt)) {
            throw new BusinessException("Accès interdit");
        }
        if (!List.of("AWAITING_FULFILLMENT","PARTIALLY_FULFILLED").contains(o.getOrderStatus())) {
            throw new BusinessException("Commande non prête pour expédition");
        }

        // Quantités déjà planifiées
        Map<UUID, Integer> already = new HashMap<>();
        for (Object[] row : shipmentRepo.sumQuantitiesByOrderItem(o.getId())) {
            already.put((UUID) row[0], ((Number) row[1]).intValue());
        }

        // Valider items
        Map<UUID, OrderItem> orderItems = o.getItems().stream()
                .collect(Collectors.toMap(OrderItem::getId, it -> it));
        UUID vendorId = req.getVendorId();
        List<ShipmentItem> shipItems = new ArrayList<>();
        for (var l : req.getItems()) {
            var oi = orderItems.get(l.getOrderItemId());
            if (oi == null) throw new BusinessException("Ligne commande introuvable: " + l.getOrderItemId());
            if (vendorId != null && oi.getVendorIdExt()!=null && !oi.getVendorIdExt().equals(vendorId)) {
                throw new BusinessException("Ligne n’appartient pas au vendeur indiqué");
            }
            int ordered = oi.getQuantity();
            int used = already.getOrDefault(oi.getId(), 0);
            int remaining = ordered - used;
            if (l.getQuantity() <= 0 || l.getQuantity() > remaining) {
                throw new BusinessException("Quantité invalide pour la ligne " + oi.getId() + " (restant: " + remaining + ")");
            }
            shipItems.add(ShipmentItem.builder().orderItem(oi).quantity(l.getQuantity()).build());
        }
        // Inférer vendor si non fourni (toutes les lignes doivent avoir même vendor)
        if (vendorId == null) {
            vendorId = shipItems.stream()
                    .map(si -> si.getOrderItem().getVendorIdExt())
                    .filter(Objects::nonNull).findFirst().orElse(null);
        }

        Shipment s = Shipment.builder()
                .order(o)
                .vendorIdExt(vendorId)
                .carrier(req.getCarrier())
                .trackingNumber(req.getTrackingNumber())
                .status(ShipmentStatus.CREATED)
                .build();
        for (var si : shipItems) { si.setShipment(s); }
        s.setItems(shipItems);

        s = shipmentRepo.save(s);

        // MAJ statut commande
        recalcOrderFulfillment(o);

        // (option) outboxService.publish("SHIPMENT_CREATED", ... , o.getId());
        return ShipmentMapper.toResponse(s);
    }

    @Transactional
    public ShipmentResponseDTO event(UUID shipmentId, ShipmentStatusEventDTO evt, UUID userIdExt, boolean isPrivileged) {
        Shipment s = shipmentRepo.findById(shipmentId)
                .orElseThrow(() -> new BusinessException("Shipment introuvable"));
        Order o = s.getOrder();

        // ACL: vendor/admin seulement
        if (!isPrivileged) throw new BusinessException("Accès interdit");

        ShipmentStatus newStatus = parse(evt.getStatus());
        s.setStatus(newStatus);
        if (evt.getCarrier()!=null && !evt.getCarrier().isBlank()) s.setCarrier(evt.getCarrier().trim());
        if (evt.getTrackingNumber()!=null && !evt.getTrackingNumber().isBlank()) s.setTrackingNumber(evt.getTrackingNumber().trim());
        if (newStatus == ShipmentStatus.SHIPPED) s.setShippedAt(Instant.now());
        if (newStatus == ShipmentStatus.DELIVERED) s.setDeliveredAt(Instant.now());

        // MAJ statut commande
        recalcOrderFulfillment(o);

        // (option) outboxService.publish("SHIPMENT_"+newStatus.name(), ..., o.getId());
        return ShipmentMapper.toResponse(s);
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponseDTO> listByOrder(UUID orderId, UUID userIdExt, boolean canReadAny) {
        Order o = orderRepo.findById(orderId).orElseThrow(() -> new BusinessException("Commande introuvable"));
        if (!canReadAny && !o.getUserIdExt().equals(userIdExt)) throw new BusinessException("Accès interdit");
        return shipmentRepo.findByOrder_IdOrderByCreatedAtAsc(orderId).stream().map(ShipmentMapper::toResponse).toList();
    }

    private static ShipmentStatus parse(String raw){
        try { return ShipmentStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT)); }
        catch(Exception e){ throw new BusinessException("Statut de shipment invalide: " + raw); }
    }

    /** Recalcule un statut de commande simple selon les quantités planifiées et livrées */
    private void recalcOrderFulfillment(Order o){
        int totalOrdered = o.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
        // quantités expédiées (SHIPPED/DELIVERED)
        var shipments = shipmentRepo.findByOrder_IdOrderByCreatedAtAsc(o.getId());
        int totalShipped = shipments.stream()
                .filter(s -> s.getStatus()==ShipmentStatus.SHIPPED || s.getStatus()==ShipmentStatus.DELIVERED)
                .flatMap(s -> s.getItems().stream())
                .mapToInt(ShipmentItem::getQuantity).sum();
        int totalDelivered = shipments.stream()
                .filter(s -> s.getStatus()==ShipmentStatus.DELIVERED)
                .flatMap(s -> s.getItems().stream())
                .mapToInt(ShipmentItem::getQuantity).sum();

        if (totalDelivered >= totalOrdered) {
            o.setOrderStatus("COMPLETED");
        } else if (totalShipped >= totalOrdered) {
            o.setOrderStatus("FULFILLED");
        } else if (totalShipped > 0) {
            o.setOrderStatus("PARTIALLY_FULFILLED");
        } else {
            // laisse "AWAITING_FULFILLMENT"
            if (!"AWAITING_FULFILLMENT".equals(o.getOrderStatus())) {
                o.setOrderStatus("AWAITING_FULFILLMENT");
            }
        }
    }
}
