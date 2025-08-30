package com.example.saga.payment;

import com.example.saga.payment.events.CompensatePaymentEvent;
import com.example.saga.payment.events.OrderCreatedEvent;
import com.example.saga.payment.events.PaymentAuthorizedEvent;
import com.example.saga.payment.events.PaymentFailedEvent;
import com.example.saga.payment.events.PaymentRevertedEvent;
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
public class PaymentProcessorConfig {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessorConfig.class);
    private final StreamBridge streamBridge;
    private final Random random = new Random();

    public PaymentProcessorConfig(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Bean
    public Consumer<Message<OrderCreatedEvent>> processOrder() {
        return message -> {
            OrderCreatedEvent evt = message.getPayload();
            String orderId = evt.orderId();

            // Configurable failure ratio via env var PAYMENT_FAIL_RATIO (0.0 - 1.0)
            double failRatio = Double.parseDouble(System.getenv().getOrDefault("PAYMENT_FAIL_RATIO", "0.2"));
            boolean fail = random.nextDouble() < failRatio;

            if (!fail) {
                var out = new PaymentAuthorizedEvent(orderId, "pay-" + Math.abs(random.nextInt()), "OK");
                Message<PaymentAuthorizedEvent> msg = MessageBuilder.withPayload(out)
                        .setHeader("eventType", "PaymentAuthorized")
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                        .build();
                log.info("Payment authorized for order {}", orderId);
                streamBridge.send("paymentsAuthorized-out-0", msg);
            } else {
                var out = new PaymentFailedEvent(orderId, "Payment declined");
                Message<PaymentFailedEvent> msg = MessageBuilder.withPayload(out)
                        .setHeader("eventType", "PaymentFailed")
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                        .build();
                log.info("Payment failed for order {}", orderId);
                streamBridge.send("paymentsFailed-out-0", msg);
            }
        };
    }

    @Bean
    public Consumer<Message<CompensatePaymentEvent>> compensate() {
        return message -> {
            CompensatePaymentEvent evt = message.getPayload();
            String orderId = evt.orderId();
            log.info("Compensating payment for order {} due to: {}", orderId, evt.reason());
            var out = new PaymentRevertedEvent(orderId, "Payment refunded");
            Message<PaymentRevertedEvent> msg = MessageBuilder.withPayload(out)
                    .setHeader("eventType", "PaymentReverted")
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                    .build();
            streamBridge.send("paymentsReverted-out-0", msg);
        };
    }
}
