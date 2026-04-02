package com.nv.ecommerce.exception;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import com.nv.ecommerce.dto.response.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<ErrorResponse> HandleUsernameNotFound(UsernameNotFoundException ex,
			HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication failed", request.getRequestURI());
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
			HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not supported for this endpoint",
				request.getRequestURI());
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.NOT_FOUND, "Endpoint not found", request.getRequestURI());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpServletRequest request) {

		String message = "Request body is missing or malformed";

		return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<ErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException ex,
			HttpServletRequest request) {

		String message = "Required header '" + ex.getHeaderName() + "' is missing";

		return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
			HttpServletRequest request) {

		String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", ex.getValue(),
				ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

		return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpServletRequest request) {

		// Extract the first error message
		String errorMessage = ex.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + ": " + err.getDefaultMessage()).findFirst() // gives only first filed error
				.orElse("Validation failed");

	
		return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMessage, request.getRequestURI());
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
			HttpServletRequest request) {

		String message = ex.getConstraintViolations().stream().map(ConstraintViolation::getMessage).findFirst()
				.orElse("Invalid request");

		return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex,
			HttpServletRequest request) {

		String message = "Database constraint violation";

		return buildErrorResponse(HttpStatus.CONFLICT, message, request.getRequestURI());
	}

	@ExceptionHandler(SQLIntegrityConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleSQLIntegrityConstraintViolation(
			SQLIntegrityConstraintViolationException ex, HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.CONFLICT, "Database constraint violated", request.getRequestURI());
	}

	@ExceptionHandler(EmptyResultDataAccessException.class)
	public ResponseEntity<ErrorResponse> handleEmptyResultDataAccess(EmptyResultDataAccessException ex,
			HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.NOT_FOUND, "Resource not found", request.getRequestURI());
	}

	// Occurs when transaction fails during commit or rollback
	// Mostly caused by validation failure or constraint violation during flush
	@ExceptionHandler(TransactionSystemException.class)
	public ResponseEntity<ErrorResponse> handleTransactionSystem(TransactionSystemException ex,
			HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.BAD_REQUEST, "Transaction failed due to invalid data",
				request.getRequestURI());
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Business (Custom)
	////////////////////////////////////// Exceptions
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
			HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(DuplicateEmailException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
	}
	
	@ExceptionHandler(InsufficientStockException.class)
	public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex,
			HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
	}
	
	@ExceptionHandler(EmptyCartException.class)
	public ResponseEntity<ErrorResponse> handleEmptyCart(EmptyCartException ex,
			HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex,
			HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
	}
	
	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ErrorResponse> handleCustomAccessDenied(CustomAccessDeniedException ex,
			HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
	}
	
	@ExceptionHandler(ResourceAlreadyExistException.class)
	public ResponseEntity<ErrorResponse> handleResourceAlreadyExist(ResourceAlreadyExistException ex,
			HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.FOUND, ex.getMessage(), request.getRequestURI());
	}
	
	@ExceptionHandler(RazorpayOrderException.class)
	public ResponseEntity<ErrorResponse> handleRazorpayOrder(RazorpayOrderException ex,
			HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
	}

	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {

		return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", request.getRequestURI());
	}

	private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, String path) {

		ErrorResponse error = new ErrorResponse();
		error.setTimestamp(LocalDateTime.now());
		error.setStatus(status.value());
		error.setError(status.name());
		error.setMessage(message);
		error.setPath(path);

		return ResponseEntity.status(status).body(error);
	}
}