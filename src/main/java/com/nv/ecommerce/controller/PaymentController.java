package com.nv.ecommerce.controller;

import com.nv.ecommerce.dto.request.PaymentRequestDto;
import com.nv.ecommerce.dto.request.RazorpayPaymentVerifyRequest;
import com.nv.ecommerce.dto.response.ApiResponse;
import com.nv.ecommerce.dto.response.PaymentResponseDto;
import com.nv.ecommerce.dto.response.RazorpayOrderCreateResponse;
import com.nv.ecommerce.dto.response.RazorpayPaymentVerifyResponse;
import com.nv.ecommerce.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	// CREATE RAZORPAY ORDER FOR PAYMENT
	@PostMapping("/create")
	public ResponseEntity<ApiResponse<RazorpayOrderCreateResponse>> createPayment(
			@Valid @RequestBody PaymentRequestDto request) {

		RazorpayOrderCreateResponse responseData = paymentService.createPayment(request);

		ApiResponse<RazorpayOrderCreateResponse> response = new ApiResponse<>();
		response.setStatus(HttpStatus.CREATED.value());
		response.setMessage("Razorpay order created successfully.");
		response.setData(responseData);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/verify")
	public ResponseEntity<ApiResponse<RazorpayPaymentVerifyResponse>> verifyPayment(
			@Valid @RequestBody RazorpayPaymentVerifyRequest request) {

		RazorpayPaymentVerifyResponse responseData = paymentService.verifyPayment(request);

		ApiResponse<RazorpayPaymentVerifyResponse> response = new ApiResponse<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage("Payment verified successfully.");
		response.setData(responseData);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<ApiResponse<PaymentResponseDto>> getPaymentDetails(@PathVariable Long orderId) {

		PaymentResponseDto payment = paymentService.getPaymentDetails(orderId);

		ApiResponse<PaymentResponseDto> response = new ApiResponse<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage("Payment details fetched successfully.");
		response.setData(payment);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/webhook")
	public ResponseEntity<String> handleWebhook(@RequestBody String payload,
			@RequestHeader("X-Razorpay-Signature") String signature) {

		paymentService.handleWebhook(payload, signature);

		return ResponseEntity.ok("Webhook processed");
	}
}