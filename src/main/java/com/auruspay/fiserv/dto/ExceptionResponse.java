package com.auruspay.dto;
public class ExceptionResponse {

    private String status;
    private String code;
    private String message;
    private String lookupKey;
    

    public ExceptionResponse(String status, String code, String message) {
		super();
		this.status = status;
		this.code = code;
		this.message = message;
	}
    

	public ExceptionResponse(String status, String code, String message, String lookupKey) {
		super();
		this.status = status;
		this.code = code;
		this.message = message;
		this.lookupKey = lookupKey;
	}


	public ExceptionResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getLookupKey() {
        return lookupKey;
    }

    public void setLookupKey(String lookupKey) {
        this.lookupKey = lookupKey;
    }
}