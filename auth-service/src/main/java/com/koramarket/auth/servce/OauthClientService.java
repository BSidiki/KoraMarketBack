package com.koramarket.auth.servce;

import com.koramarket.auth.model.OauthClient;
import com.koramarket.auth.repository.OauthClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OauthClientService {

    private final OauthClientRepository oauthClientRepository;

    public List<OauthClient> findAll() {
        return oauthClientRepository.findAll();
    }

    public Optional<OauthClient> findById(Long id) {
        return oauthClientRepository.findById(id);
    }

    public Optional<OauthClient> findByClientId(String clientId) {
        return oauthClientRepository.findByClientId(clientId);
    }

    public OauthClient save(OauthClient client) {
        return oauthClientRepository.save(client);
    }

    public void delete(Long id) {
        oauthClientRepository.deleteById(id);
    }
}
