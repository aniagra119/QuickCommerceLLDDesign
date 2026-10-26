package com.quickcommerce.service;

import com.quickcommerce.model.Location;
import com.quickcommerce.model.OrderItem;
import com.quickcommerce.model.Store;
import java.util.List;
import java.util.Objects;

public class OrderPricingService {

    public double calculateTotal(List<OrderItem> items, Store store, Location deliveryLocation) {
        Objects.requireNonNull(items, "items cannot be null");
        Objects.requireNonNull(store, "store cannot be null");
        Objects.requireNonNull(deliveryLocation, "deliveryLocation cannot be null");

        double itemsSubtotal = 0.0;
        for (OrderItem item : items) {
            itemsSubtotal += item.getSubtotal();
        }

        double distanceKm = store.getLocation().distanceTo(deliveryLocation);
        double deliveryFee = (distanceKm < 3.0) ? 20.0 : 40.0;
        double taxes = Math.round(itemsSubtotal * 0.05 * 100.0) / 100.0; // 5% GST

        double total = itemsSubtotal + deliveryFee + taxes;
        return Math.round(total * 100.0) / 100.0;
    }

    public double calculateItemsSubtotal(List<OrderItem> items) {
        double itemsSubtotal = 0.0;
        for (OrderItem item : items) {
            itemsSubtotal += item.getSubtotal();
        }
        return Math.round(itemsSubtotal * 100.0) / 100.0;
    }
}
