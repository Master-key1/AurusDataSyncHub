package com.auruspay.comparator.service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Determines which EMV tags are mandatory for a given transaction, and how
 * to describe *why* - which GMF category (Credit / Refund) requires it.
 *
 * Three inputs feed the mandatory-tag decision:
 *   - GMF PymtType   (e.g. "Credit", "Debit", ...)
 *   - GMF TxnType     (e.g. "Sale", "Refund", ...)
 *   - EMV tag 9C      (Transaction Type, as read from the card/terminal)
 *   - AID prefix       (tag 4F/84, identifies the card brand)
 *
 * Only tags that plausibly appear in the terminal's outbound authorization
 * EMVData block are considered here - issuer/script-response-only tags
 * (91, 71, 72, 8A, ...) are not treated as mandatory on the request side.
 */
final class EmvMandatoryRules {

    /** Mandatory on every EMV chip/contactless authorization request, regardless of category. */
    private static final Set<String> UNIVERSAL_MANDATORY = Set.of(
            "82",   // Application Interchange Profile
            "95",   // Terminal Verification Results
            "9A",   // Transaction Date
            "9C",   // Transaction Type
            "5F2A", // Transaction Currency Code
            "9F02", // Amount Authorised
            "9F26", // Application Cryptogram
            "9F27", // Cryptogram Information Data
            "9F36", // Application Transaction Counter
            "9F37", // Unpredictable Number
            "9F1A", // Terminal Country Code
            "9F33", // Terminal Capabilities
            "9F35", // Terminal Type
            "9F09", // Application Version Number Terminal
            "9F10"  // Issuer Application Data
    );

    /** Extra tags mandatory specifically for Credit (GMF PymtType = "Credit") transactions. */
    private static final Set<String> MANDATORY_FOR_CREDIT = Set.of(
            "9F34", // Cardholder Verification Method Results - CVM outcome must be reported for credit sales
            "5F24"  // Application Expiration Date - card expiry must be validated on a credit sale
    );

    /** Extra tags mandatory specifically for Refund (GMF TxnType = "Refund") transactions. */
    private static final Set<String> MANDATORY_FOR_REFUND = Set.of(
            "9F03", // Amount Other - carries the refund amount
            "89"    // Authorisation Code - links the refund back to the original approved sale
    );

    /** Extra tags mandatory for specific card brands, identified by AID prefix. */
    private static final Map<String, Set<String>> MANDATORY_BY_AID_PREFIX = Map.of(
            "A0000000031010", Set.of("9F6E"), // Visa
            "A0000000041010", Set.of("9F6E"), // Mastercard
            "A000000025",     Set.of("9F6E")  // American Express family
    );

    /**
     * Resolves the full mandatory-tag set for this transaction, combining
     * the universal set with any Credit/Refund/brand-specific additions.
     */
    static Set<String> mandatoryTagsFor(String pymtType, String txnType, String aid) {
        Set<String> mandatory = new LinkedHashSet<>(UNIVERSAL_MANDATORY);

        if (isCredit(pymtType)) {
            mandatory.addAll(MANDATORY_FOR_CREDIT);
        }
        if (isRefund(txnType)) {
            mandatory.addAll(MANDATORY_FOR_REFUND);
        }
        if (aid != null) {
            for (Map.Entry<String, Set<String>> entry : MANDATORY_BY_AID_PREFIX.entrySet()) {
                if (aid.startsWith(entry.getKey())) {
                    mandatory.addAll(entry.getValue());
                }
            }
        }
        return mandatory;
    }

    /**
     * Human-readable label for why a tag is mandatory, e.g. "Credit",
     * "Refund", "Credit and Refund", or null if neither category applies
     * (falls back to generic wording in the caller).
     */
    static String categoryLabel(String pymtType, String txnType) {
        boolean credit = isCredit(pymtType);
        boolean refund = isRefund(txnType);
        if (credit && refund) return "Credit and Refund";
        if (credit) return "Credit";
        if (refund) return "Refund";
        return null;
    }

    private static boolean isCredit(String pymtType) {
        return pymtType != null && "Credit".equalsIgnoreCase(pymtType.trim());
    }

    private static boolean isRefund(String txnType) {
        return txnType != null && "Refund".equalsIgnoreCase(txnType.trim());
    }

    private EmvMandatoryRules() {
    }
}