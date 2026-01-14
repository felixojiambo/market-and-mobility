package com.cymelle.app.rides.dto;

import com.cymelle.app.rides.RideStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRideStatusRequest {

    @NotNull(message = "status is required")
    private RideStatus status;
}
