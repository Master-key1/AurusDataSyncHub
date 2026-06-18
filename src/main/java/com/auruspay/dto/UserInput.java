package com.auruspay.dto;

public class UserInput {



    private String cctRequest;
    private String processorRequest;
   

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
