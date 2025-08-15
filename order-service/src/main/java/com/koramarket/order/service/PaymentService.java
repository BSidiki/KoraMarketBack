package com.koramarket.order.service;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.dto.PaymentRequestDTO;
import com.koramarket.order.dto.PaymentResponseDTO;
import com.koramarket.order.mapper.PaymentMapper;
import com.koramarket.order.model.Order;
import com.koramarket.order.model.Payment;
import com.koramarket.order.model.PaymentState;
import com.koramarket.order.repository.OrderRepository;
import com.koramarket.order.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final OrderRepository orderRepo;
    private final InvoiceService invoiceService;

    @Transactional
    public PaymentResponseDTO createIntent(PaymentRequestDTO req,
                                           String idemKey,
                                           UUID userIdExt,
                                           boolean isPrivileged) {
        if (req.getOrderId() == null) throw new BusinessException("orderId requis");

        String key = (idemKey == null || idemKey.isBlank()) ? null : idemKey.trim();
        if (key != null) {
            var existing = paymentRepo.findByIdempotencyKey(key);
            if (existing.isPresent()) return PaymentMapper.toResponse(existing.get());
        }

        Order o = orderRepo.findById(req.getOrderId())
                .orElseThrow(() -> new BusinessException("Commande introuvable"));

        // ownership / droits
        if (!isPrivileged && !o.getUserIdExt().equals(userIdExt)) {
            throw new BusinessException("Accès interdit à cette commande");
        }

        // états autorisés pour initier un paiement
        Set<String> allowed = Set.of("PENDING", "AWAITING_PAYMENT");
        if (!allowed.contains(o.getOrderStatus())) {
            throw new BusinessException("Commande non payable à ce stade");
        }
        if ("PAID".equalsIgnoreCase(o.getPaymentStatus())) {
            throw new BusinessException("Commande déjà payée");
        }

        long amount = o.getGrandTotalAmount() == null ? 0L : o.getGrandTotalAmount();
        String currency = (req.getCurrency() == null || req.getCurrency().isBlank())
                ? o.getCurrency()
                : req.getCurrency().trim().toUpperCase();

        // Simuler un provider: on autorise immédiatement (AUTHORIZED)
        Payment p = Payment.builder()
                .id(UUID.randomUUID())
                .order(o)
                .provider(req.getProvider() == null ? "MOCK" : req.getProvider().trim().toUpperCase())
                .status(PaymentState.AUTHORIZED)
                .amountAuthorized(amount)
                .amountCaptured(0L)
                .currency(currency)
                .idempotencyKey(key)
                .externalTransactionId(null) // à remplir si vrai provider
                .authorizedAt(Instant.now())
                .build();

        try {
            p = paymentRepo.save(p);
        } catch (DataIntegrityViolationException e) {
            if (key != null) {
                return paymentRepo.findByIdempotencyKey(key)
                        .map(PaymentMapper::toResponse)
                        .orElseThrow(() -> e);
            }
            throw e;
        }

        // Mettre à jour l’état de la commande (toujours en attente de capture)
        o.setPaymentStatus("AUTHORIZED");

        return PaymentMapper.toResponse(p);
    }

    @Transactional
    public PaymentResponseDTO capture(UUID paymentId, boolean isPrivileged) {
        if (!isPrivileged) throw new BusinessException("Permission refusée");

        Payment p = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new BusinessException("Paiement introuvable"));

        if (p.getStatus() != PaymentState.AUTHORIZED) {
            throw new BusinessException("Paiement non capturable");
        }

        p.setStatus(PaymentState.CAPTURED);
        p.setAmountCaptured(p.getAmountAuthorized());
        p.setCapturedAt(Instant.now());

        // Maj commande -> PAID
        Order o = p.getOrder();
        o.setPaymentStatus("PAID");
        if (List.of("PENDING","AWAITING_PAYMENT","AUTHORIZED").contains(o.getOrderStatus())) {
            o.setOrderStatus("AWAITING_FULFILLMENT"); // prêt à expédier
        }
        invoiceService.ensureForOrder(o);
        if (p.getStatus() == PaymentState.CAPTURED) {
            return PaymentMapper.toResponse(p); // idempotent: 200 OK
        }
        if (p.getStatus() != PaymentState.AUTHORIZED) {
            throw new BusinessException("Paiement non capturable");
        }
        return PaymentMapper.toResponse(p);
    }

    @Transactional(readOnly = true)
    public Optional<PaymentResponseDTO> findOne(UUID id) {
        return paymentRepo.findById(id).map(PaymentMapper::toResponse);
    }
}
