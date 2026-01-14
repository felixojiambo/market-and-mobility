package com.cymelle.app.rides.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestRideRequest {

    // optional
    private Long driverId;

    @NotBlank(message = "pickupLocation is required")
    private String pickupLocation;

    @NotBlank(message = "dropoffLocation is required")
    private String dropoffLocation;

    @NotNull(message = "fare is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "fare must be >= 0")
    private BigDecimal fare;
}
