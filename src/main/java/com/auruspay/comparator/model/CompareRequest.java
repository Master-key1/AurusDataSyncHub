package com.auruspay.comparator.model;

public class CompareRequest {

    private String declinedJson;
    private String approvedJson;

    public CompareRequest() {
    }

    public String getDeclinedJson() {
        return declinedJson;
    }

    public void setDeclinedJson(String declinedJson) {
        this.declinedJson = declinedJson;
    }

    public String getApprovedJson() {
        return approvedJson;
    }

    public void setApprovedJson(String approvedJson) {
        this.approvedJson = approvedJson;
    }
}