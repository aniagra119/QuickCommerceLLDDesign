package com.quickcommerce.inventory;

import com.quickcommerce.model.Item;
import com.quickcommerce.model.OrderItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class StoreInventory {
    private final String storeId;
    private final Map<String, StoreInventoryItem> inventoryMap = new ConcurrentHashMap<>();

    public StoreInventory(String storeId) {
        this.storeId = Objects.requireNonNull(storeId, "storeId cannot be null");
    }

    public String getStoreId() {
        return storeId;
    }

    public void addItem(Item item, int quantity) {
        inventoryMap.put(item.getItemId(), new StoreInventoryItem(item, quantity));
    }

    public StoreInventoryItem getInventoryItem(String itemId) {
        return inventoryMap.get(itemId);
    }

    public Map<String, StoreInventoryItem> getAllItems() {
        return Collections.unmodifiableMap(inventoryMap);
    }

    public synchronized boolean hasAvailableStock(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        for (OrderItem orderItem : items) {
            StoreInventoryItem invItem = inventoryMap.get(orderItem.getItem().getItemId());
            if (invItem == null || !invItem.hasSufficientStock(orderItem.getQuantity())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Atomically reserves inventory for an entire order.
     * All-or-Nothing semantics: if any item fails to reserve, rollback all preceding reservations.
     */
    public synchronized boolean reserveStock(List<OrderItem> items) {
        if (!hasAvailableStock(items)) {
            return false;
        }

        List<OrderItem> successfullyReserved = new ArrayList<>();
        for (OrderItem orderItem : items) {
            StoreInventoryItem invItem = inventoryMap.get(orderItem.getItem().getItemId());
            if (invItem != null && invItem.reserve(orderItem.getQuantity())) {
                successfullyReserved.add(orderItem);
            } else {
                // Rollback
                for (OrderItem reserved : successfullyReserved) {
                    StoreInventoryItem rollbackItem = inventoryMap.get(reserved.getItem().getItemId());
                    if (rollbackItem != null) {
                        rollbackItem.releaseReservation(reserved.getQuantity());
                    }
                }
                return false;
            }
        }
        return true;
    }

    public synchronized void commitStock(List<OrderItem> items) {
        for (OrderItem orderItem : items) {
            StoreInventoryItem invItem = inventoryMap.get(orderItem.getItem().getItemId());
            if (invItem != null) {
                invItem.commitReservation(orderItem.getQuantity());
            }
        }
    }

    public synchronized void releaseStock(List<OrderItem> items) {
        for (OrderItem orderItem : items) {
            StoreInventoryItem invItem = inventoryMap.get(orderItem.getItem().getItemId());
            if (invItem != null) {
                invItem.releaseReservation(orderItem.getQuantity());
            }
        }
    }
}
