package com.quickcommerce;

import com.quickcommerce.enums.ItemCategory;
import com.quickcommerce.inventory.StoreInventory;
import com.quickcommerce.inventory.StoreInventoryItem;
import com.quickcommerce.model.Item;
import com.quickcommerce.model.OrderItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Store Inventory & Atomic Reservation Tests")
class StoreInventoryReservationTest {

    @Test
    @DisplayName("Should successfully reserve stock when sufficient inventory exists")
    void shouldReserveStockSuccessfully() {
        StoreInventory inventory = new StoreInventory("STORE-1");
        Item milk = new Item("I-1", "Amul Milk 500ml", "Toned fresh milk", 35.0, ItemCategory.DAIRY_AND_BREAD, 0.5);
        Item bread = new Item("I-2", "Modern Brown Bread", "Whole wheat bread", 45.0, ItemCategory.DAIRY_AND_BREAD, 0.4);

        inventory.addItem(milk, 10);
        inventory.addItem(bread, 5);

        List<OrderItem> items = List.of(
                new OrderItem("OI-1", milk, 3),
                new OrderItem("OI-2", bread, 2)
        );

        boolean reserved = inventory.reserveStock(items);
        assertTrue(reserved);

        StoreInventoryItem milkInv = inventory.getInventoryItem("I-1");
        assertEquals(7, milkInv.getAvailableQuantity());
        assertEquals(3, milkInv.getReservedQuantity());

        StoreInventoryItem breadInv = inventory.getInventoryItem("I-2");
        assertEquals(3, breadInv.getAvailableQuantity());
        assertEquals(2, breadInv.getReservedQuantity());
    }

    @Test
    @DisplayName("Should rollback all reservations if any item lacks sufficient inventory (All-or-Nothing)")
    void shouldRollbackIfAnyItemFails() {
        StoreInventory inventory = new StoreInventory("STORE-1");
        Item apple = new Item("I-1", "Shimla Apple", "Fresh crisp apples", 120.0, ItemCategory.FRUITS_AND_VEGETABLES, 1.0);
        Item avocado = new Item("I-2", "Hass Avocado", "Imported avocado", 200.0, ItemCategory.FRUITS_AND_VEGETABLES, 0.3);

        inventory.addItem(apple, 10);
        inventory.addItem(avocado, 1); // Only 1 in stock

        List<OrderItem> items = List.of(
                new OrderItem("OI-1", apple, 5),    // Available (10 >= 5)
                new OrderItem("OI-2", avocado, 3)   // Insufficient! (1 < 3)
        );

        boolean reserved = inventory.reserveStock(items);
        assertFalse(reserved);

        // Apple must be rolled back completely
        StoreInventoryItem appleInv = inventory.getInventoryItem("I-1");
        assertEquals(10, appleInv.getAvailableQuantity());
        assertEquals(0, appleInv.getReservedQuantity());
    }
}
