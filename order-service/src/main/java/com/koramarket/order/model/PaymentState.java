package com.koramarket.order.model;

public enum PaymentState {
    CREATED,
    AUTHORIZED,
    CAPTURED,
    FAILED,
   PARTIALLY_REFUNDED,
    REFUNDED,
    PARTIALLY_CANCELED,
    CANCELED
}
