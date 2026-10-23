package com.quickcommerce.event.listener;

import com.quickcommerce.event.OrderDeliveredEvent;
import com.quickcommerce.event.OrderDispatchedEvent;
import com.quickcommerce.event.OrderEventListener;
import com.quickcommerce.event.OrderPlacedEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StorePackingKdsListener implements OrderEventListener {
    private final List<String> activePackingTickets = new ArrayList<>();

    @Override
    public void onOrderPlaced(OrderPlacedEvent event) {
        activePackingTickets.add("PACKING TICKET: Store " + event.getOrder().getStore().getName() + " - Order " + event.getOrder().getOrderId() + " (" + event.getOrder().getItems().size() + " items)");
    }

    @Override
    public void onOrderDispatched(OrderDispatchedEvent event) {
        activePackingTickets.removeIf(t -> t.contains(event.getOrder().getOrderId()));
    }

    @Override
    public void onOrderDelivered(OrderDeliveredEvent event) {
    }

    public List<String> getActivePackingTickets() {
        return Collections.unmodifiableList(activePackingTickets);
    }
}
