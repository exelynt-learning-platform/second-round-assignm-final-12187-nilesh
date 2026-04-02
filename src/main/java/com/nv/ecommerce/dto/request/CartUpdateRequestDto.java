package com.nv.ecommerce.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartUpdateRequestDto {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @Min(value = 0, message = "Quantity cannot be negative")
    private int quantity;
}