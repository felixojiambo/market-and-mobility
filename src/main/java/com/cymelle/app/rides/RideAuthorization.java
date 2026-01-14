package com.cymelle.app.rides;

import com.cymelle.app.common.exception.NotFoundException;
import com.cymelle.app.users.AppUser;

public final class RideAuthorization {

    private RideAuthorization() {}

    public static void requireOwnerOrAdmin(Ride ride, AppUser actor) {
        if (actor.getRole().name().equals("ADMIN")) return;
        if (!ride.getUser().getId().equals(actor.getId())) {
            throw new NotFoundException("Ride not found");
        }
    }
}
