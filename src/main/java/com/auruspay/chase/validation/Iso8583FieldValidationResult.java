package com.auruspay.chase.validation;

import java.util.ArrayList;
import java.util.List;

public class Iso8583FieldValidationResult {

    private final int bit;
    private final String fieldName;
    private final String approvedValue;
    private final String declinedValue;

    private boolean approvedValid = true;
    private boolean declinedValid = true;

    private boolean compared = false;
    private boolean valuesMatch = false;

    private final List<String> issues = new ArrayList<>();

    public Iso8583FieldValidationResult(
            int bit,
            String fieldName,
            String approvedValue,
            String declinedValue) {

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

    // Format validation status for both requests
    public boolean isFormatValid() {
        return approvedValid && declinedValid;
    }

    public boolean isApprovedValid() {
        return approvedValid;
    }

    public boolean isDeclinedValid() {
        return declinedValid;
    }

    // Field comparison status
    public boolean isCompared() {
        return compared;
    }

    public void setCompared(boolean compared) {
        this.compared = compared;
    }

    public boolean isValuesMatch() {
        return valuesMatch;
    }

    public void setValuesMatch(boolean valuesMatch) {
        this.valuesMatch = valuesMatch;
    }

    // Overall validation status
    public boolean isValid() {
        return isFormatValid() && (!compared || valuesMatch);
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
        return List.copyOf(issues);
    }

    @Override
    public String toString() {
        return String.format(
                "Bit %-3d %-45s %s%s",
                bit,
                fieldName,
                isValid() ? "OK" : "FAIL",
                issues.isEmpty() ? "" : " -> " + issues
        );
    }
}