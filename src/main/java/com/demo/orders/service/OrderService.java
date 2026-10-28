package com.demo.orders.service;

import com.demo.orders.discount.DiscountPolicy;
import com.demo.orders.discount.TieredDiscountPolicy;
import com.demo.orders.exception.OrderNotFoundException;
import com.demo.orders.model.CheckoutSummary;
import com.demo.orders.model.Customer;
import com.demo.orders.model.Order;
import com.demo.orders.model.OrderItem;
import com.demo.orders.repository.OrderRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Application service for placing and pricing orders.
 */
public class OrderService {

    private static final Logger LOG = Logger.getLogger(OrderService.class.getName());

    /** Scale used for all monetary values. */
    public static final int MONEY_SCALE = 2;
    /** Rounding mode used for all monetary values. */
    public static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final OrderRepository orderRepository;
    private final DiscountPolicy discountPolicy;

    /**
     * Creates the service with the default {@link TieredDiscountPolicy}.
     *
     * @param orderRepository repository used to load and store orders
     * @throws NullPointerException if {@code orderRepository} is null
     */
    public OrderService(OrderRepository orderRepository) {
        this(orderRepository, new TieredDiscountPolicy());
    }

    /**
     * Creates the service.
     *
     * @param orderRepository repository used to load and store orders
     * @param discountPolicy  policy deciding the discount applied at checkout
     * @throws NullPointerException if an argument is null
     * @since DEMO-95
     */
    public OrderService(OrderRepository orderRepository, DiscountPolicy discountPolicy) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository must not be null");
        this.discountPolicy = Objects.requireNonNull(discountPolicy, "discountPolicy must not be null");
    }

    /**
     * Validates and stores a new order.
     *
     * @param order the order to place
     * @return the stored order
     * @throws IllegalArgumentException if the order is null or has no items
     */
    public Order placeOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("order must not be null");
        }
        if (order.items().isEmpty()) {
            throw new IllegalArgumentException("order must contain at least one item");
        }
        LOG.info(() -> "Placing order " + order.id() + " for customer " + order.customerId());
        return orderRepository.save(order);
    }

    /**
     * Loads an order by id.
     *
     * @param orderId the order id
     * @return the order
     * @throws OrderNotFoundException if no order exists with that id
     */
    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /**
     * Calculates the order subtotal: the sum of all line totals, before any discount.
     *
     * @param order the order to price
     * @return the subtotal at {@link #MONEY_SCALE} decimal places
     * @throws IllegalArgumentException if the order is null
     */
    public BigDecimal calculateSubtotal(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("order must not be null");
        }
        return order.items().stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    /**
     * Prices an order at checkout, applying the discount decided by the {@link DiscountPolicy}.
     *
     * <p>All values in the result are scaled to {@link #MONEY_SCALE} decimal places
     * using {@link #MONEY_ROUNDING}.</p>
     *
     * @param orderId  id of the order to check out
     * @param customer the customer checking out
     * @return subtotal, discount percentage, discount amount and total payable
     * @throws IllegalArgumentException if {@code orderId} or {@code customer} is null, or the order has no items
     * @throws OrderNotFoundException   if no order exists with that id
     * @since DEMO-95
     */
    public CheckoutSummary checkout(String orderId, Customer customer) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null");
        }
        if (customer == null) {
            throw new IllegalArgumentException("customer must not be null");
        }
        Order order = getOrder(orderId);
        if (order.items().isEmpty()) {
            throw new IllegalArgumentException("order " + orderId + " must contain at least one item");
        }

        BigDecimal subtotal = calculateSubtotal(order);
        BigDecimal discountPercent = discountPolicy.discountPercent(subtotal, customer)
                .setScale(MONEY_SCALE, MONEY_ROUNDING);
        // DEMO-95: percent → fraction via movePointLeft avoids a non-terminating division; round once at the end (BR-4).
        BigDecimal discountAmount = subtotal.multiply(discountPercent.movePointLeft(2))
                .setScale(MONEY_SCALE, MONEY_ROUNDING);
        BigDecimal total = subtotal.subtract(discountAmount);

        LOG.info(() -> "Checkout of order " + orderId + " for customer " + customer.id()
                + ": " + discountPercent + "% discount");
        return new CheckoutSummary(subtotal, discountPercent, discountAmount, total);
    }
}
