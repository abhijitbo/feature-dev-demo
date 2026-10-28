package com.demo.orders.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Result of pricing an order at checkout.
 *
 * <p>All values are scaled to 2 decimal places using {@code HALF_UP} rounding.</p>
 *
 * @param subtotal        sum of all line totals, before discount
 * @param discountPercent combined discount percentage applied, e.g. {@code 7.00} for 7%
 * @param discountAmount  amount deducted from the subtotal
 * @param total           amount payable: subtotal minus discount amount
 * @since DEMO-95
 */
public record CheckoutSummary(BigDecimal subtotal, BigDecimal discountPercent,
                              BigDecimal discountAmount, BigDecimal total) {

    public CheckoutSummary {
        Objects.requireNonNull(subtotal, "subtotal must not be null");
        Objects.requireNonNull(discountPercent, "discountPercent must not be null");
        Objects.requireNonNull(discountAmount, "discountAmount must not be null");
        Objects.requireNonNull(total, "total must not be null");
    }
}
