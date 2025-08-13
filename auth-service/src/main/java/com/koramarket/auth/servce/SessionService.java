package com.koramarket.auth.servce;

import com.koramarket.auth.model.Session;
import com.koramarket.auth.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    public List<Session> findAll() {
        return sessionRepository.findAll();
    }

    public Optional<Session> findById(Long id) {
        return sessionRepository.findById(id);
    }

    public Optional<Session> findByToken(String token) {
        return sessionRepository.findByToken(token);
    }

    public Optional<Session> findByRefreshToken(String refreshToken) {
        return sessionRepository.findByRefreshToken(refreshToken);
    }

    public Session save(Session session) {
        return sessionRepository.save(session);
    }

    public void delete(Long id) {
        sessionRepository.deleteById(id);
    }
}
