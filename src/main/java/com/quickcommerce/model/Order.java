package com.quickcommerce.model;

import com.quickcommerce.enums.OrderStatus;
import java.time.Duration;
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
    private volatile Payment payment;
    private final Instant createdAt;
    private final Instant estimatedDeliveryTime;
    private volatile Instant actualDeliveryTime;
    private final int priorityScore;
    private final Duration preparationTimeEstimate;

    public Order(String orderId, User customer, Store store, List<OrderItem> items, Location deliveryLocation, double totalAmount) {
        this.orderId = Objects.requireNonNull(orderId, "orderId cannot be null");
        this.customer = Objects.requireNonNull(customer, "customer cannot be null");
        this.store = Objects.requireNonNull(store, "store cannot be null");
        this.items = new ArrayList<>(Objects.requireNonNull(items, "items cannot be null"));
        this.deliveryLocation = Objects.requireNonNull(deliveryLocation, "deliveryLocation cannot be null");
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PLACED;
        this.createdAt = Instant.now();
        this.estimatedDeliveryTime = this.createdAt.plus(Duration.ofMinutes(15));
        this.preparationTimeEstimate = Duration.ofMinutes(5);
        this.priorityScore = 5;
    }

    public Order(String orderId, User customer, Store store, List<OrderItem> items, Location deliveryLocation) {
        this(orderId, customer, store, items, deliveryLocation, calculateDefaultTotal(items));
    }

    private static double calculateDefaultTotal(List<OrderItem> items) {
        double sum = 0.0;
        if (items != null) {
            for (OrderItem item : items) {
                sum += item.getSubtotal();
            }
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

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public DeliveryTask getDeliveryTask() {
        return deliveryTask;
    }

    public void attachDeliveryTask(DeliveryTask deliveryTask) {
        this.deliveryTask = deliveryTask;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public Instant getActualDeliveryTime() {
        return actualDeliveryTime;
    }

    public void setActualDeliveryTime(Instant actualDeliveryTime) {
        this.actualDeliveryTime = actualDeliveryTime;
    }

    public int getPriorityScore() {
        return priorityScore;
    }

    public Duration getPreparationTimeEstimate() {
        return preparationTimeEstimate;
    }

    public boolean isSlaBreached() {
        if (actualDeliveryTime != null) {
            return actualDeliveryTime.isAfter(estimatedDeliveryTime);
        }
        return Instant.now().isAfter(estimatedDeliveryTime) && status != OrderStatus.DELIVERED;
    }

    public Duration getDeliveryDuration() {
        if (actualDeliveryTime != null) {
            return Duration.between(createdAt, actualDeliveryTime);
        }
        return null;
    }
}
