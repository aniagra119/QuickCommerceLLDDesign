package com.quickcommerce.inventory;

import com.quickcommerce.model.Item;
import java.util.Objects;

public class StoreInventoryItem {
    private final Item item;
    private int availableQuantity;
    private int reservedQuantity;

    public StoreInventoryItem(Item item, int initialQuantity) {
        this.item = Objects.requireNonNull(item, "item cannot be null");
        if (initialQuantity < 0) {
            throw new IllegalArgumentException("Initial quantity cannot be negative");
        }
        this.availableQuantity = initialQuantity;
        this.reservedQuantity = 0;
    }

    public Item getItem() {
        return item;
    }

    public synchronized int getAvailableQuantity() {
        return availableQuantity;
    }

    public synchronized int getReservedQuantity() {
        return reservedQuantity;
    }

    public synchronized int getTotalQuantity() {
        return availableQuantity + reservedQuantity;
    }

    public synchronized void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }
        this.availableQuantity += quantity;
    }

    public synchronized boolean hasSufficientStock(int quantity) {
        return this.availableQuantity >= quantity;
    }

    public synchronized boolean reserve(int quantity) {
        if (quantity <= 0 || this.availableQuantity < quantity) {
            return false;
        }
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
        return true;
    }

    public synchronized void commitReservation(int quantity) {
        if (quantity > this.reservedQuantity) {
            throw new IllegalStateException("Cannot commit more than reserved quantity");
        }
        this.reservedQuantity -= quantity;
    }

    public synchronized void releaseReservation(int quantity) {
        if (quantity > this.reservedQuantity) {
            throw new IllegalStateException("Cannot release more than reserved quantity");
        }
        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
    }
}
