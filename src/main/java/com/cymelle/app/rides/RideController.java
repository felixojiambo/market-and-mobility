package com.cymelle.app.rides;

import com.cymelle.app.rides.dto.RequestRideRequest;
import com.cymelle.app.rides.dto.RideResponse;
import com.cymelle.app.rides.dto.UpdateRideStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    // 10.1 Request ride
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RideResponse requestRide(@Valid @RequestBody RequestRideRequest request) {
        return rideService.requestRide(request);
    }

    // 10.2 View ride
    @GetMapping("/{id}")
    public RideResponse getRide(@PathVariable Long id) {
        return rideService.getRide(id);
    }

    // 10.3 Search rides (paginated)
    @GetMapping
    public Page<RideResponse> searchRides(
            @RequestParam(required = false) RideStatus status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long driverId,
            Pageable pageable
    ) {
        return rideService.searchRides(userId, driverId, status, pageable);
    }

    // 10.4 Update ride status (admin)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public RideResponse updateRideStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRideStatusRequest request
    ) {
        return rideService.updateRideStatus(id, request);
    }
}
