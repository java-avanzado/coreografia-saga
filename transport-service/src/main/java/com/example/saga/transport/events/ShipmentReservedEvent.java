package com.example.saga.transport.events;

public record ShipmentReservedEvent(String orderId, String shipmentId,
                                    String reason) {
}
