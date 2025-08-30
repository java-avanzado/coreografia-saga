package com.example.saga.coordinator.api;

import com.example.saga.coordinator.events.OrderCreatedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

@Component
public class PendingOrderPublisher {

    private static final BlockingQueue<Message<OrderCreatedEvent>> queue = new LinkedBlockingQueue<>();

    public static void publishNext(Message<OrderCreatedEvent> message) {
        queue.offer(message);
    }

    @Bean
    public Supplier<Message<OrderCreatedEvent>> orderCreated() {
        return queue::poll; // returns null if empty (no emission)
    }
}
