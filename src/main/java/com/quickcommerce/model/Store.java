package com.quickcommerce.model;

import com.quickcommerce.inventory.StoreInventory;
import java.util.Objects;

public class Store {
    private final String storeId;
    private final String name;
    private final Location location;
    private final Address address;
    private final StoreInventory inventory;

    public Store(String storeId, String name, Location location, Address address) {
        this.storeId = Objects.requireNonNull(storeId, "storeId cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.location = Objects.requireNonNull(location, "location cannot be null");
        this.address = Objects.requireNonNull(address, "address cannot be null");
        this.inventory = new StoreInventory(storeId);
    }

    public String getStoreId() {
        return storeId;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public Address getAddress() {
        return address;
    }

    public StoreInventory getInventory() {
        return inventory;
    }
}
