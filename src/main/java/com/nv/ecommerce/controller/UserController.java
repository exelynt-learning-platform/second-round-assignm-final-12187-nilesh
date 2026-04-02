package com.nv.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nv.ecommerce.dto.request.UserLoginRequestDto;
import com.nv.ecommerce.dto.request.UserRegisterRequestDto;
import com.nv.ecommerce.dto.response.ApiResponse;
import com.nv.ecommerce.dto.response.UserLoginResponseDto;
import com.nv.ecommerce.service.UserService;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/auth")
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody UserRegisterRequestDto request) {

		userService.register(request);

		ApiResponse<Void> response = new ApiResponse<>();

		response.setStatus(HttpStatus.CREATED.value());
		response.setMessage("User registered successfully.");
		response.setData(null);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<UserLoginResponseDto>> login(@Valid @RequestBody UserLoginRequestDto request) {

		UserLoginResponseDto responseDto = userService.login(request);

		ApiResponse<UserLoginResponseDto> response = new ApiResponse<>();

		response.setStatus(HttpStatus.OK.value());
		response.setMessage("User Logged In successfully.");
		response.setData(responseDto);

		return ResponseEntity.status(HttpStatus.OK).body(response);

	}
}
