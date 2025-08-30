package com.example.saga.coordinator.events;

public record PaymentAuthorizedEvent(String orderId, String paymentId,
                                     String reason) {
}

public record PaymentFailedEvent(String orderId, String reason) {
}

public record CompensatePaymentEvent(String orderId, String reason) {
}

public record PaymentRevertedEvent(String orderId, String reason) {
}
