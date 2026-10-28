package com.auruspay.comparator.model;

/**
 * Result of comparing a single EMV TLV tag between the Approved and Declined
 * EMV data blocks.
 *
 * status:
 *   MATCH               - present in both, equal
 *   MISMATCH            - present in both, different values
 *   MISSING_IN_APPROVED - present in Declined only
 *   MISSING_IN_DECLINED - present in Approved only
 */
public record EMVComparisonResult(
        String tag,
        String tagName,
        String approvedValue,
        String declinedValue,
        String status,
        boolean mandatory,
        String reason
) {
    /**
     * Backward-compatible constructor for call sites still using the
     * original 4-field shape (tag, status, approvedValue, declinedValue).
     * tagName defaults to the raw tag, mandatory defaults to false, and
     * reason is left blank.
     */
    public EMVComparisonResult(String tag, String status, String approvedValue, String declinedValue) {
        this(tag, tag, approvedValue, declinedValue, status, false, "");
    }
}