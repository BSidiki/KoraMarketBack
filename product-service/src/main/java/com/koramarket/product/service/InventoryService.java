package com.koramarket.product.service;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.product.dto.ReleaseStockRequestDTO;
import com.koramarket.product.dto.ReserveStockRequestDTO;
import com.koramarket.product.dto.ReserveStockResponseDTO;
import com.koramarket.product.model.InventoryReservation;
import com.koramarket.product.model.InventoryReservationItem;
import com.koramarket.product.repository.InventoryReservationRepository;
import com.koramarket.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepo;
    private final InventoryReservationRepository resRepo;

    @Transactional
    public ReserveStockResponseDTO reserve(ReserveStockRequestDTO req) {
        var existing = resRepo.findByOrderId(req.getOrderId());
        if (existing.isPresent()) return toResponse(existing.get());

        // All-or-nothing
        for (var l : req.getLines()) {
            if (productRepo.decrementIfEnough(l.getProductId(), l.getQuantity()) == 0) {
                throw new BusinessException("Stock insuffisant pour produit " + l.getProductId());
            }
        }
        // Persist reservation + items
        var res = InventoryReservation.builder()
                .orderId(req.getOrderId()).status("ACTIVE").build();
        var items = new ArrayList<InventoryReservationItem>();
        for (var l : req.getLines()) {
            items.add(InventoryReservationItem.builder()
                    .reservation(res).productId(l.getProductId()).quantity(l.getQuantity()).build());
        }
        res.setItems(items);
        res = resRepo.save(res);
        return toResponse(res);
    }

    @Transactional
    public void release(ReleaseStockRequestDTO req) {
        var res = resRepo.findByOrderId(req.getOrderId())
                .orElseThrow(() -> new BusinessException("Réservation introuvable pour orderId=" + req.getOrderId()));
        if ("RELEASED".equals(res.getStatus())) return; // idempotent

        // Remet en stock
        for (var it : res.getItems()) productRepo.increment(it.getProductId(), it.getQuantity());
        res.setStatus("RELEASED"); res.setReleasedAt(Instant.now());
    }

    private static ReserveStockResponseDTO toResponse(InventoryReservation r) {
        ReserveStockResponseDTO dto = new ReserveStockResponseDTO();
        dto.setReservationId(r.getId());
        dto.setOrderId(r.getOrderId());
        dto.setStatus(r.getStatus());

        var lines = new ArrayList<ReserveStockResponseDTO.Line>();
        for (var it : r.getItems()) {
            ReserveStockResponseDTO.Line l = new ReserveStockResponseDTO.Line(); // <-- FIX
            l.setProductId(it.getProductId());
            l.setQuantity(it.getQuantity());
            lines.add(l);
        }
        dto.setLines(lines);
        return dto;
    }

}

