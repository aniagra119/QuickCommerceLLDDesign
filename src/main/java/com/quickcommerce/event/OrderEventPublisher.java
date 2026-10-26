package com.quickcommerce.event;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class OrderEventPublisher implements IOrderEventPublisher {
    private final List<OrderEventListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void registerListener(OrderEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(OrderEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void publish(OrderEvent event) {
        Objects.requireNonNull(event, "event cannot be null");
        for (OrderEventListener listener : listeners) {
            if (listener.supportsEventType(event.getClass())) {
                listener.onEvent(event);
            }
        }
    }

    public void publishOrderPlaced(OrderPlacedEvent event) {
        publish(event);
    }

    public void publishOrderDispatched(OrderDispatchedEvent event) {
        publish(event);
    }

    public void publishOrderDelivered(OrderDeliveredEvent event) {
        publish(event);
    }
}
