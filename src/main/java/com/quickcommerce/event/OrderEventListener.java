package com.quickcommerce.event;

public interface OrderEventListener {
    void onOrderPlaced(OrderPlacedEvent event);
    void onOrderDispatched(OrderDispatchedEvent event);
    void onOrderDelivered(OrderDeliveredEvent event);
}
