package com.example.saga.coordinator.events;

public record PaymentFailedEvent(String orderId, String reason) {
}
