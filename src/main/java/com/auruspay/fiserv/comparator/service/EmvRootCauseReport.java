package com.auruspay.comparator.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Renders a human-readable, payment-domain root-cause report comparing
 * Approved vs Declined EMV data - grouped into "Potential Root Cause"
 * (transaction-defining + verification tags) and "Configuration /
 * Application Differences" (card/terminal identity tags), in the same
 * markdown table style used for manual EMV decline analysis.
 */
@Service
public class EmvRootCauseReport {

    public String generate(Map<String, String> approvedEmv, Map<String, String> declinedEmv) {
        List<String> rootCauseRows = new ArrayList<>();
        List<String> configRows = new ArrayList<>();

        TreeSet<String> allTags = new TreeSet<>();
        allTags.addAll(approvedEmv.keySet());
        allTags.addAll(declinedEmv.keySet());

        for (String tag : allTags) {
            String approvedValue = approvedEmv.get(tag);
            String declinedValue = declinedEmv.get(tag);

            if (approvedValue == null || declinedValue == null || approvedValue.equals(declinedValue)) {
                continue; // only mismatches are analyzed here
            }

            EmvRootCauseAnalysis.Tier tier = EmvRootCauseAnalysis.tierOf(tag);
            if (tier == EmvRootCauseAnalysis.Tier.EXPECTED) {
                continue; // dynamic per-transaction noise, excluded from the report
            }

            String analysis = EmvRootCauseAnalysis.analyze(tag, approvedValue, declinedValue);
            String tagName = EmvTagDictionary.nameOf(tag);

            if (tier == EmvRootCauseAnalysis.Tier.ROOT_CAUSE || tier == EmvRootCauseAnalysis.Tier.VERIFICATION_CHECK) {
                rootCauseRows.add(String.format("| %s (%s) | %s | %s | %s |",
                        tag, tagName, approvedValue, declinedValue, analysis));
            } else {
                configRows.add(String.format("| %s (%s) | %s |", tag, tagName, analysis));
            }
        }

        StringBuilder report = new StringBuilder();

        report.append("### \u274C Potential Root Cause\n");
        report.append("These tags are the ones most likely to cause the decline.\n\n");
        if (rootCauseRows.isEmpty()) {
            report.append("_No root-cause or verification-tag mismatches found._\n");
        } else {
            report.append("| Tag | Approved | Declined | Analysis |\n");
            report.append("|---|---|---|---|\n");
            rootCauseRows.forEach(row -> report.append(row).append("\n"));
        }

        report.append("\n### \u26A0\uFE0F Configuration / Application Differences\n");
        if (configRows.isEmpty()) {
            report.append("_No configuration/application tag mismatches found._\n");
        } else {
            report.append("| Tag | Reason |\n");
            report.append("|---|---|\n");
            configRows.forEach(row -> report.append(row).append("\n"));
            report.append("\nThese usually indicate a different card application, a different EMV kernel, ")
                    .append("or a different terminal configuration. They are not necessarily the root cause by themselves.\n");
        }

        return report.toString();
    }
}