package com.auruspay.comparator.service;

import java.util.Map;

/** Human-readable names for common EMV Transaction Type (tag 9C) codes. */
final class EmvTxnTypeDictionary {

    private static final Map<String, String> NAMES = Map.of(
            "00", "Purchase",
            "01", "Cash Advance",
            "09", "Purchase with Cashback",
            "20", "Refund",
            "30", "Balance Inquiry"
    );

    static String nameOf(String code) {
        if (code == null) return "Unknown";
        return NAMES.getOrDefault(code, "Transaction Type " + code);
    }

    private EmvTxnTypeDictionary() {
    }
}