package com.auruspay.dto;

import jakarta.validation.constraints.NotBlank;

public class UserInput {

    @NotBlank
    private String cctRequest;

    private String processorRequest; // allows "" and null

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
}