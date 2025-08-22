package com.koramarket.order.mapper;

import com.koramarket.order.dto.CreditNoteResponseDTO;
import com.koramarket.order.model.CreditNote;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CreditNoteMapper {
    public static CreditNoteResponseDTO toResponse(CreditNote cn) {
        var dto = new CreditNoteResponseDTO();
        dto.setId(cn.getId());
        dto.setRefundId(cn.getRefund().getId());
        dto.setOrderId(cn.getOrder().getId());
        dto.setInvoiceId(cn.getInvoice() != null ? cn.getInvoice().getId() : null);
        dto.setCreditNumber(cn.getCreditNumber());
        dto.setUrlPdf(cn.getUrlPdf());
        dto.setStatus(cn.getStatus().name());
        dto.setAmount(cn.getAmount());
        dto.setCurrency(cn.getCurrency());
        dto.setReason(cn.getReason());
        dto.setCreatedAt(cn.getCreatedAt());
        dto.setIssuedAt(cn.getIssuedAt());
        return dto;
    }
}
