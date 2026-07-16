package com.auruspay.comparator.model;

public record EMVComparisonResult(
        String tag,
        String approvedValue,
        String declinedValue,
        String status) {
}