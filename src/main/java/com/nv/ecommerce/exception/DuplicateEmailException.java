package com.nv.ecommerce.exception;

public class DuplicateEmailException extends RuntimeException {

	private static final long serialVersionUID = 2149080713994232095L;

	public DuplicateEmailException(String msg) {
		super(msg);
	}
}