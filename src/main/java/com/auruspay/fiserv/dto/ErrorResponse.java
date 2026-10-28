package com.auruspay.dto;

import java.time.LocalDateTime;

public class ErrorResponse {

    private String code;
    private String message;
    private LocalDateTime timestamp;
    
	public ErrorResponse() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ErrorResponse(String code, String message, LocalDateTime timestamp) {
		super();
		this.code = code;
		this.message = message;
		this.timestamp = timestamp;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
    
    
}