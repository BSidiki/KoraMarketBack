// LoggingOutboxTransport.java
package com.koramarket.order.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingOutboxTransport implements OutboxTransport {
    @Override
    public void send(String topic, String payload) {
        // Ici tu brancheras Kafka/Rabbit/HTTP. Pour l’instant on log.
        log.info("[OUTBOX] topic={} payload={}", topic, payload);
    }
}
