package com.nv.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequestDto {

    @NotBlank(message = "Shipping address must not be empty")
    private String shippingAddress;
}