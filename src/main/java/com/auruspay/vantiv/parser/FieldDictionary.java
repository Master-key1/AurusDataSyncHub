package com.worldpay.iso8583.parser;

import com.worldpay.iso8583.model.FieldDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Field layout table for every ISO 8583 bit (2-128).
 * <p>
 * The bits that appear in our actual Worldpay/Vantiv sample traffic (3, 4, 7, 11-13, 18, 19,
 * 22, 23, 25, 32, 35, 37-39, 41-43, 45, 49, 52, 55, 60, 62, 120, 127) are transcribed from
 * Worldpay's ISO 8583 Reference Guide and validated against real captures - those are as
 * accurate as this project gets.
 * <p>
 * Every other bit below is filled in from the generic ISO 8583:1987 field table so that
 * <b>no bit is ever unknown</b> - an unmapped bit used to hard-stop the parser entirely, which
 * is worse than a best-effort guess. These are NOT confirmed against a Worldpay/Vantiv spec and
 * are deliberately wired up as raw pass-through only (see {@code llVarRaw}/{@code lllVarBinary}
 * for variable fields, {@code binaryFixed} for fixed fields): the parser reads the correct
 * number of bytes so the cursor stays aligned for whatever comes after, but never validates or
 * breaks down the content, and prints it as-is. Private/reserved ranges in particular (48,
 * 56-59, 61, 63, 90-126) vary a lot processor-to-processor in real deployments - treat the
 * lengths here as a reasonable generic default, not a guarantee, until we see a real sample
 * that exercises them.
 * <p>
 * Extend or correct this map as needed - the parser and comparator are driven entirely off
 * this table, nothing else needs to change.
 */
public final class FieldDictionary {

    private static final Map<Integer, FieldDefinition> DEFINITIONS = new LinkedHashMap<>();

    static {
        // ---- Worldpay/Vantiv-confirmed fields (from real captures) ----
        add(FieldDefinition.llVarNumeric(2, "Primary Account Number (PAN)"));
        add(FieldDefinition.fixedNumeric(3, "Processing Code", 3, 6));
        add(FieldDefinition.fixedNumeric(4, "Transaction Amount", 6, 12));
        add(FieldDefinition.fixedNumeric(7, "Transmission Date and Time", 5, 10));
        add(FieldDefinition.fixedNumeric(11, "Systems Trace Audit Number", 3, 6));
        add(FieldDefinition.fixedNumeric(12, "Local Transaction Time", 3, 6));
        add(FieldDefinition.fixedNumeric(13, "Local Transaction Date", 2, 4));
        add(FieldDefinition.fixedNumeric(18, "Merchant Type", 2, 4));
        add(FieldDefinition.fixedNumeric(19, "Acquiring Institution Country Code", 2, 3));
        add(FieldDefinition.fixedNumeric(22, "Point of Service Entry Mode", 2, 4));
        add(FieldDefinition.fixedNumeric(23, "Card Sequence Number", 2, 3));
        add(FieldDefinition.fixedNumeric(25, "Point of Service Condition Code", 1, 2));
        add(FieldDefinition.llVarNumeric(32, "Acquiring Institution Identification Code"));
        add(FieldDefinition.track2(35, "Track II Data"));
        add(FieldDefinition.fixedAlpha(37, "Retrieval Reference Number", 12));
        add(FieldDefinition.fixedAlpha(38, "Authorization Identification Response", 6));
        add(FieldDefinition.fixedAlpha(39, "Response Code", 2));
        add(FieldDefinition.fixedAlpha(41, "Card Acceptor Terminal Identification", 15));
        add(FieldDefinition.fixedAlpha(42, "Card Acceptor Identification", 15));
        add(FieldDefinition.fixedAlpha(43, "Card Acceptor Name and Location Data", 40));
        add(FieldDefinition.llVarRaw(45, "Track I Data"));
        add(FieldDefinition.fixedNumeric(49, "Transaction Currency Code", 2, 3));
        add(FieldDefinition.binaryFixed(52, "Personal Identification Number Data", 8));
        add(FieldDefinition.lllVarBinary(55, "Integrated Circuit Card Data"));
        add(FieldDefinition.lllVarBinary(60, "Additional POS Data"));
      //  add(FieldDefinition.decimalAscii(60.1, "Additional POS Data: Terminal Type",1));
        add(FieldDefinition.lllVarBinary(62, "Vantiv Transaction Data"));
        add(FieldDefinition.lllVarAscii(120, "Additional Request Data"));
        add(FieldDefinition.lllVarBinary(127, "Forwarding / Private Data"));

        // ---- Generic ISO 8583:1987 fields, raw pass-through, not Worldpay-confirmed ----
        add(FieldDefinition.binaryFixed(5, "Amount, Reconciliation", 6));
        add(FieldDefinition.binaryFixed(6, "Amount, Cardholder Billing", 6));
        add(FieldDefinition.binaryFixed(8, "Amount, Cardholder Billing Fee", 4));
        add(FieldDefinition.binaryFixed(9, "Conversion Rate, Reconciliation", 4));
        add(FieldDefinition.binaryFixed(10, "Conversion Rate, Cardholder Billing", 4));
        add(FieldDefinition.binaryFixed(14, "Date, Expiration", 2));
        add(FieldDefinition.binaryFixed(15, "Date, Settlement", 2));
        add(FieldDefinition.binaryFixed(16, "Date, Conversion", 2));
        add(FieldDefinition.binaryFixed(17, "Date, Capture", 2));
        add(FieldDefinition.binaryFixed(20, "PAN Extended Country Code", 2));
        add(FieldDefinition.binaryFixed(21, "Forwarding Institution Country Code", 2));
        add(FieldDefinition.binaryFixed(24, "Function Code (Network International ID)", 2));
        add(FieldDefinition.binaryFixed(26, "POS PIN Capture Code", 1));
        add(FieldDefinition.binaryFixed(27, "Authorizing ID Response Length", 1));
        add(FieldDefinition.binaryFixed(28, "Amount, Transaction Fee", 9));
        add(FieldDefinition.binaryFixed(29, "Amount, Settlement Fee", 9));
        add(FieldDefinition.binaryFixed(30, "Amount, Transaction Processing Fee", 9));
        add(FieldDefinition.binaryFixed(31, "Amount, Settlement Processing Fee", 9));
        add(FieldDefinition.llVarRaw(33, "Forwarding Institution Identification Code"));
        add(FieldDefinition.llVarRaw(34, "PAN Extended"));
        add(FieldDefinition.lllVarBinary(36, "Track III Data"));
        add(FieldDefinition.binaryFixed(40, "Service Restriction Code", 3));
        add(FieldDefinition.llVarRaw(44, "Additional Response Data"));
        add(FieldDefinition.lllVarBinary(46, "Additional Data - ISO"));
        add(FieldDefinition.lllVarBinary(47, "Additional Data - National"));
        add(FieldDefinition.lllVarBinary(48, "Additional Data - Private"));
        add(FieldDefinition.binaryFixed(50, "Currency Code, Settlement", 2));
        add(FieldDefinition.binaryFixed(51, "Currency Code, Cardholder Billing", 2));
        add(FieldDefinition.binaryFixed(53, "Security Related Control Information", 8));
        add(FieldDefinition.lllVarBinary(54, "Additional Amounts"));
        add(FieldDefinition.lllVarBinary(56, "Reserved - ISO"));
        add(FieldDefinition.lllVarBinary(57, "Reserved - National"));
        add(FieldDefinition.lllVarBinary(58, "Reserved - National"));
        add(FieldDefinition.lllVarBinary(59, "Reserved - National"));
        add(FieldDefinition.lllVarBinary(61, "Reserved - Private"));
        add(FieldDefinition.lllVarBinary(63, "Reserved - Private"));
        add(FieldDefinition.binaryFixed(64, "Message Authentication Code (MAC)", 8));
        add(FieldDefinition.binaryFixed(65, "Extended Bitmap Indicator", 8));
        add(FieldDefinition.binaryFixed(66, "Settlement Code", 1));
        add(FieldDefinition.binaryFixed(67, "Extended Payment Code", 1));
        add(FieldDefinition.binaryFixed(68, "Receiving Institution Country Code", 2));
        add(FieldDefinition.binaryFixed(69, "Settlement Institution Country Code", 2));
        add(FieldDefinition.binaryFixed(70, "Network Management Information Code", 2));
        add(FieldDefinition.binaryFixed(71, "Message Number", 2));
        add(FieldDefinition.binaryFixed(72, "Message Number, Last", 2));
        add(FieldDefinition.binaryFixed(73, "Date, Action", 3));
        add(FieldDefinition.binaryFixed(74, "Credits, Number", 5));
        add(FieldDefinition.binaryFixed(75, "Credits, Reversal Number", 5));
        add(FieldDefinition.binaryFixed(76, "Debits, Number", 5));
        add(FieldDefinition.binaryFixed(77, "Debits, Reversal Number", 5));
        add(FieldDefinition.binaryFixed(78, "Transfer Number", 5));
        add(FieldDefinition.binaryFixed(79, "Transfer, Reversal Number", 5));
        add(FieldDefinition.binaryFixed(80, "Inquiries Number", 5));
        add(FieldDefinition.binaryFixed(81, "Authorizations, Number", 5));
        add(FieldDefinition.binaryFixed(82, "Credits, Processing Fee Amount", 6));
        add(FieldDefinition.binaryFixed(83, "Credits, Transaction Fee Amount", 6));
        add(FieldDefinition.binaryFixed(84, "Debits, Processing Fee Amount", 6));
        add(FieldDefinition.binaryFixed(85, "Debits, Transaction Fee Amount", 6));
        add(FieldDefinition.binaryFixed(86, "Credits, Amount", 8));
        add(FieldDefinition.binaryFixed(87, "Credits, Reversal Amount", 8));
        add(FieldDefinition.binaryFixed(88, "Debits, Amount", 8));
        add(FieldDefinition.binaryFixed(89, "Debits, Reversal Amount", 8));
        add(FieldDefinition.binaryFixed(90, "Original Data Elements", 21));
        add(FieldDefinition.binaryFixed(91, "File Update Code", 1));
        add(FieldDefinition.binaryFixed(92, "File Security Code", 2));
        add(FieldDefinition.binaryFixed(93, "Response Indicator", 5));
        add(FieldDefinition.binaryFixed(94, "Service Indicator", 7));
        add(FieldDefinition.binaryFixed(95, "Replacement Amounts", 42));
        add(FieldDefinition.binaryFixed(96, "Message Security Code", 8));
        add(FieldDefinition.binaryFixed(97, "Amount, Net Settlement", 17));
        add(FieldDefinition.binaryFixed(98, "Payee", 25));
        add(FieldDefinition.llVarRaw(99, "Settlement Institution Identification Code"));
        add(FieldDefinition.llVarRaw(100, "Receiving Institution Identification Code"));
        add(FieldDefinition.binaryFixed(101, "File Name", 17));
        add(FieldDefinition.llVarRaw(102, "Account Identification 1"));
        add(FieldDefinition.llVarRaw(103, "Account Identification 2"));
        add(FieldDefinition.lllVarBinary(104, "Transaction Description"));
        add(FieldDefinition.lllVarBinary(105, "Reserved - ISO"));
        add(FieldDefinition.lllVarBinary(106, "Reserved - ISO"));
        add(FieldDefinition.lllVarBinary(107, "Reserved - ISO"));
        add(FieldDefinition.lllVarBinary(108, "Reserved - ISO"));
        add(FieldDefinition.lllVarBinary(109, "Reserved - ISO"));
        add(FieldDefinition.lllVarBinary(110, "Reserved - ISO"));
        add(FieldDefinition.lllVarBinary(111, "Reserved - ISO"));
        add(FieldDefinition.lllVarBinary(112, "Reserved - National"));
        add(FieldDefinition.lllVarBinary(113, "Reserved - National"));
        add(FieldDefinition.lllVarBinary(114, "Reserved - National"));
        add(FieldDefinition.lllVarBinary(115, "Reserved - National"));
        add(FieldDefinition.lllVarBinary(116, "Reserved - National"));
        add(FieldDefinition.lllVarBinary(117, "Reserved - National"));
        add(FieldDefinition.lllVarBinary(118, "Reserved - National"));
        add(FieldDefinition.lllVarBinary(119, "Reserved - National"));
        add(FieldDefinition.lllVarBinary(121, "Reserved - Private"));
        add(FieldDefinition.lllVarBinary(122, "Reserved - Private"));
        add(FieldDefinition.lllVarBinary(123, "Reserved - Private"));
        add(FieldDefinition.lllVarAscii(124, "Transaction Dependent Data"));
        add(FieldDefinition.lllVarBinary(125, "Network Management Information"));
        add(FieldDefinition.lllVarAscii(126, "Electronic Commerce/MOTO Indicator"));
        add(FieldDefinition.binaryFixed(128, "Message Authentication Code (MAC)", 8));
    }

    private static void add(FieldDefinition def) {
        DEFINITIONS.put(def.bit(), def);
    }

    public static FieldDefinition get(int bit) {
        return DEFINITIONS.get(bit);
    }

    public static boolean isKnown(int bit) {
        return DEFINITIONS.containsKey(bit);
    }

    public static Map<Integer, FieldDefinition> all() {
        return DEFINITIONS;
    }

    private FieldDictionary() {
    }
}