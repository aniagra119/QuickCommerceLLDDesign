package com.quickcommerce;

import com.quickcommerce.enums.ItemCategory;
import com.quickcommerce.enums.OrderStatus;
import com.quickcommerce.enums.TaskStatus;
import com.quickcommerce.enums.VehicleType;
import com.quickcommerce.event.OrderEventPublisher;
import com.quickcommerce.event.listener.CustomerSmsNotificationListener;
import com.quickcommerce.event.listener.StorePackingKdsListener;
import com.quickcommerce.matching.ProximityToStoreMatchingStrategy;
import com.quickcommerce.model.Address;
import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.Item;
import com.quickcommerce.model.Location;
import com.quickcommerce.model.Order;
import com.quickcommerce.model.OrderItem;
import com.quickcommerce.model.Store;
import com.quickcommerce.model.User;
import com.quickcommerce.service.DispatchService;
import com.quickcommerce.service.OrderService;
import com.quickcommerce.service.QuickCommercePlatform;
import com.quickcommerce.tracking.PartnerLocationTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Quick Commerce Platform End-to-End Integration Tests")
class QuickCommerceEndToEndIntegrationTest {

    private QuickCommercePlatform platform;
    private CustomerSmsNotificationListener smsListener;
    private StorePackingKdsListener kdsListener;
    private Store darkStore;
    private User customer;
    private DeliveryPartner partnerNear;
    private DeliveryPartner partnerFar;
    private Item milk;
    private Item eggs;

    @BeforeEach
    void setUp() {
        ProximityToStoreMatchingStrategy matchingStrategy = new ProximityToStoreMatchingStrategy();
        DispatchService dispatchService = new DispatchService(matchingStrategy);
        OrderEventPublisher eventPublisher = new OrderEventPublisher();
        PartnerLocationTracker locationTracker = new PartnerLocationTracker();

        smsListener = new CustomerSmsNotificationListener();
        kdsListener = new StorePackingKdsListener();
        eventPublisher.registerListener(smsListener);
        eventPublisher.registerListener(kdsListener);

        OrderService orderService = new OrderService(dispatchService, eventPublisher);
        platform = new QuickCommercePlatform(dispatchService, orderService, locationTracker);

        // Dark Store
        Location storeLoc = new Location(12.9350, 77.6240);
        Address storeAddr = new Address("100 Feet Rd", "Koramangala 4th Block", "Bangalore", "560034", storeLoc);
        darkStore = new Store("STR-1", "Zepto Koramangala Hub", storeLoc, storeAddr);

        milk = new Item("I-MILK", "Country Delight Cow Milk 1L", "Pure unpasteurized milk", 85.0, ItemCategory.DAIRY_AND_BREAD, 1.0);
        eggs = new Item("I-EGGS", "Farm Fresh Eggs 6pk", "White organic eggs", 60.0, ItemCategory.DAIRY_AND_BREAD, 0.35);

        darkStore.getInventory().addItem(milk, 20);
        darkStore.getInventory().addItem(eggs, 15);
        platform.registerStore(darkStore);

        // Customer
        Location custLoc = new Location(12.9420, 77.6310);
        Address custAddr = new Address("12th Main", "Indiranagar", "Bangalore", "560038", custLoc);
        customer = new User("USR-1", "Anirudh Agrawal", "+919876543210", "anirudh@example.com", custAddr);
        platform.registerUser(customer);

        // Partners
        partnerNear = new DeliveryPartner("DP-NEAR", "Suraj", "+919001", VehicleType.ELECTRIC_VEHICLE, new Location(12.9360, 77.6250));
        partnerNear.goOnline();

        partnerFar = new DeliveryPartner("DP-FAR", "Vikram", "+919002", VehicleType.MOTORCYCLE, new Location(12.9600, 77.6500));
        partnerFar.goOnline();

        platform.registerPartner(partnerNear);
        platform.registerPartner(partnerFar);
    }

    @Test
    @DisplayName("Should successfully place order, auto-dispatch to closest online partner, pick up and deliver")
    void shouldExecuteCompleteDeliveryLifecycle() {
        List<OrderItem> items = List.of(
                new OrderItem("OI-1", milk, 2),
                new OrderItem("OI-2", eggs, 1)
        );

        // 1. Place Order
        Order order = platform.placeOrder("USR-1", "STR-1", items, customer.getAddress().getLocation());
        assertNotNull(order);
        assertEquals(OrderStatus.ASSIGNED, order.getStatus());
        assertEquals(261.5, order.getTotalAmount()); // 2*85 + 60 = 230 items + 20 delivery + 11.5 GST
        assertNotNull(order.getPayment());
        assertEquals(com.quickcommerce.enums.PaymentStatus.COMPLETED, order.getPayment().getStatus());
        assertFalse(order.isSlaBreached());

        // Auto-assigned to closest partner (partnerNear)
        assertEquals("DP-NEAR", order.getDeliveryTask().getAssignedPartner().getPartnerId());
        assertEquals(TaskStatus.ASSIGNED, order.getDeliveryTask().getStatus());

        // Check Inventory Reserved
        assertEquals(18, darkStore.getInventory().getInventoryItem("I-MILK").getAvailableQuantity());
        assertEquals(2, darkStore.getInventory().getInventoryItem("I-MILK").getReservedQuantity());

        // 2. Partner moves towards store (Location Pings)
        platform.updatePartnerLocation("DP-NEAR", new Location(12.9351, 77.6241), Instant.now());
        assertEquals(new Location(12.9351, 77.6241), platform.getPartnerLocation("DP-NEAR"));

        // 3. Order Packed & Picked Up by partner
        platform.pickUpOrder(order.getOrderId());
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, order.getStatus());
        assertEquals(TaskStatus.PICKED_UP, order.getDeliveryTask().getStatus());
        // Reserved inventory committed
        assertEquals(0, darkStore.getInventory().getInventoryItem("I-MILK").getReservedQuantity());

        // 4. Order Delivered
        platform.deliverOrder(order.getOrderId());
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(TaskStatus.DELIVERED, order.getDeliveryTask().getStatus());

        // 5. Verify Notifications
        assertEquals(3, smsListener.getSentAlerts().size());
        assertTrue(smsListener.getSentAlerts().get(0).contains("placed"));
        assertTrue(smsListener.getSentAlerts().get(1).contains("out for delivery"));
        assertTrue(smsListener.getSentAlerts().get(2).contains("delivered safely"));
    }

    @Test
    @DisplayName("Should reject order if store inventory is insufficient")
    void shouldRejectOrderWhenStockInsufficient() {
        List<OrderItem> excessiveItems = List.of(
                new OrderItem("OI-MAX", milk, 50) // Store only has 20
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                platform.placeOrder("USR-1", "STR-1", excessiveItems, customer.getAddress().getLocation())
        );
        assertTrue(ex.getMessage().contains("does not have sufficient inventory"));
    }
}

