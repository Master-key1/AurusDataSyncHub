package com.auruspay.logservice.exception;

public class NoDataFoundException extends RuntimeException {

    private String status;
    private String code;
    private String lookupKey;

    public NoDataFoundException(String message) {
        super(message);
    }

    public NoDataFoundException(String message, String lookupKey) {
        super(message);
        this.lookupKey = lookupKey;
    }

    public NoDataFoundException(String status, String code, String message, String lookupKey) {
        super(message);
        this.status = status;
        this.code = code;
        this.lookupKey = lookupKey;
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

    public String getLookupKey() {
        return lookupKey;
    }

    public void setLookupKey(String lookupKey) {
        this.lookupKey = lookupKey;
    }
}