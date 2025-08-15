package com.koramarket.order.outbox;

public interface OutboxTransport {
    void send(String topic, String payload) throws Exception;
}
