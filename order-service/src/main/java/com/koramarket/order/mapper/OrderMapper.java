package com.koramarket.order.mapper;

import com.koramarket.order.dto.OrderResponseDTO;
import com.koramarket.order.model.Order;
import com.koramarket.order.model.OrderItem;
import lombok.experimental.UtilityClass;

import java.util.Objects;
import java.util.stream.Collectors;

@UtilityClass
public class OrderMapper {

    /*public static OrderResponseDTO toResponse(Order o) {
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
        dto.setItems(o.getItems().stream().map(OrderMapper::toItem).collect(Collectors.toList()));
        return dto;
    }

    private static OrderResponseDTO.Item toItem(OrderItem i) {
        OrderResponseDTO.Item dto = new OrderResponseDTO.Item();
        dto.setId(i.getId());                          // UUID (id de ligne)
        dto.setProductId(i.getProductIdExt());  // Use the productIdExt directly as Long
        dto.setName(i.getProductNameSnap());
        dto.setSku(i.getProductSkuSnap() != null ? i.getProductSkuSnap() : "");
        dto.setUnitPrice(i.getUnitPriceAmount());
        dto.setQuantity(i.getQuantity());
        dto.setTaxAmount(i.getTaxAmount());
        dto.setLineTotal(i.getLineTotalAmount());
        dto.setImage(i.getProductImageSnap() != null ? i.getProductImageSnap() : "");
        return dto;
    }

    public static String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }*/


    // OrderMapper.java
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

        // ↓↓↓ Mono-vendeur ? remonter au header
        var vendorIds   = o.getItems().stream().map(OrderItem::getVendorIdExt)
                .filter(Objects::nonNull).distinct().toList();
        if (vendorIds.size() == 1) dto.setVendeurId(vendorIds.get(0));

        var vendorEmails = o.getItems().stream().map(OrderItem::getVendorEmailSnap)
                .filter(Objects::nonNull).distinct().toList();
        if (vendorEmails.size() == 1) dto.setVendeurEmail(vendorEmails.get(0));

        dto.setItems(o.getItems().stream().map(OrderMapper::toItem).collect(Collectors.toList()));
        return dto;
    }

    private static OrderResponseDTO.Item toItem(OrderItem i) {
        OrderResponseDTO.Item dto = new OrderResponseDTO.Item();
        dto.setId(i.getId());
        dto.setProductId(i.getProductIdExt());
        dto.setName(i.getProductNameSnap());
        dto.setSku(blankToNull(i.getProductSkuSnap()));       // "" -> null
        dto.setUnitPrice(i.getUnitPriceAmount());
        dto.setQuantity(i.getQuantity());
        dto.setTaxAmount(i.getTaxAmount());
        dto.setLineTotal(i.getLineTotalAmount());
        dto.setImage(blankToNull(i.getProductImageSnap()));   // "" -> null
        return dto;
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

}
