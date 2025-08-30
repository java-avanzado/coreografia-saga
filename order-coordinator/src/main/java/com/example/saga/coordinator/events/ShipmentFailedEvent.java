package com.example.saga.coordinator.events;

public record ShipmentFailedEvent(String orderId, String reason) {
}
