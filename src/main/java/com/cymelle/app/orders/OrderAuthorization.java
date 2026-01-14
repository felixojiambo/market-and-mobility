package com.cymelle.app.orders;

import com.cymelle.app.common.exception.NotFoundException;
import com.cymelle.app.users.AppUser;

public final class OrderAuthorization {

    private OrderAuthorization() {}

    public static void requireOwnerOrAdmin(Order order, AppUser actor) {
        if (actor.getRole().name().equals("ADMIN")) return;
        if (!order.getCustomer().getId().equals(actor.getId())) {
            throw new NotFoundException("Order not found");
        }
    }
}
