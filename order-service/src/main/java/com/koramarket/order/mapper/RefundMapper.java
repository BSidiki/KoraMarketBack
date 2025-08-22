package com.koramarket.order.mapper;

import com.koramarket.order.dto.RefundResponseDTO;
import com.koramarket.order.model.Refund;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RefundMapper {
    public static RefundResponseDTO toResponse(Refund r){
        var dto = new RefundResponseDTO();
        dto.setId(r.getId());
        dto.setPaymentId(r.getPayment().getId());
        dto.setOrderId(r.getOrder().getId());
        dto.setAmount(r.getAmount());
        dto.setCurrency(r.getCurrency());
        dto.setStatus(r.getStatus().name());
        dto.setExternalRefundId(r.getExternalRefundId());
        dto.setReason(r.getReason());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setCompletedAt(r.getCompletedAt());
        return dto;
    }
}
