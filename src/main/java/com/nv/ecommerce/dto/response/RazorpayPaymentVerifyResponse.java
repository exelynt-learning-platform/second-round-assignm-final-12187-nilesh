package com.nv.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RazorpayPaymentVerifyResponse {

    private String status; // SUCCESS / FAILED
}