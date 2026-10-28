package com.auruspay.dto;

public class CustomResponse {

    private String status;
    private Object message;
    private String lookupKey;

    // Default Constructor
    public CustomResponse() {
    }

    // Parameterized Constructor
    public CustomResponse(String status, Object message, String lookupKey) {
        this.status = status;
        this.message = message;
        this.lookupKey = lookupKey;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public String getLookupKey() {
        return lookupKey;
    }

    public void setLookupKey(String lookupKey) {
        this.lookupKey = lookupKey;
    }

    // toString method (useful for logging/debugging)
    @Override
    public String toString() {
        return "CustomResponse{" +
                "status='" + status + '\'' +
                ", message=" + message +
                ", lookupKey='" + lookupKey + '\'' +
                '}';
    }
}