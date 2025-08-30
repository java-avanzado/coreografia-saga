package com.example.saga.payment.events;

public record OrderCreatedEvent(String orderId, String userId, double amount,
                                String address) {
}

public record PaymentAuthorizedEvent(String orderId, String paymentId,
                                     String reason) {
}

public record PaymentFailedEvent(String orderId, String reason) {
}

public record CompensatePaymentEvent(String orderId, String reason) {
}

public record PaymentRevertedEvent(String orderId, String reason) {
}
