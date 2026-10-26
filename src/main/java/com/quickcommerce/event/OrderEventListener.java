package com.quickcommerce.event;

public interface OrderEventListener {
    default void onEvent(OrderEvent event) {
        if (event instanceof OrderPlacedEvent placed) {
            onOrderPlaced(placed);
        } else if (event instanceof OrderDispatchedEvent dispatched) {
            onOrderDispatched(dispatched);
        } else if (event instanceof OrderDeliveredEvent delivered) {
            onOrderDelivered(delivered);
        }
    }

    default boolean supportsEventType(Class<? extends OrderEvent> eventType) {
        return OrderEvent.class.isAssignableFrom(eventType);
    }

    default void onOrderPlaced(OrderPlacedEvent event) {}
    default void onOrderDispatched(OrderDispatchedEvent event) {}
    default void onOrderDelivered(OrderDeliveredEvent event) {}
}
