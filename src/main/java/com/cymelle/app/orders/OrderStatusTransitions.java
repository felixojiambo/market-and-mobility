package com.cymelle.app.orders;

import com.cymelle.app.common.exception.ConflictException;

public final class OrderStatusTransitions {

    private OrderStatusTransitions() {}

    public static void validate(OrderStatus current, OrderStatus next) {
        if (current == next) return;

        boolean ok =
                (current == OrderStatus.PENDING && next == OrderStatus.SHIPPED) ||
                        (current == OrderStatus.SHIPPED && next == OrderStatus.DELIVERED);

        if (!ok) {
            throw new ConflictException("Invalid order status transition: " + current + " -> " + next);
        }
    }
}
