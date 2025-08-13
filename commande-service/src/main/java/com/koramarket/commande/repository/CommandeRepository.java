package com.koramarket.commande.repository;

import com.koramarket.commande.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandeRepository extends JpaRepository<Commande, String> {
}
