package com.auruspay.comparator.service;

import java.util.Map;

/**
 * Human-readable names for EMV TLV tags. Sourced from the full Aurus
 * emvtags.xml reference list.
 */
final class EmvTagDictionary {

    private static final Map<String, String> NAMES = Map.ofEntries(
            Map.entry("4F", "Application Identifier (AID)"),
            Map.entry("50", "Application Label"),
            Map.entry("57", "Track 2 Equivalent Data"),
            Map.entry("5A", "Application PAN"),
            Map.entry("5F20", "Cardholder Name"),
            Map.entry("5F24", "Application Expiration Date"),
            Map.entry("5F25", "Application Effective Date"),
            Map.entry("5F28", "Issuer Country Code"),
            Map.entry("5F2A", "Transaction Currency Code"),
            Map.entry("5F2D", "Language Preference"),
            Map.entry("5F30", "Service Code"),
            Map.entry("5F34", "PAN Sequence Number"),
            Map.entry("5F36", "Transaction Currency Exponent"),

            Map.entry("61", "Application Template"),
            Map.entry("6F", "FCI Template"),
            Map.entry("70", "EMV Record Template"),
            Map.entry("71", "Issuer Script Template 1"),
            Map.entry("72", "Issuer Script Template 2"),
            Map.entry("73", "Directory Discretionary Template"),
            Map.entry("77", "Response Message Template"),
            Map.entry("80", "Response Message Template Format 1"),

            Map.entry("82", "Application Interchange Profile"),
            Map.entry("84", "Dedicated File Name"),
            Map.entry("87", "Application Priority Indicator"),
            Map.entry("88", "SFI"),
            Map.entry("89", "Authorisation Code"),
            Map.entry("8A", "Authorisation Response Code"),
            Map.entry("8C", "CDOL1"),
            Map.entry("8D", "CDOL2"),
            Map.entry("8E", "Cardholder Verification Method List"),
            Map.entry("8F", "Certification Authority Public Key Index"),

            Map.entry("90", "Issuer Public Key Certificate"),
            Map.entry("91", "Issuer Authentication Data"),
            Map.entry("92", "Issuer Public Key Remainder"),
            Map.entry("93", "Signed Static Application Data"),
            Map.entry("94", "Application File Locator"),
            Map.entry("95", "Terminal Verification Results"),

            Map.entry("97", "Transaction Certificate Data Object List"),
            Map.entry("98", "Transaction Certificate Hash Value"),
            Map.entry("99", "Transaction PIN Data"),
            Map.entry("9A", "Transaction Date"),
            Map.entry("9B", "Transaction Status Information"),
            Map.entry("9C", "Transaction Type"),
            Map.entry("9D", "Directory Definition File Name"),

            Map.entry("9F01", "Acquirer Identifier"),
            Map.entry("9F02", "Amount Authorised"),
            Map.entry("9F03", "Amount Other"),
            Map.entry("9F04", "Amount Other Binary"),
            Map.entry("9F05", "Application Discretionary Data"),
            Map.entry("9F06", "Application Identifier"),
            Map.entry("9F07", "Application Usage Control"),
            Map.entry("9F08", "Application Version Number"),
            Map.entry("9F09", "Application Version Number Terminal"),
            Map.entry("9F0B", "Cardholder Name Extended"),
            Map.entry("9F0D", "Issuer Action Code Default"),
            Map.entry("9F0E", "Issuer Action Code Denial"),
            Map.entry("9F0F", "Issuer Action Code Online"),

            Map.entry("9F10", "Issuer Application Data"),
            Map.entry("9F11", "Issuer Code Table Index"),
            Map.entry("9F12", "Application Preferred Name"),
            Map.entry("9F13", "Last Online ATC Register"),
            Map.entry("9F14", "Lower Consecutive Offline Limit"),
            Map.entry("9F15", "Merchant Category Code"),
            Map.entry("9F16", "Merchant Identifier"),
            Map.entry("9F17", "PIN Try Counter"),
            Map.entry("9F18", "Issuer Script Identifier"),
            Map.entry("9F1A", "Terminal Country Code"),
            Map.entry("9F1B", "Terminal Floor Limit"),
            Map.entry("9F1C", "Terminal Identification"),
            Map.entry("9F1D", "Terminal Risk Management Data"),
            Map.entry("9F1E", "IFD Serial Number"),
            Map.entry("9F1F", "Track 1 Discretionary Data"),

            Map.entry("9F20", "Track 2 Discretionary Data"),
            Map.entry("9F21", "Transaction Time"),
            Map.entry("9F22", "CA Public Key Index"),
            Map.entry("9F23", "Upper Consecutive Offline Limit"),
            Map.entry("9F26", "Application Cryptogram"),
            Map.entry("9F27", "Cryptogram Information Data"),
            Map.entry("9F2D", "ICC PIN Encipherment Public Key Certificate"),
            Map.entry("9F2E", "ICC PIN Encipherment Public Key Exponent"),
            Map.entry("9F2F", "ICC PIN Encipherment Public Key Remainder"),

            Map.entry("9F32", "Issuer Public Key Exponent"),
            Map.entry("9F33", "Terminal Capabilities"),
            Map.entry("9F34", "Cardholder Verification Method Results"),
            Map.entry("9F35", "Terminal Type"),
            Map.entry("9F36", "Application Transaction Counter"),
            Map.entry("9F37", "Unpredictable Number"),
            Map.entry("9F38", "PDOL"),
            Map.entry("9F39", "POS Entry Mode"),
            Map.entry("9F3A", "Amount Reference Currency"),
            Map.entry("9F3B", "Application Reference Currency"),
            Map.entry("9F3C", "Transaction Reference Currency Code"),
            Map.entry("9F3D", "Transaction Reference Currency Exponent"),

            Map.entry("9F40", "Additional Terminal Capabilities"),
            Map.entry("9F41", "Transaction Sequence Counter"),
            Map.entry("9F42", "Application Currency Code"),
            Map.entry("9F43", "Application Reference Currency Exponent"),
            Map.entry("9F44", "Application Currency Exponent"),
            Map.entry("9F45", "Data Authentication Code"),
            Map.entry("9F46", "ICC Public Key Certificate"),
            Map.entry("9F47", "ICC Public Key Exponent"),
            Map.entry("9F48", "ICC Public Key Remainder"),
            Map.entry("9F49", "DDOL"),
            Map.entry("9F4A", "Static Data Authentication Tag List"),

            Map.entry("9F4B", "Signed Dynamic Application Data"),
            Map.entry("9F4C", "ICC Dynamic Number"),
            Map.entry("9F4D", "Log Entry"),
            Map.entry("9F4E", "Merchant Name and Location"),
            Map.entry("9F4F", "Log Format"),

            Map.entry("9F53", "Transaction Category Code"),
            Map.entry("9F5B", "Issuer Script Results"),
            Map.entry("9F66", "Terminal Transaction Qualifiers"),
            Map.entry("9F6C", "Mag Stripe Application Version Number"),
            Map.entry("9F6E", "Third Party Data"),
            Map.entry("9F7C", "Customer Exclusive Data")
    );

    static String nameOf(String tag) {
        return NAMES.getOrDefault(tag, "Tag " + tag);
    }

    private EmvTagDictionary() {
    }
}