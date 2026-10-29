package com.auruspay.chase;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Parses raw ISO 8583 request messages (as received - base64/AES encrypted)
 * into a structured {@link Iso8583ParseResult}.
 *
 * This is a direct refactor of the original Parser.main() console demo:
 *  - decryption is delegated to AesMessageDecryptor instead of being
 *    instantiated inline (constructor injection, testable with a mock)
 *  - System.out.println tracing is replaced with SLF4J logging
 *  - the original's "print an error and `return;`" validation failures
 *    now throw Iso8583ParseException so a REST layer can turn them into a
 *    proper error response instead of silently truncating output
 *  - parsed values are collected into Iso8583ParseResult.fields keyed by
 *    DE number, instead of only being printed
 *
 * PCI-DSS NOTE: bit 2 (PAN), bit 35/45 (track data), and bit 52 (PIN block)
 * are sensitive cardholder data. This class returns them raw in the result
 * object as the original did - make sure whatever logs or persists an
 * Iso8583ParseResult masks/truncates those fields (e.g. first6+last4 for
 * PAN) rather than writing them out in full. Nothing in this class should
 * be changed to log those fields directly.
 */
@Service
public class Iso8583MessageParser {

    private static final Logger log = LoggerFactory.getLogger(Iso8583MessageParser.class);

    private static final List<String> ISO_1110_STYLE_MTIS =
            Arrays.asList("1110", "1210", "1310", "1410", "1430", "1510", "1810");
    private static final List<String> ISO_1100_STYLE_MTIS =
            Arrays.asList("1100", "1200", "1300", "1400", "1420", "1500", "1800");

    private final AesMessageDecryptor decryptor;

    public Iso8583MessageParser(AesMessageDecryptor decryptor) {
        this.decryptor = decryptor;
    }

    /**
     * @param rawRequest the incoming (base64/AES-encrypted) ISO8583 request
     * @return structured parse result
     * @throws Iso8583ParseException on decryption failure or a field that
     *         fails length/format validation
     */
    public Iso8583ParseResult parseMessage(String rawRequest) throws Iso8583ParseException {
        Iso8583ParseResult result = new Iso8583ParseResult();

        String decrypted = decryptor.decrypt(rawRequest);
        log.debug("Decrypted payload: {}", decrypted);

        String mti = determineMti(decrypted);
        result.setMti(mti);
        log.info("MTI resolved to {}", mti);

        if (ISO_1110_STYLE_MTIS.contains(mti)) {
            decrypted = decrypted.substring(24);
        } else {
            decrypted = decrypted.substring(48);
        }

        String bitMap = hex2Binary(decrypted.substring(0, 16));
        decrypted = decrypted.substring(16);

        String isomsg;
        if (ISO_1110_STYLE_MTIS.contains(mti)) {
            isomsg = decrypted;
        } else {
            isomsg = hexToText(decrypted);
        }
        log.debug("Message body: {}", isomsg);

        int counter = 0;
        int i = 1;
        while (i <= bitMap.length()) {
            if (bitMap.charAt(i - 1) == '1') {
                switch (i) {
                    case 1: {
                        log.debug("Secondary bitmap present");
                        String secondMap = hex2Binary(decrypted.substring(0, 16));
                        decrypted = decrypted.substring(16);
                        bitMap += secondMap;
                        isomsg = hexToText(decrypted);
                        result.setSecondaryBitmapBinary(secondMap);
                        break;
                    }
                    case 2: {
                        int panLength = Integer.parseInt(isomsg.substring(0, 2));
                        counter = 2;
                        int end = counter + panLength;
                        result.putField(2, isomsg.substring(counter, end));
                        counter = panLength + 1;
                        break;
                    }
                    case 3: {
                        if ("1500".equals(mti)) {
                            result.putField(3, isomsg.substring(counter, counter += 6));
                        } else {
                            result.putField(3, isomsg.substring(counter + 1, counter += 7));
                        }
                        break;
                    }
                    case 4:
                        result.putField(4, isomsg.substring(counter, counter += 12));
                        break;
                    case 7:
                        result.putField(7, isomsg.substring(counter, counter += 14));
                        break;
                    case 8:
                        result.putField(8, isomsg.substring(counter, counter += 12));
                        break;
                    case 11:
                        result.putField(11, isomsg.substring(counter, counter += 6));
                        break;
                    case 12:
                        result.putField(12, isomsg.substring(counter, counter += 6));
                        break;
                    case 13:
                        result.putField(13, isomsg.substring(counter, counter += 8));
                        break;
                    case 14:
                        result.putField(14, isomsg.substring(counter, counter += 4));
                        break;
                    case 15:
                        result.putField(15, isomsg.substring(counter, counter += 4));
                        break;
                    case 18: {
                        String mccRaw = isomsg.substring(counter, counter += 4);
                        int mcc = Integer.parseInt(mccRaw);
                        if (String.valueOf(mcc).length() == 4 && mcc != 0 && mcc != 9999) {
                            result.putField(18, mccRaw);
                        } else {
                            throw new Iso8583ParseException("Bit 18 Merchant Category Code is incorrect: " + mcc);
                        }
                        break;
                    }
                    case 22:
                        result.putField(22, isomsg.substring(counter, counter += 3));
                        break;
                    case 23:
                        result.putField(23, isomsg.substring(counter, counter += 2));
                        break;
                    case 25:
                        result.putField(25, isomsg.substring(counter, counter += 2));
                        break;
                    case 35: {
                        int len = Integer.parseInt(isomsg.substring(counter, counter += 2));
                        result.putField(35, isomsg.substring(counter, counter += len));
                        break;
                    }
                    case 37:
                        result.putField(37, isomsg.substring(counter, counter += 12));
                        break;
                    case 38:
                        result.putField(38, isomsg.substring(counter, counter += 6));
                        break;
                    case 39:
                        result.putField(39, isomsg.substring(counter, counter += 2));
                        break;
                    case 41:
                        result.putField(41, isomsg.substring(counter, counter += 3));
                        break;
                    case 42:
                        result.putField(42, isomsg.substring(counter, counter += 12));
                        break;
                    case 44: {
                        int len = Integer.parseInt(isomsg.substring(counter, counter += 3));
                        result.putField(44, isomsg.substring(counter, counter += len));
                        break;
                    }
                    case 45: {
                        int len = Integer.parseInt(isomsg.substring(counter, counter += 2));
                        result.putField(45, isomsg.substring(counter, counter += len));
                        break;
                    }
                    case 48:
                        counter = parseBit48(isomsg, counter, result);
                        break;
                    case 49:
                        result.putField(49, isomsg.substring(counter, counter += 3));
                        break;
                    case 52:
                        result.putField(52, isomsg.substring(counter, counter += 16));
                        break;
                    case 53:
                        result.putField(53, isomsg.substring(counter, counter += 16));
                        break;
                    case 54:
                        result.putField(54, isomsg.substring(counter, counter += 12));
                        break;
                    case 55: {
                        int len = Integer.parseInt(isomsg.substring(counter, counter += 3));
                        if (len >= 3 && len <= 513) {
                            result.putField(55, isomsg.substring(counter, counter += len));
                        }
                        // else: original silently skipped - preserved as-is
                        break;
                    }
                    case 60: {
                        int len = Integer.parseInt(isomsg.substring(counter, counter += 3));
                        result.putField(60, isomsg.substring(counter, counter += len));
                        break;
                    }
                    case 62:
                        counter = parseBit62(isomsg, counter, result);
                        break;
                    case 63:
                        counter = parseBit63(isomsg, counter, result);
                        break;
                    case 70:
                        result.putField(70, isomsg.substring(counter, counter += 3));
                        break;
                    case 71:
                        result.putField(71, isomsg.substring(counter, counter += 7));
                        break;
                    case 72:
                        result.putField(72, isomsg.substring(counter, counter += 12));
                        break;
                    case 90:
                        result.putField(90, isomsg.substring(counter, counter += 46));
                        break;
                    case 121: {
                        int len = Integer.parseInt(isomsg.substring(counter, counter += 3));
                        result.putField(121, isomsg.substring(counter, counter += len));
                        break;
                    }
                    case 122: {
                        int len = Integer.parseInt(isomsg.substring(counter, counter += 3));
                        result.putField(122, isomsg.substring(counter, counter += len));
                        break;
                    }
                    case 123: {
                        int len = Integer.parseInt(isomsg.substring(counter, counter += 3));
                        result.putField(123, isomsg.substring(counter, counter += len));
                        break;
                    }
                    default:
                        // bit set in bitmap but no handler defined - matches
                        // original behavior of simply not printing anything
                        break;
                }
            }
            i++;
        }

        result.setPrimaryBitmapBinary(bitMap);
        return result;
    }

    private String determineMti(String decrypted) {
        try {
            String mti = hexToText(decrypted).substring(20).substring(0, 4);
            if (ISO_1110_STYLE_MTIS.contains(mti)) {
                return decrypted.substring(20).substring(0, 4);
            } else if (ISO_1100_STYLE_MTIS.contains(mti)) {
                return hexToText(decrypted).substring(20).substring(0, 4);
            } else {
                return decrypted.substring(20).substring(0, 4);
            }
        } catch (Exception e) {
            return decrypted.substring(20).substring(0, 4);
        }
    }

    private int parseBit48(String isomsg, int counter, Iso8583ParseResult result) throws Iso8583ParseException {
        int len = Integer.parseInt(isomsg.substring(counter, counter + 3));
        counter += 3;
        int end = counter + len;
        StringBuilder sub = new StringBuilder();
        while (counter < end) {
            String subTag = isomsg.substring(counter, counter += 2);
            int subLen = Integer.parseInt(isomsg.substring(counter, counter += 2));
            validateBit48SubTagLength(subTag, subLen);
            sub.append(subTag).append(':').append(isomsg.substring(counter, counter += subLen)).append(';');
            if ("ZZ".equals(subTag)) {
                break;
            }
        }
        result.putField(48, sub.toString());
        return counter;
    }

    private void validateBit48SubTagLength(String subTag, int len) throws Iso8583ParseException {
        switch (subTag) {
            case "A1": require(len >= 9 && len <= 29, subTag, len); break;
            case "A6": require(len >= 1 && len <= 32, subTag, len); break;
            case "A7": require(len >= 12 && len <= 32, subTag, len); break;
            case "B1": require(len == 18 || len == 34 || len == 50, subTag, len); break;
            case "B3": require(len == 21 || len == 40, subTag, len); break;
            case "BI": require(len == 1, subTag, len); break;
            case "C1": require(len == 3 || len == 4, subTag, len); break;
            case "D7": require(len >= 1 && len <= 25, subTag, len); break;
            case "E1": require(len >= 1 && len <= 17, subTag, len); break;
            case "E4": require(len >= 1 && len <= 16, subTag, len); break;
            case "E5": require(len == 18 || len == 34 || len == 50 || len == 66 || len == 82 || len == 98, subTag, len); break;
            case "E7": require(len >= 1 && len <= 5, subTag, len); break;
            case "H1": require(len == 14 || len == 28 || len == 42 || len == 56 || len == 70, subTag, len); break;
            case "P3": require(len >= 9 && len <= 16, subTag, len); break;
            case "P4": require(len >= 4 && len <= 45, subTag, len); break;
            case "R1": require(len >= 1 && len <= 30, subTag, len); break;
            case "R4": require(len >= 1 && len <= 99, subTag, len); break;
            default:
                // ZZ and anything else unlisted: no range check, matches original
                break;
        }
    }

    private int parseBit62(String isomsg, int counter, Iso8583ParseResult result) throws Iso8583ParseException {
        int len = Integer.parseInt(isomsg.substring(counter, counter + 3));
        counter += 3;
        int end = counter + len;
        StringBuilder sub = new StringBuilder();
        while (counter < end) {
            String subTag = isomsg.substring(counter, counter += 2);
            int subLen = Integer.parseInt(isomsg.substring(counter, counter += 3));
            switch (subTag) {
                case "DV": require(subLen <= 100, subTag, subLen); break;
                case "G2": require(subLen >= 1 && subLen <= 100, subTag, subLen); break;
                case "P2": require(subLen >= 16 && subLen <= 999, subTag, subLen); break;
                case "P3": require(subLen >= 6 && subLen <= 256, subTag, subLen); break;
                case "ZD": require(subLen >= 16 && subLen <= 248, subTag, subLen); break;
                case "H1": require(subLen >= 1 && subLen <= 16, subTag, subLen); break;
                default: break;
            }
            sub.append(subTag).append(':').append(isomsg.substring(counter, counter += subLen)).append(';');
        }
        result.putField(62, sub.toString());
        return counter;
    }

    private int parseBit63(String isomsg, int counter, Iso8583ParseResult result) throws Iso8583ParseException {
        int len = Integer.parseInt(isomsg.substring(counter, counter + 3));
        counter += 3;
        int end = counter + len;
        StringBuilder sub = new StringBuilder();
        while (counter < end) {
            String subTag = isomsg.substring(counter, counter += 2);
            int subLen = Integer.parseInt(isomsg.substring(counter, counter += 3));
            switch (subTag) {
                case "D6":
                    require(subLen == 13 || subLen == 28 || subLen == 43 || subLen == 58
                            || subLen == 73 || subLen == 88 || subLen == 103, subTag, subLen);
                    break;
                case "E1":
                    require(subLen == 18 || subLen == 20, subTag, subLen);
                    break;
                case "P2":
                    require(subLen == 36 || subLen == 71 || subLen == 106 || subLen == 141
                            || subLen == 176 || subLen == 211 || subLen == 246 || subLen == 316, subTag, subLen);
                    break;
                case "P3":
                    require(subLen == 46 || subLen == 90 || subLen == 134 || subLen == 178
                            || subLen == 222 || subLen == 266 || subLen == 310 || subLen == 354
                            || subLen == 398, subTag, subLen);
                    break;
                default:
                    break;
            }
            sub.append(subTag).append(':').append(isomsg.substring(counter, counter += subLen)).append(';');
        }
        result.putField(63, sub.toString());
        return counter;
    }

    private void require(boolean condition, String subTag, int len) throws Iso8583ParseException {
        if (!condition) {
            throw new Iso8583ParseException("Length of SubTag " + subTag + " is incorrect: " + len);
        }
    }

    // ---- helpers ported from the original Parser class ----

    static String hex2Binary(String hex) {
        StringBuilder retval = new StringBuilder();
        try {
            int[] binNum = new int[4 * hex.length()];
            for (int i = 0; i < hex.length(); i++) {
                int digit = Character.digit(hex.charAt(i), 16);
                binNum[4 * i] = digit / 8;
                digit %= 8;
                binNum[4 * i + 1] = digit / 4;
                digit %= 4;
                binNum[4 * i + 2] = digit / 2;
                digit %= 2;
                binNum[4 * i + 3] = digit;
            }
            for (int v : binNum) {
                retval.append(v);
            }
        } catch (Exception e) {
            log.warn("hex2Binary failed for input of length {}", hex == null ? -1 : hex.length(), e);
        }
        return retval.toString();
    }

    static String hexToText(String hex) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String hexPair = hex.substring(i, i + 2);
            try {
                int decimalValue = Integer.parseInt(hexPair, 16);
                output.append((char) decimalValue);
            } catch (NumberFormatException e) {
                log.debug("Skipping invalid hex pair '{}' at offset {}", hexPair, i);
            }
        }
        return output.toString();
    }
}
