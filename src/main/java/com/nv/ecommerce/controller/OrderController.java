package com.nv.ecommerce.controller;

import com.nv.ecommerce.dto.request.OrderRequestDto;
import com.nv.ecommerce.dto.response.ApiResponse;
import com.nv.ecommerce.dto.response.OrderResponseDto;
import com.nv.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	// PLACE ORDER
	@PostMapping
	public ResponseEntity<ApiResponse<OrderResponseDto>> placeOrder(@RequestBody @Valid OrderRequestDto request) {

		OrderResponseDto order = orderService.placeOrder(request);

		ApiResponse<OrderResponseDto> response = new ApiResponse<>();
		response.setStatus(HttpStatus.CREATED.value());
		response.setMessage("Order placed successfully.");
		response.setData(order);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// GET MY ORDERS
	@GetMapping
	public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getMyOrders() {

		List<OrderResponseDto> orders = orderService.getMyOrders();

		ApiResponse<List<OrderResponseDto>> response = new ApiResponse<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage("Orders fetched successfully.");
		response.setData(orders);

		return ResponseEntity.ok(response);
	}

	// CANCEL ORDER
	@DeleteMapping("/{orderId}")
	public ResponseEntity<ApiResponse<Void>> cancelOrder(
			@PathVariable @NotNull(message = "orderId should not be null") Long orderId) {

		orderService.cancelOrder(orderId);

		ApiResponse<Void> response = new ApiResponse<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage("Order cancelled successfully.");
		response.setData(null);

		return ResponseEntity.ok(response);
	}
}