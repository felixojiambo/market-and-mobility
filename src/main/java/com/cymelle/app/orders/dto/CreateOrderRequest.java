package com.cymelle.app.orders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @Valid
    @NotEmpty(message = "items must not be empty")
    private List<OrderItemRequest> items;
}
