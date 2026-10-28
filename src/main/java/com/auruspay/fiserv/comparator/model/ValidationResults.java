package com.auruspay.comparator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Result of comparing a single GMF field's value between the Approved and
 * Declined transaction records.
 *
 * VALUE   - "MATCH" if ApprovedValue equals DeclinedValue, otherwise "MISMATCH"
 * PATTERN - "MATCHED" if BOTH values individually satisfy the field's expected
 *           format/business rule, otherwise "MISMATCH"
 * Reason  - human readable, field-specific explanation of the outcome
 */
@JsonPropertyOrder({ "FIELD", "ApprovedValue", "DeclinedValue", "VALUE", "Reason", "PATTERN" })
public class ValidationResults {

    @JsonProperty("FIELD")
    private final String field;

    @JsonProperty("ApprovedValue")
    private final String approvedValue;

    @JsonProperty("DeclinedValue")
    private final String declinedValue;

    @JsonProperty("VALUE")
    private final String value;

    @JsonProperty("Reason")
    private final String reason;

    @JsonProperty("PATTERN")
    private final String pattern;

    public ValidationResults(String field, String approvedValue, String declinedValue,
                              String value, String reason, String pattern) {
        this.field = field;
        this.approvedValue = approvedValue;
        this.declinedValue = declinedValue;
        this.value = value;
        this.reason = reason;
        this.pattern = pattern;
    }

    public String getField() {
        return field;
    }

    public String getApprovedValue() {
        return approvedValue;
    }

    public String getDeclinedValue() {
        return declinedValue;
    }

    public String getValue() {
        return value;
    }

    public String getReason() {
        return reason;
    }

    public String getPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return "ValidationResults{" +
                "FIELD='" + field + '\'' +
                ", ApprovedValue='" + approvedValue + '\'' +
                ", DeclinedValue='" + declinedValue + '\'' +
                ", VALUE='" + value + '\'' +
                ", Reason='" + reason + '\'' +
                ", PATTERN='" + pattern + '\'' +
                '}';
    }
}