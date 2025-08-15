package com.koramarket.order.service;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.dto.InvoiceResponseDTO;
import com.koramarket.order.mapper.InvoiceMapper;
import com.koramarket.order.model.Invoice;
import com.koramarket.order.model.InvoiceStatus;
import com.koramarket.order.model.Order;
import com.koramarket.order.repository.InvoiceRepository;
import com.koramarket.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepo;
    private final OrderRepository orderRepo;
    private final OutboxService outboxService;

    // base publique pour construire l'URL (mettre ton domaine Gateway en prod)
    @org.springframework.beans.factory.annotation.Value("${invoice.public-base-url:}")
    private String publicBaseUrl;

    /** Idempotent: renvoie la facture existante ou la crée pour la commande donnée */
    @Transactional
    public Invoice ensureForOrder(Order o) {
        return invoiceRepo.findByOrder_Id(o.getId()).orElseGet(() -> createNew(o));
    }

    @Transactional
    public Invoice ensureForOrder(UUID orderId) {
        Order o = orderRepo.findById(orderId).orElseThrow(() -> new BusinessException("Commande introuvable"));
        return ensureForOrder(o);
    }

    private Invoice createNew(Order o) {
        // On génère l'ID nous-mêmes pour pouvoir construire l'URL (sinon PrePersist le ferait)
        java.util.UUID newId = java.util.UUID.randomUUID();

        Invoice inv = Invoice.builder()
                .id(newId)
                .order(o)
                .invoiceNumber(generateInvoiceNumber())
                .status(InvoiceStatus.ISSUED)
                .urlPdf(buildInvoiceUrl(newId))     // ✅ on renseigne l’URL ici
                .build();

        try {
            return invoiceRepo.save(inv);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // collision très improbable -> on régénère et retente une fois
            inv.setInvoiceNumber(generateInvoiceNumber());
//            return invoiceRepo.save(inv);
            var saved = invoiceRepo.save(inv);
            try {
                outboxService.publish(
                        "INVOICE_ISSUED",
                        java.util.Map.of(
                                "invoiceId", saved.getId().toString(),
                                "invoiceNumber", saved.getInvoiceNumber(),
                                "orderId", o.getId().toString(),
                                "amount", o.getGrandTotalAmount(),
                                "currency", o.getCurrency()
                        ),
                        o.getId()
                );
            } catch (Exception ignore) { /* ne bloque pas la facturation */ }
            return saved;
        }
    }

    private String buildInvoiceUrl(java.util.UUID invoiceId) {
        // Si tu as un gateway (ex: http://localhost:8080), configure invoice.public-base-url
        String base = (publicBaseUrl == null || publicBaseUrl.isBlank()) ? "" : publicBaseUrl.trim();
        // URL stable vers notre endpoint PDF:
        //  - absolue si base configurée (ex: http://localhost:8080/api/invoices/{id}/pdf)
        //  - relative sinon (ex: /api/invoices/{id}/pdf)
        return base + "/api/invoices/" + invoiceId + "/pdf";
    }

    private static String generateInvoiceNumber() {
        String ym = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String rnd = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "INV-" + ym + "-" + rnd;
    }

    /* --------- Queries --------- */

    @Transactional(readOnly = true)
    public Optional<InvoiceResponseDTO> findByOrderId(UUID orderId) {
        return invoiceRepo.findByOrder_Id(orderId).map(InvoiceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<InvoiceResponseDTO> findById(UUID id) {
        return invoiceRepo.findById(id).map(InvoiceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<InvoiceResponseDTO> findByNumber(String invoiceNumber) {
        if (invoiceNumber == null || invoiceNumber.isBlank()) return Optional.empty();
        return invoiceRepo.findByInvoiceNumber(invoiceNumber.trim()).map(InvoiceMapper::toResponse);
    }
}
