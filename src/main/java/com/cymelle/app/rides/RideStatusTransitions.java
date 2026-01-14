package com.cymelle.app.rides;

import com.cymelle.app.common.exception.ConflictException;

public final class RideStatusTransitions {

    private RideStatusTransitions() {}

    public static void validate(RideStatus current, RideStatus next) {
        if (current == next) return;

        boolean ok =
                (current == RideStatus.REQUESTED && next == RideStatus.ACCEPTED) ||
                        (current == RideStatus.ACCEPTED && next == RideStatus.COMPLETED);

        if (!ok) {
            throw new ConflictException("Invalid ride status transition: " + current + " -> " + next);
        }
    }
}
