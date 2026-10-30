package com.auruspay.chase;

public class ChaseParseRequest {

    private String approvedRequest;
    private String declinedRequest;

    public String getApprovedRequest() {
        return approvedRequest;
    }

    public void setApprovedRequest(String approvedRequest) {
        this.approvedRequest = approvedRequest;
    }

    public String getDeclinedRequest() {
        return declinedRequest;
    }

    public void setDeclinedRequest(String declinedRequest) {
        this.declinedRequest = declinedRequest;
    }
}