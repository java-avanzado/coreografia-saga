package com.example.saga.coordinator.events;

public record CompensatePaymentEvent(String orderId, String reason) {
}
