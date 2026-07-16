package com.auruspay.dto;

public class ProcessRequest {

    private String cctRequest;
    private String processorRequest;
    private String processorResponse;
    private String cctResponse;
    private String processorId ;
    
    

    public String getProcessorId() {
		return processorId;
	}

	public void setProcessorId(String processorId) {
		this.processorId = processorId;
	}

	public String getCctRequest() {
        return cctRequest;
    }

    public void setCctRequest(String cctRequest) {
        this.cctRequest = cctRequest;
    }

    public String getProcessorRequest() {
        return processorRequest;
    }

    public void setProcessorRequest(String processorRequest) {
        this.processorRequest = processorRequest;
    }

    public String getProcessorResponse() {
        return processorResponse;
    }

    public void setProcessorResponse(String processorResponse) {
        this.processorResponse = processorResponse;
    }

    public String getCctResponse() {
        return cctResponse;
    }

    public void setCctResponse(String cctResponse) {
        this.cctResponse = cctResponse;
    }

	@Override
	public String toString() {
		return "ProcessRequest [cctRequest=" + cctRequest + ", processorRequest=" + processorRequest
				+ ", processorResponse=" + processorResponse + ", cctResponse=" + cctResponse + "]";
	}
    
    
}