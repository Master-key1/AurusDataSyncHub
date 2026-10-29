package com.auruspay.vantiv.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldComparisonResult {

	@JsonProperty("Field")
    private int bit;
	@JsonProperty("FieldName")
    private String fieldName;
	@JsonProperty("ApprovedValue")
    private String approvedValue;
	@JsonProperty("DeclinedValue")
    private String declinedValue;
	@JsonProperty("Reason")
	private String reason = "";
	@JsonProperty("ApprovedSubFields")
    private Map<String,String> approvedSubFields;
	@JsonProperty("DeclinedSubFields")
    private Map<String,String> declinedSubFields;
	@JsonProperty("Value")
	private String value;
	@JsonProperty("Pattern")
	private String pattern;
    

    public FieldComparisonResult() {
    }
    

    public FieldComparisonResult(int bit, String fieldName, String approvedValue, String declinedValue) {
        this.bit = bit;
        this.fieldName = fieldName;
        this.approvedValue = approvedValue;
        this.declinedValue = declinedValue;
    }

    public void addIssue(String issue) {

        if (reason == null || reason.isBlank()) {
            reason = issue;
        } else {
            reason = reason + "; " + issue;
        }
    }

    // --- getters / setters (Jackson needs these for the requested field order) ---
    
    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public int getBit() {
        return bit;
    }

    public void setBit(int bit) {
        this.bit = bit;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getApprovedValue() {
        return approvedValue;
    }

    public void setApprovedValue(String approvedValue) {
        this.approvedValue = approvedValue;
    }

    public String getDeclinedValue() {
        return declinedValue;
    }

    public void setDeclinedValue(String declinedValue) {
        this.declinedValue = declinedValue;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public Map<String, String> getApprovedSubFields() {
        return approvedSubFields;
    }

    public void setApprovedSubFields(Map<String, String> approvedSubFields) {
        this.approvedSubFields = approvedSubFields;
    }

    public Map<String, String> getDeclinedSubFields() {
        return declinedSubFields;
    }

    public void setDeclinedSubFields(Map<String, String> declinedSubFields) {
        this.declinedSubFields = declinedSubFields;
    }
}