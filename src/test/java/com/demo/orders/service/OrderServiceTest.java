package com.demo.orders.service;

import com.demo.orders.exception.OrderNotFoundException;
import com.demo.orders.model.CheckoutSummary;
import com.demo.orders.model.Customer;
import com.demo.orders.model.CustomerTier;
import com.demo.orders.model.Order;
import com.demo.orders.model.OrderItem;
import com.demo.orders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OrderService}.
 *
 * <p>Checkout tests cover Jira story DEMO-95: Apply tiered discount to order total at checkout.</p>
 *
 * Acceptance criteria tested:
 * - AC-01: below 100.00 and STANDARD → no discount, total equals subtotal
 * - AC-02: volume discount 5% for 100.00–499.99, 10% for ≥ 500.00
 * - AC-03: loyalty percentage added to volume percentage, capped at 15%
 * - AC-04: summary values are BigDecimal at scale 2, HALF_UP
 * - AC-05: null order id, null customer or empty order → IllegalArgumentException
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository);
    }

    private static Order orderWith(OrderItem... items) {
        return new Order("ORD-1", "CUST-1", List.of(items));
    }

    @Test
    void placeOrder_whenOrderHasItems_savesOrder() {
        Order order = orderWith(new OrderItem("P-1", new BigDecimal("10.00"), 1));
        when(orderRepository.save(order)).thenReturn(order);

        Order saved = orderService.placeOrder(order);

        assertSame(order, saved);
        verify(orderRepository).save(order);
    }

    @Test
    void placeOrder_whenOrderHasNoItems_throwsIllegalArgumentException() {
        Order empty = orderWith();

        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(empty));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_whenOrderIsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(null));
    }

    @Test
    void getOrder_whenOrderExists_returnsOrder() {
        Order order = orderWith(new OrderItem("P-1", BigDecimal.ONE, 1));
        when(orderRepository.findById("ORD-1")).thenReturn(Optional.of(order));

        assertSame(order, orderService.getOrder("ORD-1"));
    }

    @Test
    void getOrder_whenOrderMissing_throwsOrderNotFoundException() {
        when(orderRepository.findById("ORD-404")).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrder("ORD-404"));
    }

    @Test
    void calculateSubtotal_whenMultipleItems_returnsSumOfLineTotals() {
        Order order = orderWith(
                new OrderItem("P-1", new BigDecimal("19.99"), 2),
                new OrderItem("P-2", new BigDecimal("5.005"), 1));

        assertEquals(new BigDecimal("44.99"), orderService.calculateSubtotal(order));
    }

    @Test
    void calculateSubtotal_whenNoItems_returnsZero() {
        assertEquals(new BigDecimal("0.00"), orderService.calculateSubtotal(orderWith()));
    }

    private static Customer customer(CustomerTier tier) {
        return new Customer("CUST-1", tier);
    }

    private CheckoutSummary checkoutSingleItem(String price, CustomerTier tier) {
        Order order = orderWith(new OrderItem("P-1", new BigDecimal(price), 1));
        when(orderRepository.findById("ORD-1")).thenReturn(Optional.of(order));
        return orderService.checkout("ORD-1", customer(tier));
    }

    private static void assertSummary(CheckoutSummary summary, String subtotal, String percent,
                                      String discountAmount, String total) {
        assertEquals(new BigDecimal(subtotal), summary.subtotal());
        assertEquals(new BigDecimal(percent), summary.discountPercent());
        assertEquals(new BigDecimal(discountAmount), summary.discountAmount());
        assertEquals(new BigDecimal(total), summary.total());
    }

    // AC-01

    @Test
    void checkout_whenBelowThresholdAndStandard_appliesNoDiscount() {
        assertSummary(checkoutSingleItem("99.99", CustomerTier.STANDARD), "99.99", "0.00", "0.00", "99.99");
    }

    @Test
    void checkout_whenSubtotalIsZero_returnsZeroTotal() {
        assertSummary(checkoutSingleItem("0.00", CustomerTier.GOLD), "0.00", "5.00", "0.00", "0.00");
    }

    // AC-02

    @Test
    void checkout_whenSubtotalExactly100_appliesFivePercent() {
        assertSummary(checkoutSingleItem("100.00", CustomerTier.STANDARD), "100.00", "5.00", "5.00", "95.00");
    }

    @Test
    void checkout_whenSubtotalJustBelow500_appliesFivePercentRoundedHalfUp() {
        // 499.99 × 5% = 24.9995 → 25.00
        assertSummary(checkoutSingleItem("499.99", CustomerTier.STANDARD), "499.99", "5.00", "25.00", "474.99");
    }

    @Test
    void checkout_whenSubtotalExactly500_appliesTenPercent() {
        assertSummary(checkoutSingleItem("500.00", CustomerTier.STANDARD), "500.00", "10.00", "50.00", "450.00");
    }

    // AC-03

    @Test
    void checkout_whenSilverCustomer_addsTwoPercentToVolume() {
        assertSummary(checkoutSingleItem("100.00", CustomerTier.SILVER), "100.00", "7.00", "7.00", "93.00");
    }

    @Test
    void checkout_whenGoldCustomerBelowVolumeThreshold_appliesLoyaltyOnly() {
        // 99.99 × 5% = 4.9995 → 5.00
        assertSummary(checkoutSingleItem("99.99", CustomerTier.GOLD), "99.99", "5.00", "5.00", "94.99");
    }

    @Test
    void checkout_whenGoldCustomerAndLargeOrder_capsAtFifteenPercent() {
        assertSummary(checkoutSingleItem("1000.00", CustomerTier.GOLD), "1000.00", "15.00", "150.00", "850.00");
    }

    @Test
    void checkout_whenCustomPolicy_appliesPolicyPercent() {
        OrderService service = new OrderService(orderRepository, (subtotal, customer) -> new BigDecimal("15"));
        when(orderRepository.findById("ORD-1"))
                .thenReturn(Optional.of(orderWith(new OrderItem("P-1", new BigDecimal("200.00"), 1))));

        assertSummary(service.checkout("ORD-1", customer(CustomerTier.STANDARD)), "200.00", "15.00", "30.00", "170.00");
    }

    // AC-04

    @Test
    void checkout_whenFractionalPrices_returnsAllValuesAtScaleTwo() {
        // 33.335 × 3 = 100.005 → subtotal 100.01; 5% = 5.0005 → 5.00
        Order order = orderWith(new OrderItem("P-1", new BigDecimal("33.335"), 3));
        when(orderRepository.findById("ORD-1")).thenReturn(Optional.of(order));

        CheckoutSummary summary = orderService.checkout("ORD-1", customer(CustomerTier.STANDARD));

        assertSummary(summary, "100.01", "5.00", "5.00", "95.01");
        assertEquals(2, summary.subtotal().scale());
        assertEquals(2, summary.discountPercent().scale());
        assertEquals(2, summary.discountAmount().scale());
        assertEquals(2, summary.total().scale());
    }

    // AC-05

    @Test
    void checkout_whenOrderIdIsNull_throwsIllegalArgumentException() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> orderService.checkout(null, customer(CustomerTier.GOLD)));
        assertTrue(e.getMessage().contains("orderId"));
        verify(orderRepository, never()).findById(any());
    }

    @Test
    void checkout_whenCustomerIsNull_throwsIllegalArgumentException() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> orderService.checkout("ORD-1", null));
        assertTrue(e.getMessage().contains("customer"));
    }

    @Test
    void checkout_whenOrderHasNoItems_throwsIllegalArgumentException() {
        when(orderRepository.findById("ORD-1")).thenReturn(Optional.of(orderWith()));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> orderService.checkout("ORD-1", customer(CustomerTier.GOLD)));
        assertTrue(e.getMessage().contains("at least one item"));
    }

    @Test
    void checkout_whenOrderMissing_throwsOrderNotFoundException() {
        when(orderRepository.findById("ORD-404")).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.checkout("ORD-404", customer(CustomerTier.GOLD)));
    }
}
