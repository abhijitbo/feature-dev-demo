package com.demo.orders.service;

import com.demo.orders.exception.OrderNotFoundException;
import com.demo.orders.model.Order;
import com.demo.orders.model.OrderItem;
import com.demo.orders.repository.OrderRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Application service for placing and pricing orders.
 */
public class OrderService {

    private static final Logger LOG = Logger.getLogger(OrderService.class.getName());

    /** Scale used for all monetary values. */
    public static final int MONEY_SCALE = 2;
    /** Rounding mode used for all monetary values. */
    public static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final OrderRepository orderRepository;

    /**
     * Creates the service.
     *
     * @param orderRepository repository used to load and store orders
     * @throws NullPointerException if {@code orderRepository} is null
     */
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository must not be null");
    }

    /**
     * Validates and stores a new order.
     *
     * @param order the order to place
     * @return the stored order
     * @throws IllegalArgumentException if the order is null or has no items
     */
    public Order placeOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("order must not be null");
        }
        if (order.items().isEmpty()) {
            throw new IllegalArgumentException("order must contain at least one item");
        }
        LOG.info(() -> "Placing order " + order.id() + " for customer " + order.customerId());
        return orderRepository.save(order);
    }

    /**
     * Loads an order by id.
     *
     * @param orderId the order id
     * @return the order
     * @throws OrderNotFoundException if no order exists with that id
     */
    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /**
     * Calculates the order subtotal: the sum of all line totals, before any discount.
     *
     * @param order the order to price
     * @return the subtotal at {@link #MONEY_SCALE} decimal places
     * @throws IllegalArgumentException if the order is null
     */
    public BigDecimal calculateSubtotal(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("order must not be null");
        }
        return order.items().stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, MONEY_ROUNDING);
    }
}
