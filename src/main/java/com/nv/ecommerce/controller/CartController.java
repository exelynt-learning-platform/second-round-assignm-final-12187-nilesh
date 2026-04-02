package com.nv.ecommerce.controller;

import com.nv.ecommerce.dto.request.CartItemRemoveRequest;
import com.nv.ecommerce.dto.request.CartRequestDto;
import com.nv.ecommerce.dto.request.CartUpdateRequestDto;
import com.nv.ecommerce.dto.response.ApiResponse;
import com.nv.ecommerce.dto.response.CartResponseDto;
import com.nv.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

	private final CartService cartService;

	// ADD TO CART
	@PostMapping("/add")
	public ResponseEntity<ApiResponse<Void>> addToCart(@Valid @RequestBody CartRequestDto cartRequestDto) {

		cartService.addToCart(cartRequestDto.getProductId(), cartRequestDto.getQuantity());

		ApiResponse<Void> response = new ApiResponse<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage("Product added to cart successfully.");
		response.setData(null);

		return ResponseEntity.ok(response);
	}

	// GET CART
	@GetMapping
	public ResponseEntity<ApiResponse<CartResponseDto>> getCart() {

		CartResponseDto cart = cartService.getCart();

		ApiResponse<CartResponseDto> response = new ApiResponse<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage("Cart fetched successfully.");
		response.setData(cart);

		return ResponseEntity.ok(response);
	}

	// REMOVE ITEM FROM CART
	@DeleteMapping("/remove")
	public ResponseEntity<ApiResponse<Void>> removeFromCart( @Valid @RequestBody CartItemRemoveRequest cartItemRemoveRequest) {

		cartService.removeFromCart(cartItemRemoveRequest.getProductId());

		ApiResponse<Void> response = new ApiResponse<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage("Product removed from cart successfully.");
		response.setData(null);

		return ResponseEntity.ok(response);
	}

	// CLEAR CART
	@DeleteMapping("/clear")
	public ResponseEntity<ApiResponse<Void>> clearCart() {

		cartService.clearCart();

		ApiResponse<Void> response = new ApiResponse<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage("Cart cleared successfully.");
		response.setData(null);

		return ResponseEntity.ok(response);
	}

	@PatchMapping
	public ResponseEntity<ApiResponse<Void>> updateCart(@RequestBody @Valid CartUpdateRequestDto request) {

		cartService.updateCart(request.getProductId(), request.getQuantity());

		ApiResponse<Void> response = new ApiResponse<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage("Cart updated successfully.");
		response.setData(null);

		return ResponseEntity.ok(response);
	}
}