package com.auruspay.comparator.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * Produces payment-domain-specific analysis text for a mismatched EMV tag,
 * grouped into three tiers:
 *
 *   ROOT_CAUSE          - defines WHAT the transaction is (type, amount,
 *                         currency). A mismatch here is, by itself, enough
 *                         to explain a decline/mismatch.
 *   VERIFICATION_CHECK  - reflects HOW the transaction was risk-assessed
 *                         (AIP, TVR, terminal capabilities, CVM results,
 *                         entry mode). Worth reviewing - contributes to but
 *                         doesn't single-handedly explain a decline.
 *   CONFIGURATION       - identifies WHICH card application/terminal/kernel
 *                         was used (AID, usage control, app version, IAD,
 *                         serial number). Informative but not, by itself,
 *                         the root cause.
 *   EXPECTED            - cryptographic/counter/date data that is supposed
 *                         to differ on every transaction. No analytical
 *                         value on its own.
 */
final class EmvRootCauseAnalysis {

    enum Tier { ROOT_CAUSE, VERIFICATION_CHECK, CONFIGURATION, EXPECTED, UNCLASSIFIED }

    private static final Set<String> ROOT_CAUSE_TAGS = Set.of("9C", "9F02", "5F2A");
    private static final Set<String> VERIFICATION_CHECK_TAGS = Set.of("82", "95", "9F33", "9F34", "9F39");
    private static final Set<String> CONFIGURATION_TAGS = Set.of(
            "4F", "84", "9F06", "9F07", "9F09", "9F10", "9F1E", "5F24", "5F30", "5F34", "9F6E");
    private static final Set<String> EXPECTED_TAGS = Set.of(
            "9F26", "9F27", "9F36", "9F37", "9A", "9F21", "9F41");

    static Tier tierOf(String tag) {
        if (ROOT_CAUSE_TAGS.contains(tag)) return Tier.ROOT_CAUSE;
        if (VERIFICATION_CHECK_TAGS.contains(tag)) return Tier.VERIFICATION_CHECK;
        if (CONFIGURATION_TAGS.contains(tag)) return Tier.CONFIGURATION;
        if (EXPECTED_TAGS.contains(tag)) return Tier.EXPECTED;
        return Tier.UNCLASSIFIED;
    }

    /** Tag-specific analysis text, tailored to each tag's real payment-domain meaning. */
    static String analyze(String tag, String approvedValue, String declinedValue) {
        return switch (tag) {
            case "9C" -> "Critical. Transaction Type changed: " + EmvTxnTypeDictionary.nameOf(approvedValue)
                    + " (" + approvedValue + ") vs " + EmvTxnTypeDictionary.nameOf(declinedValue)
                    + " (" + declinedValue + "). If Approved was a " + EmvTxnTypeDictionary.nameOf(approvedValue)
                    + " and Declined is a " + EmvTxnTypeDictionary.nameOf(declinedValue)
                    + ", this is a business flow mismatch, not a genuine retry of the same transaction.";

            case "9F02" -> "Critical. EMV Amount Authorised differs (" + formatAmount(approvedValue)
                    + " vs " + formatAmount(declinedValue) + "). This must match the ISO TxnAmt field; "
                    + "a mismatch here is by itself enough to explain why the two authorizations are not comparable.";

            case "5F2A" -> "Critical. Transaction Currency Code differs (" + approvedValue + " vs " + declinedValue
                    + "). Approved and Declined were authorized in different currencies - not the same transaction.";

            case "82" -> "Check. Application Interchange Profile (AIP) differs (" + approvedValue + " vs "
                    + declinedValue + "). Indicates the terminal read different card capabilities - "
                    + "consistent with a different card or application being used.";

            case "95" -> "Check. Terminal Verification Results (TVR) changed significantly (" + approvedValue
                    + " vs " + declinedValue + "), indicating the terminal/card risk verification outcome differed "
                    + "between the two attempts - worth reviewing which risk bits flipped.";

            case "9F33" -> "Check. Terminal Capabilities differ (" + approvedValue + " vs " + declinedValue
                    + "). The terminal reported different supported capabilities - consistent with a different "
                    + "device, or a firmware/configuration change.";

            case "9F34" -> "Check. Cardholder Verification Method (CVM) Results differ (" + approvedValue + " vs "
                    + declinedValue + "). A different verification method or outcome was used/recorded "
                    + "(e.g. signature vs PIN, or CVM failed on one attempt).";

            case "9F39" -> "Check. POS Entry Mode differs (" + approvedValue + " vs " + declinedValue
                    + "). The card was presented differently on each attempt (e.g. contact vs contactless); "
                    + "confirm this was an intentional fallback and not a data-capture error.";

            case "4F", "84" -> "Different Application Identifier (AID/DF Name): " + approvedValue + " vs "
                    + declinedValue + ". Approved and Declined selected a different card application/scheme.";

            case "9F06" -> "Different terminal-supported AID (" + approvedValue + " vs " + declinedValue
                    + "), consistent with a different card application being selected.";

            case "9F07" -> "Application Usage Control differs (" + approvedValue + " vs " + declinedValue
                    + "). The card's usage restrictions read differently - consistent with a different card.";

            case "9F09" -> "Application Version Number (Terminal) differs (" + approvedValue + " vs " + declinedValue
                    + "). Suggests different terminal firmware or EMV kernel version between attempts.";

            case "9F10" -> "Issuer Application Data differs (" + approvedValue + " vs " + declinedValue
                    + "). Expected to differ if a different card/issuer risk data was returned; "
                    + "can also legitimately vary transaction-to-transaction on the same card.";

            case "9F1E" -> "IFD Serial Number differs (" + approvedValue + " vs " + declinedValue
                    + "), indicating a different physical terminal device was used.";

            case "5F24" -> "Application Expiration Date differs (" + approvedValue + " vs " + declinedValue
                    + "). Card expiry is a fixed card attribute - a difference means different cards.";

            case "5F30" -> "Service Code differs (" + approvedValue + " vs " + declinedValue
                    + "). Fixed per card - a difference means different cards.";

            case "5F34" -> "PAN Sequence Number differs (" + approvedValue + " vs " + declinedValue
                    + "). Distinguishes reissued cards on the same account - a difference means a different "
                    + "physical card instance.";

            case "9F6E" -> "Form Factor Indicator / Third Party Data differs (" + approvedValue + " vs "
                    + declinedValue + "), consistent with a different card/device (e.g. phone vs plastic card).";

            case "9F26" -> "Application Cryptogram differs - expected on every transaction, not evidence of an issue by itself.";
            case "9F27" -> "Cryptogram Information Data differs - reflects the cryptogram type (ARQC/TC/AAC) requested per attempt.";
            case "9F36" -> "Application Transaction Counter differs - expected to increment on every transaction.";
            case "9F37" -> "Unpredictable Number differs - terminal-generated fresh per transaction by design.";
            case "9A" -> "Transaction Date differs - expected if the two attempts happened on different dates.";
            case "9F21" -> "Transaction Time differs - expected between separate attempts.";
            case "9F41" -> "Transaction Sequence Counter differs - expected to increment per transaction.";

            default -> {
                Tier tier = tierOf(tag);
                yield switch (tier) {
                    case EXPECTED -> EmvTagDictionary.nameOf(tag) + " differs - expected to vary per transaction, not evidence of an issue by itself.";
                    default -> EmvTagDictionary.nameOf(tag) + " differs (" + approvedValue + " vs " + declinedValue + ").";
                };
            }
        };
    }

    /** Formats a 12-digit ISO/EMV amount field (implied 2 decimal places) as a currency string, e.g. "000000000100" -> "1.00". */
    static String formatAmount(String amount12) {
        if (amount12 == null || !amount12.matches("\\d{1,12}")) {
            return amount12;
        }
        BigDecimal value = new BigDecimal(amount12).movePointLeft(2).setScale(2, RoundingMode.UNNECESSARY);
        return value.toPlainString();
    }

    private EmvRootCauseAnalysis() {
    }
}