// CreditNoteService.java
package com.koramarket.order.service;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.model.CreditNote;
import com.koramarket.order.model.Invoice;
import com.koramarket.order.repository.CreditNoteRepository;
import com.koramarket.order.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditNoteService {
    private final CreditNoteRepository creditRepo;
    private final InvoiceRepository invoiceRepo;

    @Transactional
    public CreditNote createForRefund(UUID invoiceId, long refundAmountCents) {
        Invoice inv = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("Facture introuvable"));
        CreditNote cn = CreditNote.builder()
                .invoice(inv)
                .creditNumber(generateCN())
                .amount(refundAmountCents)
                .currency(inv.getOrder().getCurrency())
                .build();
        return creditRepo.save(cn);
    }

    private static String generateCN() {
        String ym = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String rnd = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "CN-" + ym + "-" + rnd;
    }
}
