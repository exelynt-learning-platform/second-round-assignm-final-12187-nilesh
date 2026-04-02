package com.nv.ecommerce.exception;

public class HighConcrrencyException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public HighConcrrencyException(String message) {
		super(message);
	}

}
