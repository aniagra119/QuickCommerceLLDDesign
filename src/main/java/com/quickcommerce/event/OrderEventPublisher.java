package com.quickcommerce.event;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class OrderEventPublisher {
    private final List<OrderEventListener> listeners = new CopyOnWriteArrayList<>();

    public void registerListener(OrderEventListener listener) {
        this.listeners.add(Objects.requireNonNull(listener, "listener cannot be null"));
    }

    public void removeListener(OrderEventListener listener) {
        this.listeners.remove(listener);
    }

    public void publishOrderPlaced(OrderPlacedEvent event) {
        for (OrderEventListener listener : listeners) {
            listener.onOrderPlaced(event);
        }
    }

    public void publishOrderDispatched(OrderDispatchedEvent event) {
        for (OrderEventListener listener : listeners) {
            listener.onOrderDispatched(event);
        }
    }

    public void publishOrderDelivered(OrderDeliveredEvent event) {
        for (OrderEventListener listener : listeners) {
            listener.onOrderDelivered(event);
        }
    }
}
