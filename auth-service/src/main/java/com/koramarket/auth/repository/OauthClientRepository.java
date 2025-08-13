package com.koramarket.auth.repository;

import com.koramarket.auth.model.OauthClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OauthClientRepository extends JpaRepository<OauthClient, Long> {
    Optional<OauthClient> findByClientId(String clientId);
}
