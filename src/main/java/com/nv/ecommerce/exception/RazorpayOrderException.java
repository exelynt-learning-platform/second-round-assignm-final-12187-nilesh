package com.nv.ecommerce.exception;

public class RazorpayOrderException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	public RazorpayOrderException(String message) {
		super(message);
	}

}
