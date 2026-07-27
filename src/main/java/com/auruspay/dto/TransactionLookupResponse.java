package com.auruspay.dto;

public class TransactionLookupResponse extends RuntimeException {

    private ProcessRequest processRequest;
    private String lookupKey;

    public TransactionLookupResponse(ProcessRequest processRequest, String lookupKey) {
        this.processRequest = processRequest;
        this.lookupKey = lookupKey;
    }

    public TransactionLookupResponse() {
		// TODO Auto-generated constructor stub
	}

	public ProcessRequest getProcessRequest() {
		return processRequest;
	}

	public void setProcessRequest(ProcessRequest processRequest) {
		this.processRequest = processRequest;
	}

	public String getLookupKey() {
		return lookupKey;
	}

	public void setLookupKey(String lookupKey) {
		this.lookupKey = lookupKey;
	}
    
}