package com.koramarket.order.repository;

import com.koramarket.order.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    Optional<Payment> findByExternalTransactionId(String externalTransactionId);
}
