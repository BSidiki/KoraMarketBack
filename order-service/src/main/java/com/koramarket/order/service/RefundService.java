package com.koramarket.order.service;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.dto.RefundRequestDTO;
import com.koramarket.order.dto.RefundResponseDTO;
import com.koramarket.order.mapper.RefundMapper;
import com.koramarket.order.model.*;
import com.koramarket.order.repository.CreditNoteRepository;
import com.koramarket.order.repository.OrderRepository;
import com.koramarket.order.repository.PaymentRepository;
import com.koramarket.order.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepo;
    private final PaymentRepository paymentRepo;
    private final OrderRepository orderRepo;
    private final CreditNoteRepository creditNoteRepository;
    private final CreditNoteService creditNoteService;

    private final OutboxService outboxService;     // déjà en place

    @Transactional
    public RefundResponseDTO create(RefundRequestDTO req, boolean isPrivileged) {
        if (!isPrivileged) throw new BusinessException("Permission refusée");
        if (req.getPaymentId() == null) throw new BusinessException("paymentId requis");

        Payment p = paymentRepo.findById(req.getPaymentId())
                .orElseThrow(() -> new BusinessException("Paiement introuvable"));
        Order o = p.getOrder();

        if (p.getStatus() != PaymentState.CAPTURED) {
            throw new BusinessException("Remboursement possible uniquement sur paiement capturé");
        }

//        long alreadyRefunded = refundRepo.sumCompletedByPayment(p.getId());
//        long refundable = p.getAmountAuthorized() - alreadyRefunded;
        long alreadyRefunded = refundRepo.sumCompletedByPayment(p.getId());
        long refundable = (p.getAmountCaptured() == null ? 0L : p.getAmountCaptured()) - alreadyRefunded;
        if (refundable <= 0) throw new BusinessException("Plus rien à rembourser");

        long amount = (req.getAmount() == null || req.getAmount() <= 0) ? refundable : req.getAmount();
        if (amount > refundable) throw new BusinessException("Montant > restant remboursable (" + refundable + ")");

        Refund r = Refund.builder()
                .payment(p)
                .order(o)
                .amount(amount)
                .currency(p.getCurrency())
                .status(RefundStatus.REQUESTED)
                .reason(req.getReason())
                .build();

        r = refundRepo.save(r);

        // MOCK provider: on exécute immédiatement (sinon, expose /api/refunds/{id}/complete)
        r.setStatus(RefundStatus.COMPLETED);
        r.setCompletedAt(Instant.now());

        try {
            creditNoteService.ensureForRefund(r);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass())
                    .warn("CreditNote ensureForRefund failed: {}", e.toString(), e);
        }

        long refundedNow = amount;
        long refundedTotal = alreadyRefunded + refundedNow;

        if (refundedTotal >= (p.getAmountCaptured() == null ? 0L : p.getAmountCaptured())) {
            p.setStatus(PaymentState.REFUNDED);
            o.setPaymentStatus("REFUNDED");
        } else if (!p.getCurrency().equalsIgnoreCase(o.getCurrency())) {
            throw new BusinessException("Devise incohérente entre paiement et commande");
        }else {
            p.setStatus(PaymentState.PARTIALLY_REFUNDED);
            o.setPaymentStatus("PARTIALLY_REFUNDED");
        }


        // Si remboursement total, on marque la facture en CANCELED (optionnel)
        if (amount == refundable) {
            //TODO: générer un CREDIT NOTE (CN-...), ou marquer INVOICE CANCELED
            //invoiceService.cancelOrCredit(o, r); //(à faire dans l’étape “Credit Notes”)
        }

        // Outbox
        try {
            outboxService.publish(
                    "REFUND_COMPLETED",
                    java.util.Map.of(
                            "refundId", r.getId().toString(),
                            "paymentId", p.getId().toString(),
                            "orderId", o.getId().toString(),
                            "amount", r.getAmount(),
                            "currency", r.getCurrency(),
                            "reason", r.getReason()
                    ),
                    o.getId()
            );
        } catch (Exception ignore) {}

        return RefundMapper.toResponse(r);
    }

    @Transactional(readOnly = true)
    public RefundResponseDTO findOne(UUID id){
        return refundRepo.findById(id).map(RefundMapper::toResponse)
                .orElseThrow(() -> new BusinessException("Refund introuvable"));
    }
}
