package com.example.saga.payment.events;

public record PaymentRevertedEvent(String orderId, String reason) {
}
