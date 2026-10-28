package com.demo.orders.repository;

import com.demo.orders.model.Order;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory {@link OrderRepository}, used for local runs and demos.
 */
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        orders.put(order.id(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }
}
