package com.worldpay.iso8583.dto;

public class ExecutiveSummary {

    private String comparisonDate;

    private String approvedMTI;
    private String declinedMTI;

    private String approvedBitmap;
    private String declinedBitmap;

    private int totalFields;
    private int matchedFields;
    private int patternMatchedFields;
    private int notMatchedFields;
    private int onlyInApproved;
    private int onlyInDeclined;

    private String overallStatus;
    private String remarks;

    public String getComparisonDate() {
        return comparisonDate;
    }

    public void setComparisonDate(String comparisonDate) {
        this.comparisonDate = comparisonDate;
    }

    public String getApprovedMTI() {
        return approvedMTI;
    }

    public void setApprovedMTI(String approvedMTI) {
        this.approvedMTI = approvedMTI;
    }

    public String getDeclinedMTI() {
        return declinedMTI;
    }

    public void setDeclinedMTI(String declinedMTI) {
        this.declinedMTI = declinedMTI;
    }


    public int getTotalFields() {
        return totalFields;
    }

    public void setTotalFields(int totalFields) {
        this.totalFields = totalFields;
    }

    public int getMatchedFields() {
        return matchedFields;
    }

    public void setMatchedFields(int matchedFields) {
        this.matchedFields = matchedFields;
    }

    public int getPatternMatchedFields() {
        return patternMatchedFields;
    }

    public void setPatternMatchedFields(int patternMatchedFields) {
        this.patternMatchedFields = patternMatchedFields;
    }

    public int getNotMatchedFields() {
        return notMatchedFields;
    }

    public void setNotMatchedFields(int notMatchedFields) {
        this.notMatchedFields = notMatchedFields;
    }

    public int getOnlyInApproved() {
        return onlyInApproved;
    }

    public void setOnlyInApproved(int onlyInApproved) {
        this.onlyInApproved = onlyInApproved;
    }

    public int getOnlyInDeclined() {
        return onlyInDeclined;
    }

    public void setOnlyInDeclined(int onlyInDeclined) {
        this.onlyInDeclined = onlyInDeclined;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}