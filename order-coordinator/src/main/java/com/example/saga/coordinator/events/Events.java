package com.example.saga.coordinator.events;

public class Events {
    public enum OrderStatus {NEW, PAYMENT_AUTHORIZED, PAYMENT_FAILED, SHIPMENT_RESERVED, SHIPMENT_FAILED, COMPLETED, COMPENSATED}
}
