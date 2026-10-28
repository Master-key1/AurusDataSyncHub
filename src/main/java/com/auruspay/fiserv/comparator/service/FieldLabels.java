package com.auruspay.comparator.service;

import java.util.Map;

/**
 * Human-readable metadata for each GMF field, used to build grammatically
 * correct, field-specific validation messages.
 *
 * - label: the friendly name used in messages (e.g. "Order Number")
 * - descriptor: how to describe a format failure -
 *      "Invalid"      -> "Invalid {label} format ..."   (regex/range based fields)
 *      "Unrecognized" -> "Unrecognized {label} ..."      (fixed-set / enum based fields)
 */
final class FieldLabels {

    record FieldMeta(String label, String descriptor) {
    }

    static final Map<String, FieldMeta> META = Map.ofEntries(
            Map.entry("DID", new FieldMeta("DID", "Invalid")),
            Map.entry("App", new FieldMeta("App ID", "Unrecognized")),
            Map.entry("Auth", new FieldMeta("Auth", "Invalid")),
            Map.entry("ClientRef", new FieldMeta("Client Reference", "Invalid")),
            Map.entry("PymtType", new FieldMeta("Payment Type", "Unrecognized")),
            Map.entry("TxnType", new FieldMeta("Transaction Type", "Unrecognized")),
            Map.entry("LocalDateTime", new FieldMeta("Local Date/Time", "Invalid")),
            Map.entry("TrnmsnDateTime", new FieldMeta("Transmission Date/Time", "Invalid")),
            Map.entry("STAN", new FieldMeta("STAN", "Invalid")),
            Map.entry("POSEntryMode", new FieldMeta("POS Entry Mode", "Invalid")),
            Map.entry("TxnAmt", new FieldMeta("Transaction Amount", "Invalid")),
            Map.entry("MerchEcho", new FieldMeta("Merchant Echo", "Invalid")),
            Map.entry("OrderNum", new FieldMeta("Order Number", "Invalid")),
            Map.entry("RefNum", new FieldMeta("Reference Number", "Invalid")),
            Map.entry("TermID", new FieldMeta("Terminal ID", "Invalid")),
            Map.entry("MerchID", new FieldMeta("Merchant ID", "Invalid")),
            Map.entry("MerchCatCode", new FieldMeta("Merchant Category Code", "Invalid")),
            Map.entry("POSCondCode", new FieldMeta("POS Condition Code", "Unrecognized")),
            Map.entry("TermCatCode", new FieldMeta("Terminal Category Code", "Unrecognized")),
            Map.entry("TermEntryCapablt", new FieldMeta("Terminal Entry Capability", "Unrecognized")),
            Map.entry("TxnCrncy", new FieldMeta("Transaction Currency", "Invalid")),
            Map.entry("TermLocInd", new FieldMeta("Terminal Location Indicator", "Invalid")),
            Map.entry("CardCaptCap", new FieldMeta("Card Capture Capability", "Invalid")),
            Map.entry("ProgramID", new FieldMeta("Program ID", "Invalid")),
            Map.entry("GroupID", new FieldMeta("Group ID", "Unrecognized")),
            Map.entry("POSID", new FieldMeta("POS ID", "Invalid")),
            Map.entry("SettleInd", new FieldMeta("Settlement Indicator", "Invalid")),
            Map.entry("TranInit", new FieldMeta("Transaction Initiator", "Unrecognized")),
            Map.entry("AcctNum", new FieldMeta("Account Number", "Invalid")),
            Map.entry("CardExpiryDate", new FieldMeta("Card Expiry Date", "Invalid")),
            Map.entry("Track2Data", new FieldMeta("Track 2 Data", "Invalid")),
            Map.entry("CardType", new FieldMeta("Card Type", "Unrecognized")),
            Map.entry("AVSResultCode", new FieldMeta("AVS Result Code", "Invalid")),
            Map.entry("CCVInd", new FieldMeta("CCV Indicator", "Unrecognized")),
            Map.entry("CCVData", new FieldMeta("CCV Data", "Invalid")),
            Map.entry("CCVResultCode", new FieldMeta("CCV Result Code", "Unrecognized")),
            Map.entry("PINData", new FieldMeta("PIN Data", "Invalid")),
            Map.entry("KeySerialNumData", new FieldMeta("Key Serial Number Data", "Invalid")),
            Map.entry("AddAmtType", new FieldMeta("Additional Amount Type", "Unrecognized")),
            Map.entry("AddAmtAcctType", new FieldMeta("Additional Amount Account Type", "Unrecognized")),
            Map.entry("PartAuthrztnApprvlCapablt", new FieldMeta("Partial Authorization Approval Capability", "Invalid")),
            Map.entry("EMVData", new FieldMeta("EMV Data", "Invalid")),
            Map.entry("CardSeqNum", new FieldMeta("Card Sequence Number", "Invalid")),
            Map.entry("PC3Add", new FieldMeta("PC3 Additional Amount", "Invalid")),
            Map.entry("ACI", new FieldMeta("Authorization Characteristics Indicator (ACI)", "Unrecognized")),
            Map.entry("AVSBillingPostalCode", new FieldMeta("AVS Billing Postal Code", "Invalid")),

            Map.entry("AVSBillingAddr", new FieldMeta("AVS Billing Address", "Invalid")),
            Map.entry("DevTypeInd", new FieldMeta("Device Type Indicator", "Invalid")),
            Map.entry("EcommTxnInd", new FieldMeta("E-commerce Transaction Indicator", "Invalid")),
            Map.entry("InfoReqInd", new FieldMeta("Information Request Indicator", "Unrecognized")),
            Map.entry("ServiceID", new FieldMeta("Service ID", "Invalid")),
            Map.entry("TPPID", new FieldMeta("TPP ID", "Invalid"))
    );

    /** Falls back to the raw field code (with a generic "Invalid" descriptor) if no metadata is registered. */
    static FieldMeta of(String field) {
        return META.getOrDefault(field, new FieldMeta(field, "Invalid"));
    }

    private FieldLabels() {
    }
}