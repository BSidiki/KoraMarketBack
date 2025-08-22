package com.koramarket.order.repository;

import com.koramarket.order.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface RefundRepository extends JpaRepository<Refund, UUID> {

    @Query("select coalesce(sum(r.amount),0) from Refund r where r.payment.id = :paymentId and r.status = 'COMPLETED'")
    long sumCompletedByPayment(UUID paymentId);
}
