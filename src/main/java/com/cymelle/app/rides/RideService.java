package com.cymelle.app.rides;

import com.cymelle.app.common.exception.NotFoundException;
import com.cymelle.app.rides.dto.RideResponse;
import com.cymelle.app.rides.dto.UpdateRideStatusRequest;
import com.cymelle.app.security.CurrentUser;
import com.cymelle.app.users.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;

    public RideResponse get(Long id) {
        AppUser actor = CurrentUser.require();
        Ride ride = rideRepository.findById(id).orElseThrow(() -> new NotFoundException("Ride not found"));
        RideAuthorization.requireOwnerOrAdmin(ride, actor);
        return RideResponse.from(ride);
    }

    public Page<RideResponse> search(Long userId, RideStatus status, Pageable pageable) {
        AppUser actor = CurrentUser.require();
        Long effectiveUserId = actor.getRole().name().equals("ADMIN") ? userId : actor.getId();

        if (effectiveUserId != null && status != null) {
            return rideRepository.findByUserIdAndStatus(effectiveUserId, status, pageable).map(RideResponse::from);
        }
        if (effectiveUserId != null) {
            return rideRepository.findByUserId(effectiveUserId, pageable).map(RideResponse::from);
        }
        if (status != null) {
            // only ADMIN should reach here
            return rideRepository.findByStatus(status, pageable).map(RideResponse::from);
        }
        if (actor.getRole().name().equals("ADMIN")) {
            return rideRepository.findAll(pageable).map(RideResponse::from);
        }
        return rideRepository.findByUserId(actor.getId(), pageable).map(RideResponse::from);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public RideResponse updateStatus(Long id, UpdateRideStatusRequest req) {
        Ride ride = rideRepository.findById(id).orElseThrow(() -> new NotFoundException("Ride not found"));
        RideStatusTransitions.validate(ride.getStatus(), req.getStatus());
        ride.setStatus(req.getStatus());
        rideRepository.save(ride);
        return RideResponse.from(ride);
    }
}
