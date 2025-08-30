package com.example.saga.coordinator;

import com.example.saga.coordinator.events.CompensatePaymentEvent;
import com.example.saga.coordinator.events.Events;
import com.example.saga.coordinator.events.PaymentAuthorizedEvent;
import com.example.saga.coordinator.events.PaymentFailedEvent;
import com.example.saga.coordinator.events.ShipmentFailedEvent;
import com.example.saga.coordinator.events.ShipmentReservedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Configuration
public class CoordinatorConfig {

    private static final Logger log = LoggerFactory.getLogger(CoordinatorConfig.class);

    // In-memory order state
    private final Map<String, Events.OrderStatus> state = new ConcurrentHashMap<>();

    // Queue for compensation events to payments
    private final java.util.concurrent.BlockingQueue<Message<CompensatePaymentEvent>> compensationQueue = new java.util.concurrent.LinkedBlockingQueue<>();

    @Bean
    public Supplier<Message<CompensatePaymentEvent>> compensatePayment() {
        return compensationQueue::poll;
    }

    @Bean
    public Consumer<Message<PaymentAuthorizedEvent>> payments() {
        return message -> {
            PaymentAuthorizedEvent evt = message.getPayload();
            String orderId = evt.orderId();
            log.info("Payment authorized for order {}: {}", orderId, evt.paymentId());
            state.put(orderId, Events.OrderStatus.PAYMENT_AUTHORIZED);
        };
    }

    @Bean
    public Consumer<Message<PaymentFailedEvent>> paymentsFailed() {
        return message -> {
            PaymentFailedEvent evt = message.getPayload();
            String orderId = evt.orderId();
            log.info("Payment failed for order {}: {}", orderId, evt.reason());
            state.put(orderId, Events.OrderStatus.PAYMENT_FAILED);
            completeOrCompensate(orderId);
        };
    }

    @Bean
    public Consumer<Message<ShipmentReservedEvent>> shipments() {
        return message -> {
            ShipmentReservedEvent evt = message.getPayload();
            String orderId = evt.orderId();
            log.info("Shipment reserved for order {}: {}", orderId, evt.shipmentId());
            state.put(orderId, Events.OrderStatus.SHIPMENT_RESERVED);
            completeOrCompensate(orderId);
        };
    }

    @Bean
    public Consumer<Message<ShipmentFailedEvent>> shipmentsFailed() {
        return message -> {
            ShipmentFailedEvent evt = message.getPayload();
            String orderId = evt.orderId();
            log.info("Shipment failed for order {}: {}", orderId, evt.reason());
            state.put(orderId, Events.OrderStatus.SHIPMENT_FAILED);
            // Ask payment service to compensate
            var compensate = new CompensatePaymentEvent(orderId, "Shipment failed");
            Message<CompensatePaymentEvent> msg = MessageBuilder.withPayload(compensate)
                    .setHeader("eventType", "CompensatePayment")
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                    .build();
            compensationQueue.offer(msg);
        };
    }

    private void completeOrCompensate(String orderId) {
        // Simple completion rule: if shipment reserved and payment authorized, complete.
        // If payment failed or shipment failed -> will be compensated by the flows above.
        boolean paymentOk = state.containsValue(Events.OrderStatus.PAYMENT_AUTHORIZED) && Events.OrderStatus.PAYMENT_AUTHORIZED.equals(state.get(orderId));
        boolean shipmentOk = state.containsValue(Events.OrderStatus.SHIPMENT_RESERVED) && Events.OrderStatus.SHIPMENT_RESERVED.equals(state.get(orderId));
        if (paymentOk && shipmentOk) {
            log.info("Order {} completed.", orderId);
            state.put(orderId, Events.OrderStatus.COMPLETED);
        }
    }
}
