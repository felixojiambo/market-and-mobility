package com.cymelle.app.rides;

import com.cymelle.app.rides.dto.RideResponse;
import com.cymelle.app.rides.dto.UpdateRideStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    @GetMapping("/{id}")
    public RideResponse get(@PathVariable Long id) {
        return rideService.get(id);
    }

    @GetMapping("/search")
    public Page<RideResponse> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) RideStatus status,
            Pageable pageable
    ) {
        return rideService.search(userId, status, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public RideResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateRideStatusRequest req) {
        return rideService.updateStatus(id, req);
    }
}
