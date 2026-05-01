package com.nv.ecommerce.controller;

import com.nv.ecommerce.dto.request.ProductRequestDto;
import com.nv.ecommerce.dto.response.ApiResponse;
import com.nv.ecommerce.dto.response.ProductResponseDto;
import com.nv.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class ProductController {

	private final ProductService productService;
	
	@PostMapping("/products-list")
	public ResponseEntity<ApiResponse<ProductResponseDto>> addProductList(
			@Valid @RequestBody List<ProductRequestDto> request) {

		productService.addProducts(request);

		ApiResponse<ProductResponseDto> response = new ApiResponse<>();
		response.setStatus(HttpStatus.CREATED.value());
		response.setMessage("Product list added successfully.");
		response.setData(null);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/products")
	public ResponseEntity<ApiResponse<ProductResponseDto>> createProduct(
			@Valid @RequestBody ProductRequestDto request) {

		ProductResponseDto product = productService.createProduct(request);

		ApiResponse<ProductResponseDto> response = new ApiResponse<>();
		response.setStatus(HttpStatus.CREATED.value());
		response.setMessage("Product created successfully.");
		response.setData(product);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/public/products/{id}")
	public ResponseEntity<ApiResponse<ProductResponseDto>> getProductById(
			@PathVariable @NotNull(message = "productId should not be null") Long id) {

		ProductResponseDto product = productService.getProductById(id);

		ApiResponse<ProductResponseDto> response = new ApiResponse<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage("Product fetched successfully.");
		response.setData(product);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/public/products")
	public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getAllProducts(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String sortBy, @RequestParam(defaultValue = "asc") String direction) {

		Page<ProductResponseDto> products = productService.getAllProducts(page, size, sortBy, direction);

		return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Products fetched successfully", products));
	}

	@PutMapping("/products/{id}")
	public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(
			@PathVariable @NotNull(message = "productId should not be null") Long id,
			@Valid @RequestBody ProductRequestDto request) {

		ProductResponseDto product = productService.updateProduct(id, request);

		ApiResponse<ProductResponseDto> response = new ApiResponse<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage("Product updated successfully.");
		response.setData(product);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/products/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteProduct(
			@PathVariable @NotNull(message = "productId should not be null") Long id) {

		productService.deleteProduct(id);

		ApiResponse<Void> response = new ApiResponse<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage("Product deleted successfully.");
		response.setData(null);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}