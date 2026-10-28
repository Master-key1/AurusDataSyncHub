package com.auruspay.dto;

public class TransactionResponseDto {

    private String message;
    private String endpoint;
    private String requestPayload;  
    private String decodedResponse;
    
    


	public String getDecodedResponse() {
		return decodedResponse;
	}

	public void setDecodedResponse(String decodedResponse) {
		this.decodedResponse = decodedResponse;
	}

	

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }

}