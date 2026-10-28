package com.demo.orders.discount;

import com.demo.orders.model.Customer;

import java.math.BigDecimal;

/**
 * Decides which discount percentage applies to an order at checkout.
 *
 * @since DEMO-95
 */
public interface DiscountPolicy {

    /**
     * Returns the discount percentage for an order.
     *
     * @param subtotal order subtotal before discount; must not be null or negative
     * @param customer the customer checking out; must not be null
     * @return the discount percentage, e.g. {@code 7} for 7%
     * @throws IllegalArgumentException if an argument is null or the subtotal is negative
     */
    BigDecimal discountPercent(BigDecimal subtotal, Customer customer);
}
