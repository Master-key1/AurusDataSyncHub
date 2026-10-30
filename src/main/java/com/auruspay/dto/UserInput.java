package com.auruspay.dto;

import jakarta.validation.constraints.NotBlank;

public class UserInput {

	@NotBlank
	private String cctRequest;
	@NotBlank
	private String cctResponse;
	private String procRequest;
	private String procResponse;
	private String ProcessorId;

	public String getCctRequest() {
		return cctRequest;
	}

	public void setCctRequest(String cctRequest) {
		this.cctRequest = cctRequest;
	}

	public String getCctResponse() {
		return cctResponse;
	}

	public void setCctResponse(String cctResponse) {
		this.cctResponse = cctResponse;
	}

	public String getProcRequest() {
		return procRequest;
	}

	public void setProcRequest(String procRequest) {
		this.procRequest = procRequest;
	}

	public String getProcResponse() {
		return procResponse;
	}

	public void setProcResponse(String procResponse) {
		this.procResponse = procResponse;
	}

	public String getProcessorId() {
		return ProcessorId;
	}

	public void setProcessorId(String processorId) {
		ProcessorId = processorId;
	}

}
