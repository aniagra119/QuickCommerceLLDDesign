package com.quickcommerce.event;

public interface IOrderEventPublisher {
    void registerListener(OrderEventListener listener);
    void removeListener(OrderEventListener listener);
    void publish(OrderEvent event);
}
