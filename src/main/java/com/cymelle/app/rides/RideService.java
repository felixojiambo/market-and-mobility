package com.cymelle.app.rides;

import com.cymelle.app.common.exception.NotFoundException;
import com.cymelle.app.rides.dto.RequestRideRequest;
import com.cymelle.app.rides.dto.RideResponse;
import com.cymelle.app.rides.dto.UpdateRideStatusRequest;
import com.cymelle.app.security.CurrentUser;
import com.cymelle.app.users.AppUser;
import com.cymelle.app.users.Role;
import com.cymelle.app.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;

    // 10.1 Request ride
    public RideResponse requestRide(RequestRideRequest request) {
        Long actorId = CurrentUser.id();

        AppUser user = userRepository.findById(actorId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        AppUser driver = null;
        if (request.getDriverId() != null) {
            driver = userRepository.findById(request.getDriverId())
                    .orElseThrow(() -> new NotFoundException("Driver not found"));
        }

        Ride ride = new Ride();
        ride.setUser(user);
        ride.setDriver(driver);
        ride.setPickupLocation(request.getPickupLocation());
        ride.setDropoffLocation(request.getDropoffLocation());
        ride.setFare(request.getFare());
        ride.setStatus(RideStatus.REQUESTED);

        Ride saved = rideRepository.save(ride);
        return RideResponse.from(saved);
    }

    // 10.2 View ride (owner/admin)
    public RideResponse getRide(Long id) {
        Long actorId = CurrentUser.id();
        Role actorRole = Role.valueOf(CurrentUser.require().getRole());

        Ride ride = rideRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ride not found"));

        RideAuthorization.requireOwnerOrAdmin(ride, actorId, actorRole);
        return RideResponse.from(ride);
    }

    /**
     * 10.3 Search rides (paginated)
     *
     * GET /api/v1/rides?status=&userId=&driverId=&page=&size=
     *
     * Rules:
     * - ADMIN can query any: userId, driverId, status (any combination)
     * - CUSTOMER forced to self userId; driverId ignored (optional)
     */
    public Page<RideResponse> searchRides(Long userId, Long driverId, RideStatus status, Pageable pageable) {
        Long actorId = CurrentUser.id();
        Role actorRole = Role.valueOf(CurrentUser.require().getRole());

        if (actorRole != Role.ADMIN) {
            // customer forced to self; ignore driverId
            userId = actorId;
            driverId = null;
        }

        // ADMIN: allow all combinations
        if (userId != null && status != null) {
            return rideRepository.findByUserIdAndStatus(userId, status, pageable).map(RideResponse::from);
        }
        if (userId != null) {
            return rideRepository.findByUserId(userId, pageable).map(RideResponse::from);
        }

        if (driverId != null && status != null) {
            return rideRepository.findByDriverIdAndStatus(driverId, status, pageable).map(RideResponse::from);
        }
        if (driverId != null) {
            return rideRepository.findByDriverId(driverId, pageable).map(RideResponse::from);
        }

        if (status != null) {
            return rideRepository.findByStatus(status, pageable).map(RideResponse::from);
        }

        // ADMIN list all
        return rideRepository.findAll(pageable).map(RideResponse::from);
    }

    // 10.4 Update ride status (admin)
    @PreAuthorize("hasRole('ADMIN')")
    public RideResponse updateRideStatus(Long id, UpdateRideStatusRequest request) {
        Ride ride = rideRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ride not found"));

        RideStatusTransitions.validate(ride.getStatus(), request.getStatus());

        ride.setStatus(request.getStatus());
        rideRepository.save(ride);

        return RideResponse.from(ride);
    }
}
