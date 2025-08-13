package com.koramarket.commande.repository;

import com.koramarket.commande.model.CommandeItem;
import com.koramarket.commande.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, String> {
}

