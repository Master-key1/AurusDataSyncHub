package com.auruspay.comparator.model;

public class JsonRequest {

    private String declinedJson;
    private String approvedJson;

    public JsonRequest() {
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