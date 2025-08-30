package com.example.saga.payment.events;

public record PaymentFailedEvent(String orderId, String reason) {
}
