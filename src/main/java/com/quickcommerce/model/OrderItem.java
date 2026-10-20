package com.quickcommerce.model;

import java.util.Objects;

public class OrderItem {
    private final String orderItemId;
    private final Item item;
    private final int quantity;
    private final double unitPrice;

    public OrderItem(String orderItemId, Item item, int quantity) {
        this.orderItemId = Objects.requireNonNull(orderItemId, "orderItemId cannot be null");
        this.item = Objects.requireNonNull(item, "item cannot be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.quantity = quantity;
        this.unitPrice = item.getPrice();
    }

    public String getOrderItemId() {
        return orderItemId;
    }

    public Item getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getSubtotal() {
        return Math.round((unitPrice * quantity) * 100.0) / 100.0;
    }
}
