package com.demo.orders.repository;

import com.demo.orders.model.Order;

import java.util.Optional;

/**
 * Persistence port for {@link Order} aggregates.
 */
public interface OrderRepository {

    /**
     * Stores the order, replacing any existing order with the same id.
     *
     * @param order the order to save
     * @return the saved order
     */
    Order save(Order order);

    /**
     * Looks up an order by id.
     *
     * @param orderId the order id
     * @return the order, or empty if none exists
     */
    Optional<Order> findById(String orderId);
}
