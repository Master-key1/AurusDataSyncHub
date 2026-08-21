package com.auruspay.comparator.service;

/**
 * Value-level checks that depend on GMF PymtType/TxnType context, on top of
 * the pure format checks in {@link EmvTagFormatRules}. These catch cases
 * where a tag is well-formed but semantically wrong for the transaction
 * category it's supposed to belong to.
 */
final class EmvContextualValueRules {

    /**
     * @return null if no contextual issue found, otherwise a human-readable
     *         description of the problem.
     */
    static String validate(String tag, String value, String pymtType, String txnType) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return switch (tag) {
            case "9C" -> validateTransactionType(value, pymtType, txnType);
            case "9F02" -> validateAmountAuthorised(value, pymtType, txnType);
            case "9F39" -> validateEntryMode(value);
            default -> null;
        };
    }

    private static String validateTransactionType(String value, String pymtType, String txnType) {
        boolean isRefund = txnType != null && "Refund".equalsIgnoreCase(txnType.trim());
        boolean isCredit = pymtType != null && "Credit".equalsIgnoreCase(pymtType.trim());

        if (isRefund && !"20".equals(value)) {
            return "GMF TxnType is Refund, so EMV Transaction Type (9C) was expected to be 20 (Refund) but was "
                    + value + " (" + EmvTxnTypeDictionary.nameOf(value) + ").";
        }
        if (!isRefund && isCredit && !"00".equals(value)) {
            return "GMF PymtType is Credit (a purchase), so EMV Transaction Type (9C) was expected to be 00 (Purchase) but was "
                    + value + " (" + EmvTxnTypeDictionary.nameOf(value) + ").";
        }
        return null;
    }

    private static String validateAmountAuthorised(String value, String pymtType, String txnType) {
        boolean isZero = value.matches("0+");
        boolean isRefund = txnType != null && "Refund".equalsIgnoreCase(txnType.trim());
        boolean isCredit = pymtType != null && "Credit".equalsIgnoreCase(pymtType.trim());

        if (isZero && (isRefund || isCredit)) {
            String category = isRefund ? "Refund" : "Credit";
            return "Amount Authorised (9F02) is zero, but " + category
                    + " transactions are expected to carry a non-zero amount.";
        }
        return null;
    }

    private static String validateEntryMode(String value) {
        // Common valid EMV POS Entry Mode byte values (contact/contactless chip)
        return switch (value.toUpperCase()) {
            case "05", "07", "01", "02", "10", "80", "91", "95" -> null;
            default -> "POS Entry Mode (9F39) = " + value + " is not a recognized EMV entry mode value.";
        };
    }

    private EmvContextualValueRules() {
    }
}