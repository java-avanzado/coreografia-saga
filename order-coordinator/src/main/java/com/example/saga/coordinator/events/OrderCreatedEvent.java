package com.example.saga.coordinator.events;

public record OrderCreatedEvent(String orderId, String userId, double amount,
                                String address) {
}
