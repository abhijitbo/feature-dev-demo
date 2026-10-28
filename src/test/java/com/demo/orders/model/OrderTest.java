package com.demo.orders.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link Order}.
 *
 * <p>Covers Jira story DEMO-96: Allow customers to cancel a placed order with a reason.</p>
 *
 * Acceptance criteria tested:
 * - AC-01: a new order has status PLACED
 * - AC-02: a cancelled order carries status CANCELLED, the reason and the cancellation time
 */
class OrderTest {

    private static final Instant NOW = Instant.parse("2026-10-28T10:15:30Z");
    private static final List<OrderItem> ITEMS = List.of(new OrderItem("P-1", BigDecimal.TEN, 1));

    @Test
    void constructor_whenThreeArguments_defaultsToPlacedWithoutCancellationDetails() {
        Order order = new Order("ORD-1", "CUST-1", ITEMS);

        assertEquals(OrderStatus.PLACED, order.status());
        assertNull(order.cancellationReason());
        assertNull(order.cancelledAt());
    }

    @Test
    void cancel_whenPlaced_returnsCancelledCopyAndLeavesOriginalUnchanged() {
        Order placed = new Order("ORD-1", "CUST-1", ITEMS);

        Order cancelled = placed.cancel("Changed my mind", NOW);

        assertEquals(new Order("ORD-1", "CUST-1", ITEMS, OrderStatus.CANCELLED, "Changed my mind", NOW), cancelled);
        assertEquals(OrderStatus.PLACED, placed.status());
    }

    @Test
    void cancel_whenReasonIsNull_throwsIllegalArgumentException() {
        Order placed = new Order("ORD-1", "CUST-1", ITEMS);

        assertThrows(IllegalArgumentException.class, () -> placed.cancel(null, NOW));
    }

    @Test
    void cancel_whenTimeIsNull_throwsIllegalArgumentException() {
        Order placed = new Order("ORD-1", "CUST-1", ITEMS);

        assertThrows(IllegalArgumentException.class, () -> placed.cancel("Changed my mind", null));
    }

    @Test
    void constructor_whenPlacedWithCancellationDetails_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Order("ORD-1", "CUST-1", ITEMS, OrderStatus.PLACED, "reason", NOW));
    }

    @Test
    void constructor_whenStatusIsNull_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new Order("ORD-1", "CUST-1", ITEMS, null, null, null));
    }
}
