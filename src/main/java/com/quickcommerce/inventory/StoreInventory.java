package com.quickcommerce.inventory;

import com.quickcommerce.model.Item;
import com.quickcommerce.model.OrderItem;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class StoreInventory {
    private final String storeId;
    private final Map<String, StoreInventoryItem> inventoryMap = new ConcurrentHashMap<>();
    private final InventoryTransactionManager transactionManager = new InventoryTransactionManager();

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

    public boolean hasAvailableStock(List<OrderItem> items) {
        return transactionManager.hasAvailableStock(this, items);
    }

    public boolean reserveStock(List<OrderItem> items) {
        return transactionManager.reserveStock(this, items);
    }

    public void commitStock(List<OrderItem> items) {
        transactionManager.commitStock(this, items);
    }

    public void releaseStock(List<OrderItem> items) {
        transactionManager.releaseStock(this, items);
    }
}
