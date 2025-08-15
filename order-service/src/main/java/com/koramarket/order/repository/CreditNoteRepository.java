package com.koramarket.order.repository;

import com.koramarket.order.model.CreditNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CreditNoteRepository extends JpaRepository<CreditNote, UUID> {
    Optional<CreditNote> findTopByInvoice_IdOrderByCreatedAtDesc(UUID invoiceId);
}
