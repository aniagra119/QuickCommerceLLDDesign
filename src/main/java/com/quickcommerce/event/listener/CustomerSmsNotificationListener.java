package com.quickcommerce.event.listener;

import com.quickcommerce.event.OrderDeliveredEvent;
import com.quickcommerce.event.OrderDispatchedEvent;
import com.quickcommerce.event.OrderEventListener;
import com.quickcommerce.event.OrderPlacedEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomerSmsNotificationListener implements OrderEventListener {
    private final List<String> sentAlerts = new ArrayList<>();

    @Override
    public void onOrderPlaced(OrderPlacedEvent event) {
        sentAlerts.add("SMS to " + event.getOrder().getCustomer().getPhoneNumber() + ": Order " + event.getOrder().getOrderId() + " placed! Total: ₹" + event.getOrder().getTotalAmount());
    }

    @Override
    public void onOrderDispatched(OrderDispatchedEvent event) {
        sentAlerts.add("SMS to " + event.getOrder().getCustomer().getPhoneNumber() + ": Order " + event.getOrder().getOrderId() + " out for delivery with " + event.getOrder().getDeliveryTask().getAssignedPartner().getName());
    }

    @Override
    public void onOrderDelivered(OrderDeliveredEvent event) {
        sentAlerts.add("SMS to " + event.getOrder().getCustomer().getPhoneNumber() + ": Order " + event.getOrder().getOrderId() + " delivered safely at your doorstep!");
    }

    public List<String> getSentAlerts() {
        return Collections.unmodifiableList(sentAlerts);
    }
}
