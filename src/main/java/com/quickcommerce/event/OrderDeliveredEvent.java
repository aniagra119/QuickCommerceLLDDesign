package com.quickcommerce.event;

import com.quickcommerce.model.Order;
import java.time.Instant;

public class OrderDeliveredEvent extends OrderEvent {
    public OrderDeliveredEvent(String eventId, Order order, Instant timestamp) {
        super(eventId, order, timestamp);
    }

    public OrderDeliveredEvent(Order order) {
        super("EVT-" + java.util.UUID.randomUUID().toString().substring(0, 8), order, java.time.Instant.now());
    }
}
