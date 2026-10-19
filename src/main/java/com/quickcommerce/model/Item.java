package com.quickcommerce.model;

import com.quickcommerce.enums.ItemCategory;
import java.util.Objects;

public class Item {
    private final String itemId;
    private final String name;
    private final String description;
    private final double price;
    private final ItemCategory category;
    private final double weightKg;

    public Item(String itemId, String name, String description, double price, ItemCategory category, double weightKg) {
        this.itemId = Objects.requireNonNull(itemId, "itemId cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.description = Objects.requireNonNull(description, "description cannot be null");
        this.price = price;
        this.category = Objects.requireNonNull(category, "category cannot be null");
        this.weightKg = weightKg;
    }

    public String getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public double getWeightKg() {
        return weightKg;
    }
}
