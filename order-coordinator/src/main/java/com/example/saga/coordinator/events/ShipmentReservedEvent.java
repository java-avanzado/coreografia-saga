package com.example.saga.coordinator.events;

public record ShipmentReservedEvent(String orderId, String shipmentId,
                                    String reason) {
}
