package com.example.saga.transport.events;

public record PaymentAuthorizedEvent(String orderId, String paymentId,
                                     String reason) {
}
