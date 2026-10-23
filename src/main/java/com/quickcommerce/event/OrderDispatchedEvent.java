package com.quickcommerce.event;

import com.quickcommerce.model.Order;
import java.time.Instant;

public class OrderDispatchedEvent extends OrderEvent {
    public OrderDispatchedEvent(String eventId, Order order, Instant timestamp) {
        super(eventId, order, timestamp);
    }
}
