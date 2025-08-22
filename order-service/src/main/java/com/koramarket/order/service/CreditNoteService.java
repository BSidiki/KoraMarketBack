package com.koramarket.order.service;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.dto.CreditNoteResponseDTO;
import com.koramarket.order.mapper.CreditNoteMapper;
import com.koramarket.order.model.CreditNote;
import com.koramarket.order.model.CreditNoteStatus;
import com.koramarket.order.model.Refund;
import com.koramarket.order.model.RefundStatus;
import com.koramarket.order.repository.CreditNoteRepository;
import com.koramarket.order.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditNoteService {

    private final CreditNoteRepository creditNoteRepo;
    private final InvoiceRepository invoiceRepo; // pour lier la facture si elle existe

    @Value("${creditnote.public-base-url:}")
    private String publicBaseUrl;

    /** Idempotent : 1 avoir par refund */
    @Transactional
    public CreditNote ensureForRefund(Refund refund) {
        return creditNoteRepo.findByRefund_Id(refund.getId()).orElseGet(() -> createNew(refund));
    }

    private CreditNote createNew(Refund r) {
        if (r.getStatus() != RefundStatus.COMPLETED) {
            throw new BusinessException("Avoir émis uniquement sur remboursement complété");
        }
        var o = r.getOrder();
        var inv = invoiceRepo.findByOrder_Id(o.getId()).orElse(null);

        var id = UUID.randomUUID();
        var cn = CreditNote.builder()
                .id(id)
                .refund(r)
                .order(o)
                .invoice(inv)
                .creditNumber(generateNumber())
                .status(CreditNoteStatus.ISSUED)
                .amount(r.getAmount())
                .currency(r.getCurrency())
                .urlPdf(buildUrl(id))
                .issuedAt(java.time.Instant.now())
                .build();

        return creditNoteRepo.save(cn);
    }

    private String buildUrl(UUID id) {
        String base = (publicBaseUrl == null || publicBaseUrl.isBlank()) ? "" : publicBaseUrl.trim();
        return base + "/api/credit-notes/" + id + "/pdf";
    }

    @Transactional(readOnly = true)
    public java.util.Optional<CreditNoteResponseDTO> findById(java.util.UUID id) {
        return creditNoteRepo.findById(id).map(CreditNoteMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<CreditNoteResponseDTO> findByNumber(String number) {
        if (number == null || number.isBlank()) return java.util.Optional.empty();
        return creditNoteRepo.findByCreditNumber(number.trim()).map(CreditNoteMapper::toResponse);
    }

    private static String generateNumber() {
        String ym = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String rnd = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "CN-" + ym + "-" + rnd;
    }
}
