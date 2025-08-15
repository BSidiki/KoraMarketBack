// OutboxService.java
package com.koramarket.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koramarket.order.model.OutboxMessage;
import com.koramarket.order.model.OutboxStatus;
import com.koramarket.order.outbox.OutboxTransport;
import com.koramarket.order.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository outboxRepo;
    private final ObjectMapper objectMapper;     // auto-injecté par Spring Boot
    private final OutboxTransport transport;     // Logging pour l’instant

    @Transactional
    public void publish(String topic, Map<String, Object> payload, UUID aggregateId) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            OutboxMessage msg = OutboxMessage.builder()
                    .aggregateId(aggregateId)
                    .topic(topic)
                    .payload(json)
                    .status(OutboxStatus.PENDING)
                    .attempts(0)
                    .build();
            outboxRepo.save(msg);
        } catch (Exception e) {
            // on log seulement; la création d'outbox ne doit pas casser la transaction métier
            log.warn("Outbox publish failed: {}", e.toString());
        }
    }

    // Dispatcher simple: toutes les secondes on essaie d’expédier 100 messages PENDING
    @Scheduled(fixedDelayString = "${outbox.dispatcher.interval-ms:1000}")
    @Transactional
    public void dispatch() {
        var batch = outboxRepo.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        for (var msg : batch) {
            try {
                transport.send(msg.getTopic(), msg.getPayload());
                msg.setStatus(OutboxStatus.SENT);
                msg.setSentAt(Instant.now());
            } catch (Exception ex) {
                msg.setAttempts(msg.getAttempts() + 1);
                msg.setLastError(ex.toString());
                // Option: après N tentatives, passer en FAILED
                if (msg.getAttempts() >= 10) {
                    msg.setStatus(OutboxStatus.FAILED);
                }
                log.warn("Outbox send error (attempt {}): {}", msg.getAttempts(), ex.toString());
            }
        }
    }
}
