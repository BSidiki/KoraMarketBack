package com.koramarket.order.repository;

import com.koramarket.order.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByOrder_Id(UUID orderId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}
