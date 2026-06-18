package com.auruspay.exception;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.auruspay.dto.ErrorResponse;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<String> handleJsonError(HttpMessageNotReadableException ex) {

		return ResponseEntity.badRequest().body("Invalid JSON Request: " + ex.getMostSpecificCause().getMessage());
	}

	@ExceptionHandler(MismatchedInputException.class)
	public ResponseEntity<ErrorResponse> handleMismatchedInputException(MismatchedInputException ex) {

		ErrorResponse response = new ErrorResponse();
		response.setCode("BAD_REQUEST");
		response.setMessage("Request body is empty or invalid JSON.");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Malformed JSON request.");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
		return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
				ex.getBindingResult().getFieldError().getDefaultMessage());
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
		return buildResponse(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER",
				ex.getParameterName() + " parameter is missing.");
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
		return buildResponse(HttpStatus.BAD_REQUEST, "MISSING_HEADER", ex.getHeaderName() + " header is missing.");
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException ex) {
		return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
	}

	@ExceptionHandler(SQLException.class)
	public ResponseEntity<ErrorResponse> handleSQLException(SQLException ex) {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SQL_ERROR", ex.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
		return buildResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
				"You do not have permission to access this resource.");
	}


	@ExceptionHandler(FileNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleFileNotFoundException(FileNotFoundException ex) {
		return buildResponse(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", ex.getMessage());
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "IO_ERROR", ex.getMessage());
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
		return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE",
				"Uploaded file exceeds maximum allowed size.");
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
		return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", ex.getMessage());
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
		return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", ex.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
		return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage());
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
		return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_STATE", ex.getMessage());
	}

	@ExceptionHandler(NumberFormatException.class)
	public ResponseEntity<ErrorResponse> handleNumberFormatException(NumberFormatException ex) {
		return buildResponse(HttpStatus.BAD_REQUEST, "NUMBER_FORMAT_ERROR", "Invalid number format.");
	}

	@ExceptionHandler(ArithmeticException.class)
	public ResponseEntity<ErrorResponse> handleArithmeticException(ArithmeticException ex) {
		return buildResponse(HttpStatus.BAD_REQUEST, "ARITHMETIC_ERROR", ex.getMessage());
	}

	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException ex) {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "NULL_POINTER", "Unexpected null value encountered.");
	}

	@ExceptionHandler(TimeoutException.class)
	public ResponseEntity<ErrorResponse> handleTimeoutException(TimeoutException ex) {
		return buildResponse(HttpStatus.REQUEST_TIMEOUT, "TIMEOUT", "Request timed out.");
	}

	@ExceptionHandler(ConnectException.class)
	public ResponseEntity<ErrorResponse> handleConnectException(ConnectException ex) {
		return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "CONNECTION_ERROR", ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", ex.getMessage());
	}

	private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code, String message) {

		ErrorResponse response = new ErrorResponse();
		response.setCode(code);
		response.setMessage(message);
		response.setTimestamp(LocalDateTime.now());

		return ResponseEntity.status(status).body(response);
	}
}