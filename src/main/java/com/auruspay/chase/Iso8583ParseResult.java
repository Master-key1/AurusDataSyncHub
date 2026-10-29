package com.auruspay.chase;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Structured result of parsing one ISO 8583 message.
 * fields is keyed by DE (data element) number, e.g. 2 -> PAN, 4 -> amount, etc.
 * Values are kept as raw strings (as extracted) so callers decide how to
 * further validate / mask / persist them (PAN and track data in particular
 * should be masked before logging or storage per PCI-DSS).
 */
public class Iso8583ParseResult {

    private String mti;
    private String primaryBitmapBinary;
    private String secondaryBitmapBinary;
    private final Map<Integer, String> fields = new LinkedHashMap<>();

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public String getPrimaryBitmapBinary() {
        return primaryBitmapBinary;
    }

    public void setPrimaryBitmapBinary(String primaryBitmapBinary) {
        this.primaryBitmapBinary = primaryBitmapBinary;
    }

    public String getSecondaryBitmapBinary() {
        return secondaryBitmapBinary;
    }

    public void setSecondaryBitmapBinary(String secondaryBitmapBinary) {
        this.secondaryBitmapBinary = secondaryBitmapBinary;
    }

    public Map<Integer, String> getFields() {
        return fields;
    }

    public String getField(int bit) {
        return fields.get(bit);
    }

    public void putField(int bit, String value) {
        fields.put(bit, value);
    }

    @Override
    public String toString() {
        return "Iso8583ParseResult{mti=" + mti + ", fields=" + fields + "}";
    }
}
