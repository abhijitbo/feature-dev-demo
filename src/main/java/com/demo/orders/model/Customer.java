package com.demo.orders.model;

import java.util.Objects;

/**
 * A customer checking out an order.
 *
 * @param id   unique customer identifier
 * @param tier loyalty tier of the customer
 * @since DEMO-95
 */
public record Customer(String id, CustomerTier tier) {

    public Customer {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(tier, "tier must not be null");
    }
}
