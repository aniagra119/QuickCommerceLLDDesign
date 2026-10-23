package com.quickcommerce.event;

import com.quickcommerce.model.Order;
import java.time.Instant;
import java.util.Objects;

public abstract class OrderEvent {
    private final String eventId;
    private final Order order;
    private final Instant timestamp;

    public OrderEvent(String eventId, Order order, Instant timestamp) {
        this.eventId = Objects.requireNonNull(eventId, "eventId cannot be null");
        this.order = Objects.requireNonNull(order, "order cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp cannot be null");
    }

    public String getEventId() {
        return eventId;
    }

    public Order getOrder() {
        return order;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
