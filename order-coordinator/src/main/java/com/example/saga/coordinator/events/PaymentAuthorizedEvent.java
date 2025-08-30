package com.example.saga.coordinator.events;

public record PaymentAuthorizedEvent(String orderId, String paymentId,
                                     String reason) {
}
