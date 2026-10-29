package com.auruspay.vantiv.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;

import com.auruspay.vantiv.dto.ComparisonReport;
import com.auruspay.vantiv.dto.ExecutiveSummary;
import com.auruspay.vantiv.dto.FieldComparisonResult;
import com.auruspay.vantiv.model.FieldDefinition;
import com.auruspay.vantiv.model.Iso8583Message;
import com.auruspay.vantiv.parser.FieldDictionary;
import com.auruspay.vantiv.parser.Iso8583Parser;

@Service
public class MessageComparisonService {

    /**
     * Fields that identify the merchant/terminal/card, not the individual transaction attempt -
     * these SHOULD be identical between an approved and a declined message captured from the
     * same test session. Everything else (amount, STAN, timestamps, cryptograms, PIN block,
     * response code, etc.) is expected to legitimately differ and is not flagged just for differing.
     */
    private static final Set<Integer> EXPECTED_STATIC_BITS = Set.of(18, 19, 22, 25, 32, 41, 42, 43, 49);

    private final Iso8583Parser parser;

    public MessageComparisonService(Iso8583Parser parser) {
        this.parser = parser;
    }
    
    private boolean hasSamePattern(String value1, String value2) {

        if (value1 == null || value2 == null) {
            return false;
        }

        if (value1.length() != value2.length()) {
            return false;
        }

        for (int i = 0; i < value1.length(); i++) {

            char c1 = value1.charAt(i);
            char c2 = value2.charAt(i);

            if (Character.isDigit(c1) && Character.isDigit(c2)) {
                continue;
            }

            if (Character.isLetter(c1) && Character.isLetter(c2)) {
                continue;
            }

            if (Character.isWhitespace(c1) && Character.isWhitespace(c2)) {
                continue;
            }

            if (c1 == c2) {
                continue;
            }

            return false;
        }

        return true;
    }

    private ExecutiveSummary buildExecutiveSummary(
            Iso8583Message approved,
            Iso8583Message declined,
            List<FieldComparisonResult> results) {

        ExecutiveSummary summary = new ExecutiveSummary();

        summary.setComparisonDate(
                new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z")
                        .format(new Date()));

        summary.setApprovedMTI(approved.getMti());
        summary.setDeclinedMTI(declined.getMti());

        //summary.setApprovedBitmap(approved.getPrimaryBitmap());
     //   summary.setDeclinedBitmap(declined.getPrimaryBitmap());

        summary.setTotalFields(results.size());

        int matched = 0;
        int patternMatched = 0;
        int notMatched = 0;
        int onlyApproved = 0;
        int onlyDeclined = 0;

        for (FieldComparisonResult row : results) {

            if ("MATCHED".equals(row.getValue())) {
                matched++;
            }

            if ("MATCHED".equals(row.getPattern())
                    && "NOT MATCHED".equals(row.getValue())) {
                patternMatched++;
            }

            if ("NOT MATCHED".equals(row.getValue())) {
                notMatched++;
            }

            if ("Present in APPROVED but not in DECLINED"
                    .equals(row.getReason())) {
                onlyApproved++;
            }

            if ("Present in DECLINED but not in APPROVED"
                    .equals(row.getReason())) {
                onlyDeclined++;
            }
        }

        summary.setMatchedFields(matched);
        summary.setPatternMatchedFields(patternMatched);
        summary.setNotMatchedFields(notMatched);
        summary.setOnlyInApproved(onlyApproved);
        summary.setOnlyInDeclined(onlyDeclined);

        summary.setOverallStatus(
                notMatched == 0
                        && onlyApproved == 0
                        && onlyDeclined == 0
                        ? "Matched"
                        : "Not all Fields Matched");

        if ("Matched".equals(summary.getOverallStatus())) {

            summary.setRemarks(
                    "Approved and Declined messages are identical.");

        } else {

            summary.setRemarks(
                    "Approved and Declined messages contain differences in transaction-specific fields.");
        }

        return summary;
    }
    
    public ComparisonReport compare(String approvedHex, String declinedHex) {
        Iso8583Message approved = parser.parse(approvedHex);
        Iso8583Message declined = parser.parse(declinedHex);

        List<FieldComparisonResult> results = new java.util.ArrayList<>();

        // top-level parse problems (e.g. odd hex length) apply to the whole message, surface them
        // as a synthetic bit-0 row so they aren't silently dropped.
        if (!approved.getParseIssues().isEmpty() || !declined.getParseIssues().isEmpty()) {
            FieldComparisonResult header = new FieldComparisonResult(0, "Message-level parse status",
                    String.join("; ", approved.getParseIssues()),
                    String.join("; ", declined.getParseIssues()));
            header.setValue(
            	    approved.getParseIssues().isEmpty()
            	    && declined.getParseIssues().isEmpty()
            	        ? "MATCHED"
            	        : "NOT MATCHED");
            if (!approved.getParseIssues().isEmpty()) header.addIssue("APPROVED: " + String.join(" | ", approved.getParseIssues()));
            if (!declined.getParseIssues().isEmpty()) header.addIssue("DECLINED: " + String.join(" | ", declined.getParseIssues()));
            results.add(header);
        }

        Set<Integer> allBits = new TreeSet<>();
        allBits.addAll(approved.getPresentBits());
        allBits.addAll(declined.getPresentBits());

        for (int bit : allBits) {
            FieldDefinition def = FieldDictionary.get(bit);
            String name = def != null ? def.name() : "Unknown field";
            String approvedValue = approved.getFieldValues().getOrDefault(bit, "");
            String declinedValue = declined.getFieldValues().getOrDefault(bit, "");

            FieldComparisonResult row = new FieldComparisonResult(bit, name, approvedValue, declinedValue);

            boolean approvedPresent = approved.hasField(bit);
            boolean declinedPresent = declined.hasField(bit);

            boolean valid = true;

            if (!approvedPresent) {
                row.addIssue("Present in DECLINED but not in APPROVED");
                valid = false;
            }

            if (!declinedPresent) {
                row.addIssue("Present in APPROVED but not in DECLINED");
                valid = false;
            }

            if (approvedValue != null
                    && declinedValue != null
                    && EXPECTED_STATIC_BITS.contains(bit)
                    && !approvedValue.equals(declinedValue)) {

                valid = false;
            }

            if (approvedValue != null && declinedValue != null) {

                if (approvedValue.equals(declinedValue)) {

                    row.setValue("MATCHED");
                    row.setPattern("MATCHED");

                } else if (hasSamePattern(approvedValue, declinedValue)) {

                    row.setValue("NOT MATCHED");
                    row.setPattern("MATCHED");
                    row.addIssue("The approved and declined values match in pattern but differ in value.");

                } else {

                    row.setValue("NOT MATCHED");
                    row.setPattern("NOT MATCHED");
                    row.addIssue("Approved and declined values have different patterns.");
                }
            }
            if (bit == 60) {

                row.setApprovedSubFields(
                        approved.getSubFields().get(60));

                row.setDeclinedSubFields(
                        declined.getSubFields().get(60));
            }
            if(bit == 62){

                row.setApprovedSubFields(
                        approved.getSubFields(62));

                row.setDeclinedSubFields(
                        declined.getSubFields(62));

            }
            results.add(row);
        }
        
        ExecutiveSummary executiveSummary =
                buildExecutiveSummary(
                        approved,
                        declined,
                        results);

        ComparisonReport report = new ComparisonReport();

        report.setExecutiveSummary(executiveSummary);

        report.setComparison(results);

        return report;	
    }
}