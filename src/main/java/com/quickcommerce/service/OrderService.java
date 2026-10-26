package com.quickcommerce.service;

import com.quickcommerce.enums.OrderStatus;
import com.quickcommerce.enums.PaymentMethod;
import com.quickcommerce.event.IOrderEventPublisher;
import com.quickcommerce.event.OrderDeliveredEvent;
import com.quickcommerce.event.OrderDispatchedEvent;
import com.quickcommerce.event.OrderEventPublisher;
import com.quickcommerce.event.OrderPlacedEvent;
import com.quickcommerce.inventory.InventoryTransactionManager;
import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.DeliveryTask;
import com.quickcommerce.model.Location;
import com.quickcommerce.model.Order;
import com.quickcommerce.model.OrderItem;
import com.quickcommerce.model.Payment;
import com.quickcommerce.model.Store;
import com.quickcommerce.model.User;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OrderService implements IOrderService {
    private final IDispatchService dispatchService;
    private final IOrderEventPublisher eventPublisher;
    private final OrderPricingService pricingService;
    private final InventoryTransactionManager transactionManager;
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public OrderService(IDispatchService dispatchService,
                        IOrderEventPublisher eventPublisher,
                        OrderPricingService pricingService,
                        InventoryTransactionManager transactionManager) {
        this.dispatchService = Objects.requireNonNull(dispatchService, "dispatchService cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher cannot be null");
        this.pricingService = Objects.requireNonNull(pricingService, "pricingService cannot be null");
        this.transactionManager = Objects.requireNonNull(transactionManager, "transactionManager cannot be null");
    }

    public OrderService(IDispatchService dispatchService, IOrderEventPublisher eventPublisher) {
        this(dispatchService, eventPublisher, new OrderPricingService(), new InventoryTransactionManager());
    }

    @Override
    public Order placeOrder(User customer, Store store, List<OrderItem> items, Location deliveryLocation) {
        Objects.requireNonNull(customer, "customer cannot be null");
        Objects.requireNonNull(store, "store cannot be null");
        Objects.requireNonNull(deliveryLocation, "deliveryLocation cannot be null");
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // 1. Inventory Check & Atomic Reservation via InventoryTransactionManager (SRP)
        boolean stockReserved = transactionManager.reserveStock(store.getInventory(), items);
        if (!stockReserved) {
            throw new IllegalStateException("Store " + store.getName() + " does not have sufficient inventory for this order");
        }

        // 2. Calculate Total via OrderPricingService (SRP)
        double totalAmount = pricingService.calculateTotal(items, store, deliveryLocation);

        // 3. Create Order
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8);
        Order order = new Order(orderId, customer, store, items, deliveryLocation, totalAmount);
        order.setStatus(OrderStatus.CONFIRMED);

        // 4. Attach Default Prepaid/COD Payment record
        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8);
        Payment payment = new Payment(paymentId, orderId, totalAmount, PaymentMethod.UPI);
        payment.markCompleted("TXN-" + UUID.randomUUID().toString().substring(0, 8));
        order.setPayment(payment);

        // 5. Create DeliveryTask
        String taskId = "TSK-" + UUID.randomUUID().toString().substring(0, 8);
        DeliveryTask task = new DeliveryTask(taskId, orderId, store.getLocation(), deliveryLocation);
        order.attachDeliveryTask(task);

        // 6. Try immediate dispatch
        DeliveryPartner partner = dispatchService.dispatchTask(task);
        if (partner != null) {
            order.setStatus(OrderStatus.ASSIGNED);
        }

        orders.put(orderId, order);

        // 7. Publish Event via IOrderEventPublisher
        eventPublisher.publish(new OrderPlacedEvent(order));
        return order;
    }

    @Override
    public void pickUpOrder(String orderId) {
        Order order = getOrder(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }

        // Commit inventory stock upon pickup
        transactionManager.commitStock(order.getStore().getInventory(), order.getItems());

        if (order.getDeliveryTask() != null) {
            order.getDeliveryTask().markPickedUp();
        }
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        eventPublisher.publish(new OrderDispatchedEvent(order));
    }

    @Override
    public void deliverOrder(String orderId) {
        Order order = getOrder(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }

        if (order.getDeliveryTask() != null) {
            order.getDeliveryTask().markDelivered();
            DeliveryPartner partner = order.getDeliveryTask().getAssignedPartner();
            if (partner != null) {
                partner.completeTask(order.getDeliveryTask().getTaskId(), 5.0);
            }
        }
        order.setStatus(OrderStatus.DELIVERED);
        order.setActualDeliveryTime(Instant.now());
        eventPublisher.publish(new OrderDeliveredEvent(order));
    }

    @Override
    public void cancelOrder(String orderId) {
        Order order = getOrder(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }

        // Release inventory reservation
        transactionManager.releaseStock(order.getStore().getInventory(), order.getItems());

        if (order.getPayment() != null) {
            order.getPayment().initiateRefund();
        }
        order.setStatus(OrderStatus.CANCELLED);
    }

    @Override
    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }

    public Map<String, Order> getAllOrders() {
        return orders;
    }
}
