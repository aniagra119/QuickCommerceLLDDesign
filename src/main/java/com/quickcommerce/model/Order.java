package com.quickcommerce.model;

import com.quickcommerce.enums.OrderStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Order {
    private final String orderId;
    private final User customer;
    private final Store store;
    private final List<OrderItem> items;
    private final double totalAmount;
    private final Location deliveryLocation;
    private volatile OrderStatus status;
    private volatile DeliveryTask deliveryTask;
    private final Instant createdAt;

    public Order(String orderId, User customer, Store store, List<OrderItem> items, Location deliveryLocation) {
        this.orderId = Objects.requireNonNull(orderId, "orderId cannot be null");
        this.customer = Objects.requireNonNull(customer, "customer cannot be null");
        this.store = Objects.requireNonNull(store, "store cannot be null");
        this.items = new ArrayList<>(Objects.requireNonNull(items, "items cannot be null"));
        this.deliveryLocation = Objects.requireNonNull(deliveryLocation, "deliveryLocation cannot be null");
        this.status = OrderStatus.PLACED;
        this.createdAt = Instant.now();
        this.totalAmount = calculateTotalAmount();
    }

    private double calculateTotalAmount() {
        double sum = 0.0;
        for (OrderItem item : items) {
            sum += item.getSubtotal();
        }
        return Math.round(sum * 100.0) / 100.0;
    }

    public String getOrderId() {
        return orderId;
    }

    public User getCustomer() {
        return customer;
    }

    public Store getStore() {
        return store;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public Location getDeliveryLocation() {
        return deliveryLocation;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public DeliveryTask getDeliveryTask() {
        return deliveryTask;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public synchronized void setStatus(OrderStatus status) {
        this.status = Objects.requireNonNull(status, "status cannot be null");
    }

    public synchronized void attachDeliveryTask(DeliveryTask deliveryTask) {
        this.deliveryTask = Objects.requireNonNull(deliveryTask, "deliveryTask cannot be null");
    }
}
