package com.quickcommerce;

import com.quickcommerce.enums.ItemCategory;
import com.quickcommerce.enums.PaymentMethod;
import com.quickcommerce.enums.PaymentStatus;
import com.quickcommerce.model.*;
import com.quickcommerce.service.OrderPricingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class PaymentAndPricingServiceTest {

    @Test
    @DisplayName("Should accurately calculate items subtotal, distance delivery fee, and 5% GST")
    void shouldCalculatePricingAccurately() {
        OrderPricingService pricingService = new OrderPricingService();
        Store store = new Store("STR-1", "Indiranagar Dark Store", new Location(12.9716, 77.5946), new Address("Street 1", "Indiranagar", "Bengaluru", "560038", new Location(12.9716, 77.5946)));
        Location nearLocation = new Location(12.9816, 77.6046); // ~1.5 km

        Item bread = new Item("I-1", "Artisan Bread", "Fresh Sourdough", 50.0, ItemCategory.DAIRY_AND_BREAD, 0.4);
        List<OrderItem> items = List.of(new OrderItem("OI-1", bread, 2)); // 100.0

        double total = pricingService.calculateTotal(items, store, nearLocation);
        // 100 + 20 delivery + 5 tax = 125.0
        assertEquals(125.0, total);
    }

    @Test
    @DisplayName("Should track payment lifecycle: PENDING -> COMPLETED -> REFUNDED")
    void shouldTrackPaymentLifecycle() {
        Payment payment = new Payment("PAY-1", "ORD-1", 125.0, PaymentMethod.UPI);
        assertEquals(PaymentStatus.PENDING, payment.getStatus());

        payment.markCompleted("TXN-12345");
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals("TXN-12345", payment.getTransactionId());

        payment.initiateRefund();
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
    }
}
