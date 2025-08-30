package com.example.saga.transport.events;

public record ShipmentFailedEvent(String orderId, String reason) {
}
