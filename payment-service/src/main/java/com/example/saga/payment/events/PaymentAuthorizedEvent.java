package com.example.saga.payment.events;

public record PaymentAuthorizedEvent(String orderId, String paymentId,
                                     String reason) {
}
