package com.cymelle.app.orders;

import com.cymelle.app.common.exception.ForbiddenException;
import com.cymelle.app.users.AppUser;
import com.cymelle.app.users.Role;

public final class OrderAuthorization {

    private OrderAuthorization() {}

    public static void requireOwnerOrAdmin(Order order, AppUser actor) {
        if (actor.getRole() == Role.ADMIN) return;

        if (!order.getCustomer().getId().equals(actor.getId())) {
            throw new ForbiddenException("You are not allowed to access this order");
        }
    }
}
