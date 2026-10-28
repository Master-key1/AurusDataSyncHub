package com.auruspay.comparator.service;

import com.auruspay.comparator.model.EMVComparisonResult;
import com.auruspay.comparator.model.EmvValidationSummary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Compares the EMV tag data extracted from the Approved and Declined
 * transactions, tag by tag.
 *
 * This goes beyond a flat "same/different" diff: each mismatch is judged
 * against what that tag actually represents (see {@link EmvTagStabilityRules}),
 * so the Reason text distinguishes between:
 *
 *   - CRITICAL   - card-static or contextual tags differ: Approved and
 *                  Declined are not a valid retry pair (different card,
 *                  different transaction type/amount/currency)
 *   - WARNING    - terminal-static tags differ: different physical
 *                  terminal/device or its configuration changed
 *   - NOTICE     - channel (entry mode) differs: worth a note, not
 *                  necessarily wrong (e.g. contactless fallback to contact)
 *   - EXPECTED   - cryptographic/counter tags differ: this is completely
 *                  normal between two separate transaction attempts and is
 *                  not evidence of a problem
 *
 * It also explicitly checks the card's Application Expiration Date (5F24)
 * against each side's Transaction Date (9A) and flags an expired card.
 */
@Service
public class EMVComparators {

    private static final Logger log = LoggerFactory.getLogger(EMVComparators.class);

    private static final String TAG_TXN_TYPE = "9C";
    private static final String TAG_AID_CARD = "4F";
    private static final String TAG_AID_DF = "84";
    private static final String TAG_EXPIRY = "5F24";
    private static final String TAG_TXN_DATE = "9A";

    /**
     * Backward-compatible entry point (no GMF PymtType/TxnType context).
     * Mandatory-tag messages fall back to generic "this transaction" wording.
     */
    public Map<String, EMVComparisonResult> compare(Map<String, String> approvedEmv, Map<String, String> declinedEmv) {
        return compare(approvedEmv, declinedEmv, null, null);
    }

    /**
     * @param pymtType GMF PymtType value (e.g. "Credit"), from either side of the transaction
     * @param txnType  GMF TxnType value (e.g. "Refund"), from either side of the transaction
     */
    public Map<String, EMVComparisonResult> compare(Map<String, String> approvedEmv, Map<String, String> declinedEmv,
                                                      String pymtType, String txnType) {

        String aid = firstNonNull(
                approvedEmv.get(TAG_AID_CARD), declinedEmv.get(TAG_AID_CARD),
                approvedEmv.get(TAG_AID_DF), declinedEmv.get(TAG_AID_DF));

        Set<String> mandatoryTags = EmvMandatoryRules.mandatoryTagsFor(pymtType, txnType, aid);
        String categoryLabel = EmvMandatoryRules.categoryLabel(pymtType, txnType);
        String categoryPhrase = categoryLabel != null ? categoryLabel + " transactions" : "this transaction";

        Set<String> allTags = new TreeSet<>();
        allTags.addAll(approvedEmv.keySet());
        allTags.addAll(declinedEmv.keySet());

        Map<String, EMVComparisonResult> results = new LinkedHashMap<>();

        for (String tag : allTags) {
            String approvedValue = approvedEmv.get(tag);
            String declinedValue = declinedEmv.get(tag);
            String tagName = EmvTagDictionary.nameOf(tag);
            boolean mandatory = mandatoryTags.contains(tag);
            EmvTagStabilityRules.Stability stability = EmvTagStabilityRules.classify(tag);

            String status;
            String reason;

            if (approvedValue != null && declinedValue != null) {
                if (approvedValue.equals(declinedValue)) {
                    status = "MATCH";
                    reason = tagName + " matches between Approved and Declined EMV data.";
                } else {
                    status = "MISMATCH";
                    reason = buildMismatchReason(tag, tagName, approvedValue, declinedValue, stability, mandatory, categoryPhrase);
                }
            } else if (approvedValue != null) {
                status = "MISSING_IN_DECLINED";
                reason = buildMissingReason(tag, tagName, "Declined", stability, mandatory, categoryPhrase);
            } else {
                status = "MISSING_IN_APPROVED";
                reason = buildMissingReason(tag, tagName, "Approved", stability, mandatory, categoryPhrase);
            }

            // Explicit card-expiry check, independent of match/mismatch status
            if (TAG_EXPIRY.equals(tag)) {
                reason = appendExpiryNotes(reason, approvedValue, declinedValue,
                        approvedEmv.get(TAG_TXN_DATE), declinedEmv.get(TAG_TXN_DATE));
            }

            if((reason.startsWith("[CRITICAL]") || reason.startsWith("[NOTICE]")))
            results.put(tag, new EMVComparisonResult(tag, tagName, approvedValue, declinedValue, status, mandatory, reason));
        }

        return results;
    }

    private String buildMismatchReason(String tag, String tagName, String approvedValue, String declinedValue,
                                        EmvTagStabilityRules.Stability stability, boolean mandatory, String categoryPhrase) {

        StringBuilder reason = new StringBuilder();

        switch (stability) {
            case CONTEXTUAL -> {
                if (TAG_TXN_TYPE.equals(tag)) {
                    reason.append("[CRITICAL] Transaction Type differs: Approved is ")
                            .append(EmvTxnTypeDictionary.nameOf(approvedValue))
                            .append(" (9C=").append(approvedValue).append("), Declined is ")
                            .append(EmvTxnTypeDictionary.nameOf(declinedValue))
                            .append(" (9C=").append(declinedValue).append("). ")
                            .append("Approved and Declined do not represent the same transaction attempt; ")
                            .append("comparing them as a retry pair is not meaningful.");
                } else {
                    reason.append("[CRITICAL] ").append(tagName)
                            .append(" differs between Approved (").append(approvedValue)
                            .append(") and Declined (").append(declinedValue).append("). ")
                            .append(tagName).append(" defines what the transaction is; ")
                            .append("a difference here means Approved and Declined are not the same transaction attempt.");
                }
            }
            case CARD_STATIC -> reason.append("[CRITICAL] ").append(tagName)
                    .append(" differs between Approved (").append(approvedValue)
                    .append(") and Declined (").append(declinedValue).append("). ")
                    .append(tagName).append(" is read from the card itself; a difference here means ")
                    .append("Approved and Declined used different physical cards, not a retry of the same card.");
            case TERMINAL_STATIC -> reason.append("[WARNING] ").append(tagName)
                    .append(" differs between Approved (").append(approvedValue)
                    .append(") and Declined (").append(declinedValue).append("). ")
                    .append(tagName).append(" is fixed per terminal device; a difference here suggests ")
                    .append("a different terminal was used, or its configuration changed between attempts.");
            case CHANNEL -> reason.append("[NOTICE] ").append(tagName)
                    .append(" differs between Approved (").append(approvedValue)
                    .append(") and Declined (").append(declinedValue).append("). ")
                    .append("The card was presented differently on each attempt (e.g. contact vs. contactless); ")
                    .append("verify this is expected before treating it as a decline cause.");
            case DYNAMIC_PER_TRANSACTION -> reason.append("[EXPECTED] ").append(tagName)
                    .append(" differs between Approved (").append(approvedValue)
                    .append(") and Declined (").append(declinedValue).append("). ")
                    .append(tagName).append(" is generated fresh per transaction (cryptogram/counter/date data); ")
                    .append("this difference is normal and is not, by itself, evidence of a problem.");
            default -> reason.append(tagName)
                    .append(" differs between Approved (").append(approvedValue)
                    .append(") and Declined (").append(declinedValue).append(").");
        }

        if (mandatory && stability != EmvTagStabilityRules.Stability.DYNAMIC_PER_TRANSACTION) {
            reason.append(" ").append(tagName).append(" is also mandatory for ").append(categoryPhrase).append(".");
        }

        return reason.toString();
    }

    private String buildMissingReason(String tag, String tagName, String missingSide,
                                       EmvTagStabilityRules.Stability stability, boolean mandatory, String categoryPhrase) {

        String severity = switch (stability) {
            case CARD_STATIC, CONTEXTUAL -> "[CRITICAL] ";
            case TERMINAL_STATIC -> "[WARNING] ";
            case CHANNEL -> "[NOTICE] ";
            default -> mandatory ? "[CRITICAL] " : "";
        };

        StringBuilder reason = new StringBuilder(severity)
                .append(tagName).append(" (").append(tag).append(") is missing in the ")
                .append(missingSide).append(" EMV data.");

        if (mandatory) {
            reason.append(" ").append(tagName).append(" is mandatory for ").append(categoryPhrase).append(".");
        }
        if (stability == EmvTagStabilityRules.Stability.CARD_STATIC) {
            reason.append(" This tag is a fixed card attribute and its absence suggests an incomplete EMV read ")
                    .append("or a different card than the other side.");
        }

        return reason.toString();
    }

    /**
     * Appends an explicit expiry check for the Application Expiration Date
     * (5F24), comparing each side's expiry against that side's own
     * Transaction Date (9A).
     */
    private String appendExpiryNotes(String baseReason, String approvedExpiry, String declinedExpiry,
                                      String approvedTxnDate, String declinedTxnDate) {
        StringBuilder reason = new StringBuilder(baseReason);

        if (approvedExpiry != null && isExpired(approvedExpiry, approvedTxnDate)) {
            reason.append(" [EXPIRED] Approved card's Application Expiration Date (")
                    .append(formatEmvDate(approvedExpiry))
                    .append(") had already passed as of its transaction date.");
        }
        if (declinedExpiry != null && isExpired(declinedExpiry, declinedTxnDate)) {
            reason.append(" [EXPIRED] Declined card's Application Expiration Date (")
                    .append(formatEmvDate(declinedExpiry))
                    .append(") had already passed as of its transaction date - likely cause of the decline.");
        }

        return reason.toString();
    }

    /**
     * @param expiryYYMMDD    tag 5F24 value (YYMMDD, card valid through end of that month)
     * @param referenceYYMMDD tag 9A value (YYMMDD) to check against; falls back to today if absent
     */
    private boolean isExpired(String expiryYYMMDD, String referenceYYMMDD) {
        if (expiryYYMMDD == null || expiryYYMMDD.length() != 6 || !expiryYYMMDD.matches("\\d{6}")) {
            return false;
        }
        try {
            int expYear = 2000 + Integer.parseInt(expiryYYMMDD.substring(0, 2));
            int expMonth = Integer.parseInt(expiryYYMMDD.substring(2, 4));
            LocalDate expiryLastDay = YearMonth.of(expYear, expMonth).atEndOfMonth();

            LocalDate reference;
            if (referenceYYMMDD != null && referenceYYMMDD.length() == 6 && referenceYYMMDD.matches("\\d{6}")) {
                int refYear = 2000 + Integer.parseInt(referenceYYMMDD.substring(0, 2));
                int refMonth = Integer.parseInt(referenceYYMMDD.substring(2, 4));
                int refDay = Integer.parseInt(referenceYYMMDD.substring(4, 6));
                reference = LocalDate.of(refYear, refMonth, refDay);
            } else {
                reference = LocalDate.now();
            }

            return reference.isAfter(expiryLastDay);
        } catch (NumberFormatException | DateTimeException e) {
            return false;
        }
    }

    private String formatEmvDate(String yymmdd) {
        if (yymmdd == null || yymmdd.length() != 6) return yymmdd;
        return "20" + yymmdd.substring(0, 2) + "-" + yymmdd.substring(2, 4) + "-" + yymmdd.substring(4, 6);
    }

    /**
     * Mandatory tags that are missing or mismatched - the original
     * mandatory-tag-only view.
     */
    public List<EMVComparisonResult> criticalIssues(Map<String, EMVComparisonResult> comparison) {
        return comparison.values().stream()
                .filter(r -> r.mandatory() && !"MATCH".equals(r.status()))
                .collect(Collectors.toList());
    }

    /**
     * Card-static or contextual tags that differ - the strongest signal that
     * Approved and Declined are not a valid retry pair (different card,
     * different transaction).
     */
    public List<EMVComparisonResult> structuralIssues(Map<String, EMVComparisonResult> comparison) {
        return comparison.values().stream()
                .filter(r -> !"MATCH".equals(r.status()))
                .filter(r -> {
                    var s = EmvTagStabilityRules.classify(r.tag());
                    return s == EmvTagStabilityRules.Stability.CARD_STATIC
                            || s == EmvTagStabilityRules.Stability.CONTEXTUAL;
                })
                .collect(Collectors.toList());
    }

    /**
     * Terminal-static tags that differ - suggests a different device or
     * terminal configuration change between attempts.
     */
    public List<EMVComparisonResult> terminalConfigIssues(Map<String, EMVComparisonResult> comparison) {
        return comparison.values().stream()
                .filter(r -> !"MATCH".equals(r.status()))
                .filter(r -> EmvTagStabilityRules.classify(r.tag()) == EmvTagStabilityRules.Stability.TERMINAL_STATIC)
                .collect(Collectors.toList());
    }

    /**
     * Tags that are expected to differ on every transaction (cryptogram,
     * ATC, unpredictable number, date, etc.) - informational only, not
     * evidence of a problem.
     */
    public List<EMVComparisonResult> expectedDifferences(Map<String, EMVComparisonResult> comparison) {
        return comparison.values().stream()
                .filter(r -> !"MATCH".equals(r.status()))
                .filter(r -> EmvTagStabilityRules.classify(r.tag()) == EmvTagStabilityRules.Stability.DYNAMIC_PER_TRANSACTION)
                .collect(Collectors.toList());
    }

    /**
     * Validates every tag in one side's EMV data against expected
     * format/length (all tags) and category-aware value rules (9C, 9F02,
     * 9F39). This is the check used for the "dynamic" tags - since their
     * VALUE is supposed to differ every transaction, equality is not the
     * right test; format correctness is.
     */
    public List<String> validateFormats(Map<String, String> emvData, String side, String pymtType, String txnType) {
        List<String> issues = new ArrayList<>();

        for (Map.Entry<String, String> entry : emvData.entrySet()) {
            String tag = entry.getKey();
            String value = entry.getValue();
            String tagName = EmvTagDictionary.nameOf(tag);

            String formatIssue = EmvTagFormatRules.validate(tag, value);
            if (formatIssue != null) {
                issues.add("[FORMAT] " + side + " " + tagName + " (" + tag + ") = " + value
                        + " does not match the expected format: " + formatIssue + ".");
            }

            String valueIssue = EmvContextualValueRules.validate(tag, value, pymtType, txnType);
            if (valueIssue != null) {
                issues.add("[VALUE] " + side + " " + tagName + " (" + tag + "): " + valueIssue);
            }
        }

        return issues;
    }

    /**
     * Runs the full validation pass - tag comparison, format/value checks on
     * both sides - and produces a single roll-up summary. Also prints the
     * result (structured, human-readable) so the outcome is visible in logs
     * without the caller needing to walk the comparison map themselves.
     */
    public EmvValidationSummary summarize(Map<String, String> approvedEmv, Map<String, String> declinedEmv,
                                           String pymtType, String txnType) {

        Map<String, EMVComparisonResult> comparison = compare(approvedEmv, declinedEmv, pymtType, txnType);

        List<EMVComparisonResult> structural = structuralIssues(comparison);
        List<EMVComparisonResult> terminal = terminalConfigIssues(comparison);
        List<EMVComparisonResult> mandatory = criticalIssues(comparison);

        List<String> formatIssues = new ArrayList<>();
        formatIssues.addAll(validateFormats(approvedEmv, "Approved", pymtType, txnType));
        formatIssues.addAll(validateFormats(declinedEmv, "Declined", pymtType, txnType));

        List<String> details = new ArrayList<>();
        structural.forEach(r -> details.add(r.reason()));
        terminal.forEach(r -> details.add(r.reason()));
        mandatory.stream()
                .filter(r -> details.stream().noneMatch(d -> d.equals(r.reason())))
                .forEach(r -> details.add(r.reason()));
        details.addAll(formatIssues);

        boolean issueFound = !structural.isEmpty() || !terminal.isEmpty()
                || !mandatory.isEmpty() || !formatIssues.isEmpty();

        EmvValidationSummary summary = new EmvValidationSummary(
                issueFound, structural.size(), terminal.size(), mandatory.size(), formatIssues.size(), details);

        printSummary(summary);

        return summary;
    }

    private void printSummary(EmvValidationSummary summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== EMV VALIDATION SUMMARY ==========\n");
        sb.append("Overall: ").append(summary.issueFound() ? "ISSUE FOUND" : "NO ISSUE FOUND").append("\n");
        sb.append("  Structural issues (different card/transaction): ").append(summary.structuralIssueCount()).append("\n");
        sb.append("  Terminal/device config differences:              ").append(summary.terminalIssueCount()).append("\n");
        sb.append("  Mandatory tag issues:                             ").append(summary.mandatoryIssueCount()).append("\n");
        sb.append("  Format/value issues:                              ").append(summary.formatIssueCount()).append("\n");
        if (!summary.details().isEmpty()) {
            sb.append("--- Details ---\n");
            for (String d : summary.details()) {
                sb.append("  - ").append(d).append("\n");
            }
        }
        sb.append("=============================================");

        System.out.println(sb);
        log.info(sb.toString());
    }

    private String firstNonNull(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }
}