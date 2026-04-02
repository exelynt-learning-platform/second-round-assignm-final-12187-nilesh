package com.nv.ecommerce.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.nv.ecommerce.dto.request.UserLoginRequestDto;
import com.nv.ecommerce.dto.request.UserRegisterRequestDto;
import com.nv.ecommerce.dto.response.UserLoginResponseDto;
import com.nv.ecommerce.entity.User;
import com.nv.ecommerce.enums.AccountStatus;
import com.nv.ecommerce.enums.UserRole;
import com.nv.ecommerce.exception.DuplicateEmailException;
import com.nv.ecommerce.exception.ResourceNotFoundException;
import com.nv.ecommerce.repository.UserRepository;
import com.nv.ecommerce.security.JwtUtil;
import com.nv.ecommerce.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtil jwtUtil;

	@Override
	public void register(UserRegisterRequestDto request) {

		if (userRepository.findByUsername(request.getUsername()).isPresent()) {
			throw new DuplicateEmailException("Email already registered");
		}

		User user = new User();
		user.setUsername(request.getUsername());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(UserRole.USER);
		user.setStatus(AccountStatus.ACTIVE);

		userRepository.save(user);

	}

	@Override
	public UserLoginResponseDto login(UserLoginRequestDto request) {

		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		User user = userRepository.findByUsername(request.getUsername())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		String token = jwtUtil.generateToken(authentication, user.getId());

		return UserLoginResponseDto.builder().token(token).tokenType("Bearer").build();
	}

}
