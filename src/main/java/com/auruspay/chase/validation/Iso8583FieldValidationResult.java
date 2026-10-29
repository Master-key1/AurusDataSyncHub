package com.auruspay.chase.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Outcome of validating one DE (bit) across the approved and declined
 * messages being compared. approvedValid/declinedValid are independent -
 * a bit missing from one message but present/valid in the other is common
 * (optional fields), so this does NOT assume both sides must have the bit.
 */
public class Iso8583FieldValidationResult {

    private final int bit;
    private final String fieldName;
    private final String approvedValue;
    private final String declinedValue;
    private boolean approvedValid = true;
    private boolean declinedValid = true;
    private final List<String> issues = new ArrayList<>();

    public Iso8583FieldValidationResult(int bit, String fieldName, String approvedValue, String declinedValue) {
        this.bit = bit;
        this.fieldName = fieldName;
        this.approvedValue = approvedValue;
        this.declinedValue = declinedValue;
    }

    public void failApproved(String message) {
        approvedValid = false;
        issues.add("[approved] " + message);
    }

    public void failDeclined(String message) {
        declinedValid = false;
        issues.add("[declined] " + message);
    }

    public void note(String message) {
        issues.add(message);
    }

    public boolean isValid() {
        return approvedValid && declinedValid;
    }

    public int getBit() {
        return bit;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getApprovedValue() {
        return approvedValue;
    }

    public String getDeclinedValue() {
        return declinedValue;
    }

    public List<String> getIssues() {
        return issues;
    }

    @Override
    public String toString() {
        return String.format("Bit %-3d %-45s %s%s", bit, fieldName, isValid() ? "OK" : "FAIL",
                issues.isEmpty() ? "" : " -> " + issues);
    }
}