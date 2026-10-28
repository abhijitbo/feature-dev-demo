package com.demo.orders.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * An order placed by a customer.
 *
 * @param id                 unique order identifier
 * @param customerId         identifier of the customer who placed the order
 * @param items              order lines; defensively copied and immutable
 * @param status             lifecycle status of the order
 * @param cancellationReason why the order was cancelled; set only when {@code status} is {@link OrderStatus#CANCELLED}
 * @param cancelledAt        when the order was cancelled; set only when {@code status} is {@link OrderStatus#CANCELLED}
 */
public record Order(String id, String customerId, List<OrderItem> items,
                    OrderStatus status, String cancellationReason, Instant cancelledAt) {

    public Order {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        items = items == null ? List.of() : List.copyOf(items);
        // DEMO-96: cancellation details exist exactly when the order is cancelled, so the record can't be half-cancelled.
        boolean cancelled = status == OrderStatus.CANCELLED;
        if (cancelled != (cancellationReason != null) || cancelled != (cancelledAt != null)) {
            throw new IllegalArgumentException(
                    "cancellationReason and cancelledAt must be set if and only if status is CANCELLED");
        }
    }

    /**
     * Creates a new order with status {@link OrderStatus#PLACED}.
     *
     * @param id         unique order identifier
     * @param customerId identifier of the customer who placed the order
     * @param items      order lines
     */
    public Order(String id, String customerId, List<OrderItem> items) {
        this(id, customerId, items, OrderStatus.PLACED, null, null);
    }

    /**
     * Returns a copy of this order marked as cancelled.
     *
     * @param reason why the order is cancelled
     * @param at     when the order is cancelled
     * @return the cancelled order
     * @throws IllegalArgumentException if {@code reason} or {@code at} is null
     * @since DEMO-96
     */
    public Order cancel(String reason, Instant at) {
        return new Order(id, customerId, items, OrderStatus.CANCELLED, reason, at);
    }
}
