package com.koramarket.auth.controller;

import com.koramarket.auth.servce.SessionService;
import com.koramarket.auth.dto.SessionResponseDTO;
import com.koramarket.auth.mapper.SessionMapper;
import com.koramarket.auth.model.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public List<SessionResponseDTO> getAllSessions() {
        return sessionService.findAll().stream()
                .map(SessionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionResponseDTO> getSessionById(@PathVariable Long id) {
        return sessionService.findById(id)
                .map(SessionMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/token/{token}")
    public ResponseEntity<SessionResponseDTO> getSessionByToken(@PathVariable String token) {
        return sessionService.findByToken(token)
                .map(SessionMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SessionResponseDTO> createSession(@RequestBody Session session) {
        Session saved = sessionService.save(session);
        return ResponseEntity.ok(SessionMapper.toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable Long id) {
        if (sessionService.findById(id).isPresent()) {
            sessionService.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
