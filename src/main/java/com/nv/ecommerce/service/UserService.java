package com.nv.ecommerce.service;

import com.nv.ecommerce.dto.request.UserLoginRequestDto;
import com.nv.ecommerce.dto.request.UserRegisterRequestDto;
import com.nv.ecommerce.dto.response.UserLoginResponseDto;

public interface UserService {
	
	void register(UserRegisterRequestDto request);
	
	UserLoginResponseDto login(UserLoginRequestDto request);

}
