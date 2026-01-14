package com.cymelle.app.rides.dto;

import com.cymelle.app.rides.Ride;
import com.cymelle.app.rides.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideResponse {
    private Long id;
    private Long userId;
    private Long driverId;
    private String pickupLocation;
    private String dropoffLocation;
    private BigDecimal fare;
    private RideStatus status;
    private Instant createdAt;

    public static RideResponse from(Ride ride) {
        return RideResponse.builder()
                .id(ride.getId())
                .userId(ride.getUser().getId())
                .driverId(ride.getDriver() != null ? ride.getDriver().getId() : null)
                .pickupLocation(ride.getPickupLocation())
                .dropoffLocation(ride.getDropoffLocation())
                .fare(ride.getFare())
                .status(ride.getStatus())
                .createdAt(ride.getCreatedAt())
                .build();
    }
}
