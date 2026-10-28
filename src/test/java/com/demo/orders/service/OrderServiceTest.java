package com.demo.orders.service;

import com.demo.orders.exception.OrderNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OrderService}.
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
}
