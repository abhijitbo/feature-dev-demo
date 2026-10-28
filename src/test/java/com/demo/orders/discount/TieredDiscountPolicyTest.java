package com.demo.orders.discount;

import com.demo.orders.model.Customer;
import com.demo.orders.model.CustomerTier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link TieredDiscountPolicy}.
 *
 * <p>Covers Jira story DEMO-95: Apply tiered discount to order total at checkout.</p>
 *
 * Acceptance criteria tested:
 * - AC-01: below 100.00 and STANDARD → no discount
 * - AC-02: volume discount 5% for 100.00–499.99, 10% for ≥ 500.00
 * - AC-03: loyalty percentage added to volume percentage, combined never above 15%
 */
class TieredDiscountPolicyTest {

    private final TieredDiscountPolicy policy = new TieredDiscountPolicy();

    private static Customer customer(CustomerTier tier) {
        return new Customer("CUST-1", tier);
    }

    @Test
    void discountPercent_whenBelowThresholdAndStandard_returnsZero() {
        assertEquals(0, BigDecimal.ZERO.compareTo(
                policy.discountPercent(new BigDecimal("99.99"), customer(CustomerTier.STANDARD))));
    }

    @Test
    void discountPercent_whenSubtotalIsZero_returnsZero() {
        assertEquals(0, BigDecimal.ZERO.compareTo(
                policy.discountPercent(BigDecimal.ZERO, customer(CustomerTier.STANDARD))));
    }

    @ParameterizedTest(name = "subtotal {0} → {1}%")
    @CsvSource({
            "99.99, 0",
            "100.00, 5",
            "499.99, 5",
            "500.00, 10",
            "10000.00, 10"
    })
    void discountPercent_whenStandardCustomer_returnsVolumePercent(String subtotal, String expected) {
        assertEquals(0, new BigDecimal(expected).compareTo(
                policy.discountPercent(new BigDecimal(subtotal), customer(CustomerTier.STANDARD))));
    }

    @ParameterizedTest(name = "{1} at {0} → {2}%")
    @CsvSource({
            "99.99, SILVER, 2",
            "99.99, GOLD, 5",
            "100.00, SILVER, 7",
            "100.00, GOLD, 10",
            "500.00, SILVER, 12",
            "500.00, GOLD, 15"
    })
    void discountPercent_whenLoyalCustomer_addsLoyaltyToVolumePercent(String subtotal, CustomerTier tier,
                                                                     String expected) {
        assertEquals(0, new BigDecimal(expected).compareTo(
                policy.discountPercent(new BigDecimal(subtotal), customer(tier))));
    }

    @Test
    void discountPercent_whenGoldAndVeryLargeOrder_neverExceedsCap() {
        BigDecimal percent = policy.discountPercent(new BigDecimal("1000000.00"), customer(CustomerTier.GOLD));

        assertEquals(0, TieredDiscountPolicy.MAX_DISCOUNT_PERCENT.compareTo(percent));
    }

    @Test
    void discountPercent_whenSubtotalIsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> policy.discountPercent(null, customer(CustomerTier.GOLD)));
    }

    @Test
    void discountPercent_whenSubtotalIsNegative_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> policy.discountPercent(new BigDecimal("-0.01"), customer(CustomerTier.GOLD)));
    }

    @Test
    void discountPercent_whenCustomerIsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> policy.discountPercent(new BigDecimal("100.00"), null));
    }
}
