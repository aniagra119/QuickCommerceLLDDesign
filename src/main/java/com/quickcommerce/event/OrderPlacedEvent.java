package com.quickcommerce.event;

import com.quickcommerce.model.Order;
import java.time.Instant;

public class OrderPlacedEvent extends OrderEvent {
    public OrderPlacedEvent(String eventId, Order order, Instant timestamp) {
        super(eventId, order, timestamp);
    }
}
