package com.quickcommerce.service;

import com.quickcommerce.enums.OrderStatus;
import com.quickcommerce.event.OrderDeliveredEvent;
import com.quickcommerce.event.OrderDispatchedEvent;
import com.quickcommerce.event.OrderEventPublisher;
import com.quickcommerce.event.OrderPlacedEvent;
import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.DeliveryTask;
import com.quickcommerce.model.Location;
import com.quickcommerce.model.Order;
import com.quickcommerce.model.OrderItem;
import com.quickcommerce.model.Store;
import com.quickcommerce.model.User;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OrderService {
    private final DispatchService dispatchService;
    private final OrderEventPublisher eventPublisher;
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public OrderService(DispatchService dispatchService, OrderEventPublisher eventPublisher) {
        this.dispatchService = Objects.requireNonNull(dispatchService, "dispatchService cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher cannot be null");
    }

    public Order placeOrder(User customer, Store store, List<OrderItem> items, Location deliveryLocation) {
        Objects.requireNonNull(customer, "customer cannot be null");
        Objects.requireNonNull(store, "store cannot be null");
        Objects.requireNonNull(deliveryLocation, "deliveryLocation cannot be null");
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // 1. Inventory Check & Atomic Reservation
        boolean stockReserved = store.getInventory().reserveStock(items);
        if (!stockReserved) {
            throw new IllegalStateException("Store " + store.getName() + " does not have sufficient inventory for this order");
        }

        // 2. Create Order
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8);
        Order order = new Order(orderId, customer, store, items, deliveryLocation);
        order.setStatus(OrderStatus.CONFIRMED);

        // 3. Create DeliveryTask
        String taskId = "TSK-" + UUID.randomUUID().toString().substring(0, 8);
        DeliveryTask task = new DeliveryTask(taskId, orderId, store.getLocation(), deliveryLocation);
        order.attachDeliveryTask(task);

        // 4. Try immediate dispatch
        DeliveryPartner partner = dispatchService.dispatchTask(task);
        if (partner != null) {
            order.setStatus(OrderStatus.ASSIGNED);
        }

        orders.put(orderId, order);
        eventPublisher.publishOrderPlaced(new OrderPlacedEvent("EVT-" + UUID.randomUUID(), order, Instant.now()));
        return order;
    }

    public void pickUpOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        if (order.getDeliveryTask() == null || order.getDeliveryTask().getAssignedPartner() == null) {
            throw new IllegalStateException("Order cannot be picked up without an assigned partner");
        }

        order.getStore().getInventory().commitStock(order.getItems());
        order.getDeliveryTask().markPickedUp();
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);

        eventPublisher.publishOrderDispatched(new OrderDispatchedEvent("EVT-" + UUID.randomUUID(), order, Instant.now()));
    }

    public void deliverOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }

        order.getDeliveryTask().markDelivered();
        order.setStatus(OrderStatus.DELIVERED);

        eventPublisher.publishOrderDelivered(new OrderDeliveredEvent("EVT-" + UUID.randomUUID(), order, Instant.now()));
    }

    public void cancelOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {
            throw new IllegalStateException("Cannot cancel order once out for delivery or delivered");
        }

        // Release inventory reservation
        order.getStore().getInventory().releaseStock(order.getItems());
        if (order.getDeliveryTask() != null) {
            order.getDeliveryTask().markFailed();
        }
        order.setStatus(OrderStatus.CANCELLED);
    }

    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }
}
