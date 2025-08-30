package com.example.saga.coordinator.api;

import com.example.saga.coordinator.events.OrderCreatedEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final Supplier<Message<OrderCreatedEvent>> orderCreatedSupplier;

    public OrderController(Supplier<Message<OrderCreatedEvent>> orderCreatedSupplier) {
        this.orderCreatedSupplier = requireNonNull(orderCreatedSupplier);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> payload) {
        // Generate new orderId and send OrderCreated event via supplier
        String orderId = UUID.randomUUID().toString();
        payload.put("orderId", orderId);

        // enrich event
        OrderCreatedEvent event = new OrderCreatedEvent(orderId,
                (String) payload.getOrDefault("userId", "user-unknown"),
                (Double) payload.getOrDefault("amount", 10.0),
                (String) payload.getOrDefault("address", "unknown"));

        Message<OrderCreatedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader("eventType", "OrderCreated")
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                .build();

        // Use supplier to emit event
        PendingOrderPublisher.publishNext(message);

        return ResponseEntity.accepted().body(Map.of(
                "orderId", orderId,
                "status", "ORDER_ACCEPTED",
                "message", "Order accepted and processing started."
        ));
    }
}
