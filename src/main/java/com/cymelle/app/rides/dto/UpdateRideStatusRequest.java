package com.cymelle.app.rides.dto;

import com.cymelle.app.rides.RideStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRideStatusRequest {

    @NotNull(message = "status is required")
    private RideStatus status;
}
