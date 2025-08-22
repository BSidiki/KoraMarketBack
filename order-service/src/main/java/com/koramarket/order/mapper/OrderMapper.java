package com.koramarket.order.mapper;

import com.koramarket.order.dto.OrderResponseDTO;
import com.koramarket.order.model.Order;
import com.koramarket.order.model.OrderItem;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class OrderMapper {

    public static OrderResponseDTO toResponse(Order o) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(o.getId());
        dto.setOrderNumber(o.getOrderNumber());
        dto.setUserId(o.getUserIdExt());
        dto.setCurrency(o.getCurrency());
        dto.setSubtotal(o.getSubtotalAmount());
        dto.setTaxTotal(o.getTaxTotalAmount());
        dto.setShippingTotal(o.getShippingTotalAmount());
        dto.setDiscountTotal(o.getDiscountTotalAmount());
        dto.setGrandTotal(o.getGrandTotalAmount());
        dto.setOrderStatus(o.getOrderStatus());
        dto.setPaymentStatus(o.getPaymentStatus());
        dto.setCreatedAt(o.getCreatedAt());

        // Items
        dto.setItems(o.getItems().stream().map(OrderMapper::toItem).toList());

        // ---- Agrégation par vendeur (dans le mapper) ----
        class Agg {
            java.util.UUID vendorId;
            String vendorEmail;
            int itemCount;
            long subtotal;
            long tax;
        }
        java.util.Map<String, Agg> agg = new java.util.LinkedHashMap<>();
        for (var it : o.getItems()) {
            var vId = it.getVendorIdExt();
            var vEmail = it.getVendorEmailSnap() == null ? "" : it.getVendorEmailSnap().trim();
            String key = (vId != null) ? ("ID:" + vId)
                    : (!vEmail.isEmpty() ? ("EMAIL:" + vEmail.toLowerCase(java.util.Locale.ROOT)) : "UNKNOWN");

            var a = agg.computeIfAbsent(key, k -> new Agg());
            if (a.vendorId == null && vId != null) a.vendorId = vId;
            if ((a.vendorEmail == null || a.vendorEmail.isBlank()) && !vEmail.isBlank()) a.vendorEmail = vEmail;

            int qty = it.getQuantity() == null ? 0 : it.getQuantity();
            long unit = it.getUnitPriceAmount() == null ? 0L : it.getUnitPriceAmount();
            long taxAmt = it.getTaxAmount() == null ? 0L : it.getTaxAmount();

            a.itemCount += qty;
            a.subtotal  += unit * (long) qty;
            a.tax       += taxAmt;
        }

        var vendorSummaries = new java.util.ArrayList<OrderResponseDTO.VendorSummary>();
        for (var a : agg.values()) {
            var vs = new OrderResponseDTO.VendorSummary();
            vs.setVendorId(a.vendorId);
            vs.setVendorEmail(a.vendorEmail);
            vs.setItemCount(a.itemCount);
            vs.setSubtotal(a.subtotal);
            vs.setTaxTotal(a.tax);
            vs.setGrandTotal(a.subtotal + a.tax);
            vendorSummaries.add(vs);
        }
        dto.setVendors(vendorSummaries);
        dto.setVendorCount(vendorSummaries.size());

        // Compat : ne remonter vendeurId/email qu’en cas d’unicité
        if (vendorSummaries.size() == 1) {
            var only = vendorSummaries.get(0);
            dto.setVendeurId(only.getVendorId());
            dto.setVendeurEmail(only.getVendorEmail());
        } else {
            dto.setVendeurId(null);
            dto.setVendeurEmail(null);
        }

        return dto;
    }

    private static OrderResponseDTO.Item toItem(OrderItem i) {
        OrderResponseDTO.Item dto = new OrderResponseDTO.Item();
        dto.setId(i.getId());
        dto.setProductId(i.getProductIdExt());
        dto.setName(i.getProductNameSnap());
        dto.setSku(i.getProductSkuSnap());
        dto.setUnitPrice(nz(i.getUnitPriceAmount()));
        dto.setQuantity(nz(i.getQuantity()));
        dto.setTaxAmount(nz(i.getTaxAmount()));
        dto.setLineTotal(nz(i.getLineTotalAmount()));
        dto.setImage(i.getProductImageSnap());
        return dto;
    }

    private static String nz(String s) { return (s == null ? "" : s); }
    private static int nz(Integer v) { return (v == null ? 0 : v); }
    private static long nz(Long v) { return (v == null ? 0L : v); }

    private static long safeMul(long a, int b) {
        try { return Math.multiplyExact(a, (long) b); }
        catch (ArithmeticException ex) { return a * (long) b; } // overflow très improbable ici
    }

    public static String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}