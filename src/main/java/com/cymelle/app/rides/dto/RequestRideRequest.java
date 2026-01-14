package com.cymelle.app.rides.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestRideRequest {

    // optional
    private Long driverId;

    @NotBlank(message = "pickupLocation is required")
    @Size(max = 255, message = "pickupLocation must be at most 255 chars")
    private String pickupLocation;

    @NotBlank(message = "dropoffLocation is required")
    @Size(max = 255, message = "dropoffLocation must be at most 255 chars")
    private String dropoffLocation;

    @NotNull(message = "fare is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "fare must be >= 0")
    private BigDecimal fare;
}
