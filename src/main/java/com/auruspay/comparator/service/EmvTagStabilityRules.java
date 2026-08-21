package com.auruspay.comparator.service;

import java.util.Set;

/**
 * Classifies EMV tags by what they represent, so a MISMATCH can be judged
 * correctly instead of treating every differing tag the same way:
 *
 * CARD_STATIC   - read from the card itself (AID, AIP, expiry, PAN sequence,
 *                 service code, usage control, form factor). If the SAME
 *                 physical card was used on both attempts, these MUST match.
 *                 A mismatch here means Approved and Declined used
 *                 different cards.
 *
 * TERMINAL_STATIC - fixed per physical terminal/device (country code, IFD
 *                 serial number, terminal capabilities, terminal type,
 *                 terminal app version, terminal's AID list). A mismatch
 *                 means a different terminal/device (or its config changed).
 *
 * CONTEXTUAL    - defines WHAT the transaction is (transaction type, amount,
 *                 currency). A mismatch means Approved and Declined are not
 *                 the same transaction attempt at all - comparing them as a
 *                 retry pair is not meaningful.
 *
 * CHANNEL       - how the card was presented (POS entry mode). A mismatch
 *                 is worth a note but is not necessarily wrong (e.g. a
 *                 contact-chip fallback after a failed contactless tap).
 *
 * DYNAMIC_PER_TRANSACTION - cryptographic/counter values that are
 *                 *supposed* to be different on every single transaction
 *                 (cryptogram, ATC, unpredictable number, date, sequence
 *                 counter, issuer application data, terminal risk results).
 *                 A mismatch here is expected and NOT indicative of a
 *                 problem by itself.
 *
 * UNCLASSIFIED  - no specific rule; treated as a plain mismatch.
 */
final class EmvTagStabilityRules {

    enum Stability {
        CARD_STATIC("CRITICAL"),
        TERMINAL_STATIC("WARNING"),
        CONTEXTUAL("CRITICAL"),
        CHANNEL("NOTICE"),
        DYNAMIC_PER_TRANSACTION("EXPECTED"),
        UNCLASSIFIED(null);

        private final String severity;

        Stability(String severity) {
            this.severity = severity;
        }

        String severity() {
            return severity;
        }
    }

    private static final Set<String> CARD_STATIC = Set.of(
            "4F", "84",       // AID (card / dedicated file name)
            "82",             // Application Interchange Profile
            "5F24",           // Application Expiration Date
            "5F25",           // Application Effective Date
            "5F30",           // Service Code
            "5F34",           // PAN Sequence Number
            "9F07",           // Application Usage Control
            "9F6E"            // Form Factor Indicator / Third Party Data
    );

    private static final Set<String> TERMINAL_STATIC = Set.of(
            "9F1A", // Terminal Country Code
            "9F1E", // IFD Serial Number
            "9F33", // Terminal Capabilities
            "9F35", // Terminal Type
            "9F09", // Application Version Number Terminal
            "9F06"  // Application Identifier (Terminal's supported AID)
    );

    private static final Set<String> CONTEXTUAL = Set.of(
            "9C",   // Transaction Type
            "9F02", // Amount Authorised
            "5F2A"  // Transaction Currency Code
    );

    private static final Set<String> CHANNEL = Set.of(
            "9F39"  // POS Entry Mode
    );

    private static final Set<String> DYNAMIC_PER_TRANSACTION = Set.of(
            "9F26", // Application Cryptogram
            "9F27", // Cryptogram Information Data
            "9F36", // Application Transaction Counter
            "9F37", // Unpredictable Number
            "9A",   // Transaction Date
            "9F21", // Transaction Time
            "9F41", // Transaction Sequence Counter
            "9F10", // Issuer Application Data
            "95"    // Terminal Verification Results
    );

    static Stability classify(String tag) {
        if (CARD_STATIC.contains(tag)) return Stability.CARD_STATIC;
        if (TERMINAL_STATIC.contains(tag)) return Stability.TERMINAL_STATIC;
        if (CONTEXTUAL.contains(tag)) return Stability.CONTEXTUAL;
        if (CHANNEL.contains(tag)) return Stability.CHANNEL;
        if (DYNAMIC_PER_TRANSACTION.contains(tag)) return Stability.DYNAMIC_PER_TRANSACTION;
        return Stability.UNCLASSIFIED;
    }

    private EmvTagStabilityRules() {
    }
}