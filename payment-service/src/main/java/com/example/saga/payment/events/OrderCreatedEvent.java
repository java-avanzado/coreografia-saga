package com.example.saga.payment.events;

public record OrderCreatedEvent(String orderId, String userId, double amount,
                                String address) {
}
