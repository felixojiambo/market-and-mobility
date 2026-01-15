package com.cymelle.app.rides;

import com.cymelle.app.common.exception.ForbiddenException;
import com.cymelle.app.users.Role;

public final class RideAuthorization {

    private RideAuthorization() {}

    /**
     * Allows access if:
     * - actor is ADMIN
     * - OR actor owns the ride
     */
    public static void requireOwnerOrAdmin(Ride ride, Long actorId, Role actorRole) {
        if (actorRole == Role.ADMIN) {
            return;
        }

        if (!ride.getUser().getId().equals(actorId)) {
            throw new ForbiddenException("Forbidden");
        }
    }
}
