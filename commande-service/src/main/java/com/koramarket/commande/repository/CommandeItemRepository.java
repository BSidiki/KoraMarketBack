package com.koramarket.commande.repository;

import com.koramarket.commande.model.CommandeItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandeItemRepository extends JpaRepository<CommandeItem, String> {
}
