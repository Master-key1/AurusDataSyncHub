package com.auruspay.comparator.model;

public class IssueDetail {

    private String field;
    private String fieldName;
    private String declinedValue;
    
    private String approvedValue;
    private String comparisonResult;
    private String expectedMinLength;
    private String expectedMaxLength;
    private String expectedPattern;
    private String declinedLength;
    private String approvedLength;
    private String reason;
    private String patternMatch;

    public IssueDetail() {
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDeclinedValue() {
        return declinedValue;
    }

    public void setDeclinedValue(String declinedValue) {
        this.declinedValue = declinedValue;
    }

    public String getApprovedValue() {
        return approvedValue;
    }

    public void setApprovedValue(String approvedValue) {
        this.approvedValue = approvedValue;
    }

    public String getComparisonResult() {
        return comparisonResult;
    }

    public void setComparisonResult(String comparisonResult) {
        this.comparisonResult = comparisonResult;
    }

    public String getExpectedMinLength() {
        return expectedMinLength;
    }

    public void setExpectedMinLength(String expectedMinLength) {
        this.expectedMinLength = expectedMinLength;
    }

    public String getExpectedMaxLength() {
        return expectedMaxLength;
    }

    public void setExpectedMaxLength(String expectedMaxLength) {
        this.expectedMaxLength = expectedMaxLength;
    }

    public String getExpectedPattern() {
        return expectedPattern;
    }

    public void setExpectedPattern(String expectedPattern) {
        this.expectedPattern = expectedPattern;
    }

    public String getDeclinedLength() {
        return declinedLength;
    }

    public void setDeclinedLength(String declinedLength) {
        this.declinedLength = declinedLength;
    }

    public String getApprovedLength() {
        return approvedLength;
    }

    public void setApprovedLength(String approvedLength) {
        this.approvedLength = approvedLength;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPatternMatch() {
        return patternMatch;
    }

    public void setPatternMatch(String patternMatch) {
        this.patternMatch = patternMatch;
    }

    @Override
    public String toString() {
        return "IssueDetail{" +
                "field='" + field + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", declinedValue='" + declinedValue + '\'' +
                ", approvedValue='" + approvedValue + '\'' +
                ", comparisonResult='" + comparisonResult + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}