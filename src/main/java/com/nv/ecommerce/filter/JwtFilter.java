package com.nv.ecommerce.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nv.ecommerce.security.JwtUtil;
import com.nv.ecommerce.service.impl.CustomUserDetailsService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final CustomUserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// 1. Get Authorization Header
		String authHeader = request.getHeader("Authorization");

		String token = null;
		String username = null;

		// 2. Check Bearer Token
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			token = authHeader.substring(7); // remove "Bearer "
			username = jwtUtil.extractUsername(token);
		}

		// 3. Validate & Authenticate
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			// Load user from DB
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);

			// Validate token
			if (jwtUtil.isTokenValid(token, userDetails.getUsername())) {

				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());

				// Attach request details
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				// Set authentication in context
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}

		// 4. Continue filter chain
		filterChain.doFilter(request, response);
	}
}