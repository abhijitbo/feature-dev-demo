package com.demo.orders.exception;

/**
 * Thrown when an order id does not match any stored order.
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String orderId) {
        super("Order not found: " + orderId);
    }
}
