package com.quickcommerce.event;

import com.quickcommerce.model.Order;
import java.time.Instant;

public class OrderDeliveredEvent extends OrderEvent {
    public OrderDeliveredEvent(String eventId, Order order, Instant timestamp) {
        super(eventId, order, timestamp);
    }
}
