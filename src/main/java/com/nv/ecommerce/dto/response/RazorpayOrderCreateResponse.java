package com.nv.ecommerce.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RazorpayOrderCreateResponse {
	private String razorpayOrderId;
    private BigDecimal amount;
    private String currency;
}