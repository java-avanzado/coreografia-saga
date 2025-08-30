package com.example.saga.transport;

import com.example.saga.transport.events.PaymentAuthorizedEvent;
import com.example.saga.transport.events.ShipmentFailedEvent;
import com.example.saga.transport.events.ShipmentReservedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

import java.util.Random;
import java.util.function.Consumer;

@Configuration
public class TransportProcessorConfig {

    private static final Logger log = LoggerFactory.getLogger(TransportProcessorConfig.class);
    private final StreamBridge streamBridge;
    private final Random random = new Random();

    public TransportProcessorConfig(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Bean
    public Consumer<Message<PaymentAuthorizedEvent>> reserveShipment() {
        return message -> {
            PaymentAuthorizedEvent evt = message.getPayload();
            String orderId = evt.orderId();
            double failRatio = Double.parseDouble(System.getenv().getOrDefault("SHIPMENT_FAIL_RATIO", "0.2"));
            boolean fail = random.nextDouble() < failRatio;
            if (!fail) {
                var out = new ShipmentReservedEvent(orderId, "ship-" + Math.abs(random.nextInt()), "OK");
                Message<ShipmentReservedEvent> msg = MessageBuilder.withPayload(out)
                        .setHeader("eventType", "ShipmentReserved")
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                        .build();
                log.info("Shipment reserved for order {}", orderId);
                streamBridge.send("shipmentsReserved-out-0", msg);
            } else {
                var out = new ShipmentFailedEvent(orderId, "No carriers available");
                Message<ShipmentFailedEvent> msg = MessageBuilder.withPayload(out)
                        .setHeader("eventType", "ShipmentFailed")
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                        .build();
                log.info("Shipment failed for order {}", orderId);
                streamBridge.send("shipmentsFailed-out-0", msg);
            }
        };
    }
}
