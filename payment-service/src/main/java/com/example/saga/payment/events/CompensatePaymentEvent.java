package com.example.saga.payment.events;

public record CompensatePaymentEvent(String orderId, String reason) {
}
