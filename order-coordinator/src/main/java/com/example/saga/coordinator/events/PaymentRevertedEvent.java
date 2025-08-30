package com.example.saga.coordinator.events;

public record PaymentRevertedEvent(String orderId, String reason) {
}
