package com.quickcommerce.service;

import com.quickcommerce.matching.PartnerMatchingStrategy;
import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.Location;
import com.quickcommerce.model.Order;
import com.quickcommerce.model.OrderItem;
import com.quickcommerce.model.Store;
import com.quickcommerce.model.User;
import com.quickcommerce.tracking.PartnerLocationTracker;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class QuickCommercePlatform {
    private final Map<String, Store> stores = new ConcurrentHashMap<>();
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final DispatchService dispatchService;
    private final OrderService orderService;
    private final PartnerLocationTracker locationTracker;

    public QuickCommercePlatform(DispatchService dispatchService, OrderService orderService, PartnerLocationTracker locationTracker) {
        this.dispatchService = Objects.requireNonNull(dispatchService, "dispatchService cannot be null");
        this.orderService = Objects.requireNonNull(orderService, "orderService cannot be null");
        this.locationTracker = Objects.requireNonNull(locationTracker, "locationTracker cannot be null");
    }

    public void registerStore(Store store) {
        stores.put(store.getStoreId(), Objects.requireNonNull(store, "store cannot be null"));
    }

    public void registerUser(User user) {
        users.put(user.getUserId(), Objects.requireNonNull(user, "user cannot be null"));
    }

    public void registerPartner(DeliveryPartner partner) {
        dispatchService.registerPartner(partner);
        locationTracker.registerPartner(partner);
    }

    public void updatePartnerLocation(String partnerId, Location location, Instant timestamp) {
        locationTracker.recordLocationPing(partnerId, location, timestamp);
    }

    public void setPartnerMatchingStrategy(PartnerMatchingStrategy strategy) {
        dispatchService.setMatchingStrategy(strategy);
    }

    public Order placeOrder(String userId, String storeId, List<OrderItem> items, Location deliveryLocation) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        Store store = stores.get(storeId);
        if (store == null) {
            throw new IllegalArgumentException("Store not found: " + storeId);
        }

        return orderService.placeOrder(user, store, items, deliveryLocation);
    }

    public void pickUpOrder(String orderId) {
        orderService.pickUpOrder(orderId);
    }

    public void deliverOrder(String orderId) {
        orderService.deliverOrder(orderId);
    }

    public void cancelOrder(String orderId) {
        orderService.cancelOrder(orderId);
    }

    public List<Store> getStores() {
        return Collections.unmodifiableList(new ArrayList<>(stores.values()));
    }

    public Location getPartnerLocation(String partnerId) {
        return locationTracker.getCurrentLocation(partnerId);
    }
}
