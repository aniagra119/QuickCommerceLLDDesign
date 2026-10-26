package com.quickcommerce.inventory;

import com.quickcommerce.model.OrderItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InventoryTransactionManager {

    public synchronized boolean hasAvailableStock(StoreInventory inventory, List<OrderItem> items) {
        Objects.requireNonNull(inventory, "inventory cannot be null");
        if (items == null || items.isEmpty()) {
            return false;
        }
        for (OrderItem orderItem : items) {
            StoreInventoryItem invItem = inventory.getInventoryItem(orderItem.getItem().getItemId());
            if (invItem == null || !invItem.hasSufficientStock(orderItem.getQuantity())) {
                return false;
            }
        }
        return true;
    }

    public synchronized boolean reserveStock(StoreInventory inventory, List<OrderItem> items) {
        if (!hasAvailableStock(inventory, items)) {
            return false;
        }

        List<OrderItem> successfullyReserved = new ArrayList<>();
        for (OrderItem orderItem : items) {
            StoreInventoryItem invItem = inventory.getInventoryItem(orderItem.getItem().getItemId());
            boolean success = invItem.reserve(orderItem.getQuantity());
            if (success) {
                successfullyReserved.add(orderItem);
            } else {
                // All-or-nothing rollback
                for (OrderItem rollbackItem : successfullyReserved) {
                    StoreInventoryItem rollbackInv = inventory.getInventoryItem(rollbackItem.getItem().getItemId());
                    rollbackInv.releaseReservation(rollbackItem.getQuantity());
                }
                return false;
            }
        }
        return true;
    }

    public synchronized void commitStock(StoreInventory inventory, List<OrderItem> items) {
        if (items == null) return;
        for (OrderItem orderItem : items) {
            StoreInventoryItem invItem = inventory.getInventoryItem(orderItem.getItem().getItemId());
            if (invItem != null) {
                invItem.commitReservation(orderItem.getQuantity());
            }
        }
    }

    public synchronized void releaseStock(StoreInventory inventory, List<OrderItem> items) {
        if (items == null) return;
        for (OrderItem orderItem : items) {
            StoreInventoryItem invItem = inventory.getInventoryItem(orderItem.getItem().getItemId());
            if (invItem != null) {
                invItem.releaseReservation(orderItem.getQuantity());
            }
        }
    }
}
