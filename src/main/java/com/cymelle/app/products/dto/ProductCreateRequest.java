package com.cymelle.app.products.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

    @NotBlank(message = "name is required")
    @Size(max = 120, message = "name must be at most 120 chars")
    private String name;

    @Size(max = 2000, message = "description must be at most 2000 chars")
    private String description;

    @NotNull(message = "price is required")
    @DecimalMin(value = "0.01", message = "price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "stockQuantity is required")
    @Min(value = 0, message = "stockQuantity must be >= 0")
    private Integer stockQuantity;

    @NotBlank(message = "category is required")
    @Size(max = 100, message = "category must be at most 100 chars")
    private String category;
}
