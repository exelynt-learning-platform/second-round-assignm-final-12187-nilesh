package com.nv.ecommerce.service;

import com.nv.ecommerce.dto.request.PaymentRequestDto;
import com.nv.ecommerce.dto.request.RazorpayPaymentVerifyRequest;
import com.nv.ecommerce.dto.response.PaymentResponseDto;
import com.nv.ecommerce.dto.response.RazorpayOrderCreateResponse;
import com.nv.ecommerce.dto.response.RazorpayPaymentVerifyResponse;

public interface PaymentService {
	
	PaymentResponseDto getPaymentDetails(Long orderId);

    RazorpayOrderCreateResponse createPayment(PaymentRequestDto request);

    RazorpayPaymentVerifyResponse verifyPayment(RazorpayPaymentVerifyRequest request);
    
    void handleWebhook(String payload, String signature);
}