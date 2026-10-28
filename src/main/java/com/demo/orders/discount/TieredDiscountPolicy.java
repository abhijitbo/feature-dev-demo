package com.demo.orders.discount;

import com.demo.orders.model.Customer;
import com.demo.orders.model.CustomerTier;

import java.math.BigDecimal;

/**
 * Discount policy combining a volume discount on the subtotal with a loyalty discount on the customer tier.
 *
 * <p>Volume (BR-1): below 100.00 → 0%, 100.00–499.99 → 5%, 500.00 and above → 10%.
 * Loyalty (BR-2): STANDARD → 0%, SILVER → 2%, GOLD → 5%.
 * The two are added and capped at 15% (BR-3).</p>
 *
 * @since DEMO-95
 */
public class TieredDiscountPolicy implements DiscountPolicy {

    static final BigDecimal MID_VOLUME_THRESHOLD = new BigDecimal("100.00");
    static final BigDecimal HIGH_VOLUME_THRESHOLD = new BigDecimal("500.00");

    static final BigDecimal NO_DISCOUNT = BigDecimal.ZERO;
    static final BigDecimal MID_VOLUME_PERCENT = new BigDecimal("5");
    static final BigDecimal HIGH_VOLUME_PERCENT = new BigDecimal("10");

    static final BigDecimal SILVER_PERCENT = new BigDecimal("2");
    static final BigDecimal GOLD_PERCENT = new BigDecimal("5");

    static final BigDecimal MAX_DISCOUNT_PERCENT = new BigDecimal("15");

    /**
     * Returns the volume percentage plus the loyalty percentage, capped at {@link #MAX_DISCOUNT_PERCENT}.
     *
     * @param subtotal order subtotal before discount; must not be null or negative
     * @param customer the customer checking out; must not be null
     * @return the combined discount percentage
     * @throws IllegalArgumentException if an argument is null or the subtotal is negative
     */
    @Override
    public BigDecimal discountPercent(BigDecimal subtotal, Customer customer) {
        if (subtotal == null) {
            throw new IllegalArgumentException("subtotal must not be null");
        }
        if (subtotal.signum() < 0) {
            throw new IllegalArgumentException("subtotal must not be negative");
        }
        if (customer == null) {
            throw new IllegalArgumentException("customer must not be null");
        }
        BigDecimal combined = volumePercent(subtotal).add(loyaltyPercent(customer.tier()));
        return combined.min(MAX_DISCOUNT_PERCENT);
    }

    private static BigDecimal volumePercent(BigDecimal subtotal) {
        if (subtotal.compareTo(HIGH_VOLUME_THRESHOLD) >= 0) {
            return HIGH_VOLUME_PERCENT;
        }
        if (subtotal.compareTo(MID_VOLUME_THRESHOLD) >= 0) {
            return MID_VOLUME_PERCENT;
        }
        return NO_DISCOUNT;
    }

    private static BigDecimal loyaltyPercent(CustomerTier tier) {
        return switch (tier) {
            case STANDARD -> NO_DISCOUNT;
            case SILVER -> SILVER_PERCENT;
            case GOLD -> GOLD_PERCENT;
        };
    }
}
