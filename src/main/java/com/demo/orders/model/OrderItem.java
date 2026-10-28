package com.demo.orders.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A single line on an order: a product, its unit price and the quantity ordered.
 *
 * @param productId identifier of the product
 * @param unitPrice price of one unit; must be zero or positive
 * @param quantity  number of units; must be at least 1
 */
public record OrderItem(String productId, BigDecimal unitPrice, int quantity) {

    public OrderItem {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("unitPrice must not be negative");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1");
        }
    }

    /**
     * Returns the total price for this line.
     *
     * @return unit price multiplied by quantity
     */
    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
