package com.demo.orders.model;

import java.util.List;
import java.util.Objects;

/**
 * An order placed by a customer.
 *
 * @param id         unique order identifier
 * @param customerId identifier of the customer who placed the order
 * @param items      order lines; defensively copied and immutable
 */
public record Order(String id, String customerId, List<OrderItem> items) {

    public Order {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        items = items == null ? List.of() : List.copyOf(items);
    }
}
