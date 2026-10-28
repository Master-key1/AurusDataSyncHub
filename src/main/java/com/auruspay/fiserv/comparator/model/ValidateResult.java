package com.auruspay.comparator.model;

/**
 * Result of validating a single field's declined vs approved values across
 * three independent checks: Value equality, Pattern conformance, and Length
 * validity. Mirrors the JSON shape:
 *
 * {
 *   "field": "1.1",
 *   "fieldName": "MERCHANT NUMBER",
 *   "declinedValue": "100000090522",
 *   "approvedValue": "101231234030",
 *   "Value": "MISMATCH",
 *   "pattern": "MISMATCH",
 *   "Length": "Valid",
 *   "summary": "..."
 * }
 */
public class ValidateResult {

    private String field;
    private String fieldName;
    private String declinedValue;
    private String approvedValue;
    private String Value;      // "MATCH" | "MISMATCH"
    private String pattern;    // "VALID" | "MISMATCH"
    private String Length;     // "Valid" | "Invalid"
    private String summary;

    public ValidateResult() {
    }

    public ValidateResult(String field, String fieldName, String declinedValue, String approvedValue,
                           String value, String pattern, String length, String summary) {
        this.field = field;
        this.fieldName = fieldName;
        this.declinedValue = declinedValue;
        this.approvedValue = approvedValue;
        this.Value = value;
        this.pattern = pattern;
        this.Length = length;
        this.summary = summary;
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

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        this.Value = value;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getLength() {
        return Length;
    }

    public void setLength(String length) {
        this.Length = length;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}