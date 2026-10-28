package com.auruspay.comparator.model;

import java.util.List;

/**
 * Roll-up result of a full EMV validation pass across Approved and Declined
 * EMV data: structural (different card/transaction), terminal-config,
 * mandatory-tag, and format/value issues, plus a single overall verdict.
 */
public record EmvValidationSummary(
        boolean issueFound,
        int structuralIssueCount,
        int terminalIssueCount,
        int mandatoryIssueCount,
        int formatIssueCount,
        List<String> details
) {
}