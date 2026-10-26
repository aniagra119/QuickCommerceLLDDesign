package com.quickcommerce.service;

import com.quickcommerce.model.Location;
import com.quickcommerce.model.Order;
import com.quickcommerce.model.OrderItem;
import com.quickcommerce.model.Store;
import com.quickcommerce.model.User;
import java.util.List;

public interface IOrderService {
    Order placeOrder(User customer, Store store, List<OrderItem> items, Location deliveryLocation);
    void pickUpOrder(String orderId);
    void deliverOrder(String orderId);
    void cancelOrder(String orderId);
    Order getOrder(String orderId);
}
