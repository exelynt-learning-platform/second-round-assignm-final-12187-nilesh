package com.nv.ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemRemoveRequest {
	
	@NotNull(message = "Product ID is required")
	private Long productId;
}
